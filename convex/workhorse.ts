/**
 * convex/claude.ts — Convo1.0 Conversational Refinement Engine
 *
 * KAN-6: Multi-Turn Conversation + Monad System Prompt
 *
 * This file replaces the single-shot Claude integration with a multi-turn
 * conversational engine. It supports two phases:
 *   - "refining"   → Monad asks clarifying questions (Convo1.0 loop)
 *   - "generating"  → Monad generates milestones (Matrix1.0)
 *
 * The system prompt encodes a state machine:
 *   INITIAL_INTAKE → CLARIFYING → VALIDATING → COMPLETE
 *
 * Structured tokens used for phase transitions:
 *   ---GOAL_SUMMARY--- / ---END_SUMMARY---   → Monad presents summary for approval
 *   ---GOAL_APPROVED--- / ---END_APPROVED---  → User approved, ready for milestones
 */

import { action } from "./_generated/server";
import { v } from "convex/values";

// ============================================================
// SYSTEM PROMPTS
// ============================================================

/**
 * Builds the Convo1.0 refinement prompt.
 * Monad acts as a goal refinement coach, asking one question at a time
 * until the goal meets Definition of Done criteria.
 */
function buildRefiningPrompt(dailyMinutes: number, targetDays: number): string {
  return `You are Monad, the AI engine inside OpenSpace — a platform that helps people achieve long-term goals through disciplined daily execution.

Your current job: GOAL REFINEMENT. The user has described a goal they want to achieve. Your job is to have a focused, back-and-forth conversation to refine this goal until it is crystal clear, specific, measurable, and achievable within their timeframe.

CONTEXT:
- The user has committed to investing ${dailyMinutes} minutes per day
- They want to achieve this goal within ${targetDays} days

YOUR REFINEMENT PROCESS:
1. INITIAL_INTAKE: Read their goal. Identify what's vague, unmeasurable, or unrealistic.
2. CLARIFYING: Ask ONE focused question at a time. Never dump a list of 5 questions. Be conversational, not interrogative. Probe for:
   - What specifically does success look like? (measurable outcome)
   - What's their current level/starting point?
   - What's in scope vs out of scope?
   - What resources/tools do they have or need?
   - Is the timeline realistic given their daily commitment?
3. VALIDATING: Once you believe you have enough clarity, present a GOAL SUMMARY in this exact format:

---GOAL_SUMMARY---
Goal: [one sentence, specific and measurable]
Success Criteria: [how they'll know they achieved it]
Scope: [what's included, what's excluded]
Starting Point: [their current level]
Daily Commitment: ${dailyMinutes} minutes/day
Timeline: ${targetDays} days
---END_SUMMARY---

Ask the user: "Does this capture your goal accurately? Say 'approve' to lock it in, or tell me what to adjust."

4. COMPLETE: When the user approves, respond with exactly:
---GOAL_APPROVED---
[the final approved goal summary]
---END_APPROVED---

RULES:
- Be warm but direct. You're a coach, not a therapist.
- Ask ONE question per message. Wait for the answer.
- Don't over-explain. Keep messages under 3 sentences when asking questions.
- If the goal is already clear and specific, don't force unnecessary questions — move to VALIDATING quickly.
- Never generate milestones during this phase. That comes after approval.
- If the user asks you to just skip ahead, gently explain that clarity now saves months of wasted effort later.`;
}

/**
 * Builds the Matrix1.0 milestone generation prompt.
 * Uses the approved goal context to generate a structured milestone breakdown.
 */
function buildGeneratingPrompt(
  dailyMinutes: number,
  targetDays: number,
  goalSummary: string
): string {
  return `You are Monad, the AI engine inside OpenSpace. The user has gone through goal refinement and approved their goal. Now generate the milestone breakdown.

APPROVED GOAL CONTEXT:
${goalSummary}

GENERATE MILESTONES:
- Between 2-15 milestones based on the timeline
- 1-2 for under 25 days, 2-5 for under 100 days, 6-8 for 100-365, 9-12 for multi-year
- Each milestone must be specific and achievable
- Days per milestone must sum to approximately ${targetDays} days
- Account for the user's ${dailyMinutes} min/day commitment

RESPONSE FORMAT (strict — no other text):
TickerSymbol
milestone name, X days
milestone name, X days
...`;
}

// ============================================================
// HELPERS
// ============================================================

/**
 * Extracts the approved goal summary from the conversation history.
 * Scans assistant messages for the ---GOAL_APPROVED--- token.
 * Falls back to ---GOAL_SUMMARY--- if approved block not found.
 * Last resort: uses the user's first message as the goal description.
 */
function extractGoalSummary(
  messages: Array<{ role: string; content: string }>
): string {
  // Scan in reverse — most recent approval is the one we want
  for (let i = messages.length - 1; i >= 0; i--) {
    const msg = messages[i];
    if (msg.role !== "assistant") continue;

    // Check for approved block first
    const approvedStart = msg.content.indexOf("---GOAL_APPROVED---");
    const approvedEnd = msg.content.indexOf("---END_APPROVED---");
    if (approvedStart !== -1 && approvedEnd !== -1) {
      return msg.content
        .substring(approvedStart + "---GOAL_APPROVED---".length, approvedEnd)
        .trim();
    }

    // Fall back to summary block
    const summaryStart = msg.content.indexOf("---GOAL_SUMMARY---");
    const summaryEnd = msg.content.indexOf("---END_SUMMARY---");
    if (summaryStart !== -1 && summaryEnd !== -1) {
      return msg.content
        .substring(summaryStart + "---GOAL_SUMMARY---".length, summaryEnd)
        .trim();
    }
  }

  // Last resort: first user message is the raw goal
  const firstUserMsg = messages.find((m) => m.role === "user");
  return firstUserMsg?.content ?? "No goal provided";
}

// ============================================================
// MAIN ACTION
// ============================================================

export const getClaudeResponse = action({
  args: {
    messages: v.array(
      v.object({
        role: v.union(v.literal("user"), v.literal("assistant")),
        content: v.string(),
      })
    ),
    context: v.object({
      dailyMinutes: v.number(),
      targetDays: v.number(),
      phase: v.union(v.literal("refining"), v.literal("generating")),
    }),
  },

  handler: async (ctx, args) => {
    const apiKey = process.env.ANTHROPIC_API_KEY;
    if (!apiKey) {
      throw new Error("ANTHROPIC_API_KEY not configured in Convex environment");
    }

    // Select system prompt based on current phase
    const systemPrompt =
      args.context.phase === "refining"
        ? buildRefiningPrompt(args.context.dailyMinutes, args.context.targetDays)
        : buildGeneratingPrompt(
            args.context.dailyMinutes,
            args.context.targetDays,
            extractGoalSummary(args.messages)
          );

    const response = await fetch("https://api.anthropic.com/v1/messages", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-api-key": apiKey,
        "anthropic-version": "2023-06-01",
      },
      body: JSON.stringify({
        model: "claude-sonnet-4-20250514",
        max_tokens: 1500,
        system: systemPrompt,
        messages: args.messages,
      }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Claude API error (${response.status}): ${errorText}`);
    }

    const data = await response.json();

    if (data.content && data.content.length > 0 && data.content[0].text) {
      return data.content[0].text;
    }

    throw new Error("Unexpected response format from Claude API");
  },
});