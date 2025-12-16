/**
 * claude.ts - Convex Action for Claude API Integration
 *
 * This file contains the server-side code that runs on Convex cloud.
 * It acts as a secure middleman between your Android app and the Claude API.
 *
 * WHY USE CONVEX AS A MIDDLEMAN?
 * - Keeps your API key secure (it stays on the server, not in the app)
 * - Handles CORS (Cross-Origin Resource Sharing) issues
 * - Allows you to add rate limiting, logging, or other features later
 *
 * HOW TO SET UP:
 * 1. Install Convex CLI: npm install -g convex
 * 2. Login to Convex: npx convex login
 * 3. Deploy this function: npx convex deploy
 * 4. Set your API key: npx convex env set ANTHROPIC_API_KEY your_api_key_here
 *
 * FLOW:
 * Android App -> Convex (this file) -> Claude API -> Convex -> Android App
 */

import { action } from "./_generated/server";
import { v } from "convex/values";

/**
 * getClaudeResponse - Main function that handles Claude API calls
 *
 * This is an "action" in Convex terminology, which means it can make
 * external HTTP requests (unlike "queries" and "mutations" which can't).
 *
 * @param userMessage - The message from the Android app user
 * @returns The text response from Claude
 */
export const getClaudeResponse = action({
  // Define the expected input arguments
  // v.string() means we expect a string value
  args: { userMessage: v.string() },

  // The handler function that runs when this action is called
  handler: async (ctx, args) => {
    // ==================== GET API KEY ====================
    // Retrieve the API key from Convex environment variables
    // This keeps the key secure and out of your code
    const apiKey = process.env.ANTHROPIC_API_KEY;

    // Safety check: make sure API key is configured
    if (!apiKey) {
      throw new Error(
        "ANTHROPIC_API_KEY not configured. " +
          "Run: npx convex env set ANTHROPIC_API_KEY your_key_here"
      );
    }

    // ==================== MAKE API REQUEST ====================
    // Send the user's message to Claude's API
    const response = await fetch("https://api.anthropic.com/v1/messages", {
      method: "POST",
      headers: {
        // Tell the server we're sending JSON data
        "Content-Type": "application/json",
        // Authenticate with your Anthropic API key
        "x-api-key": apiKey,
        // Specify the API version (required by Anthropic)
        "anthropic-version": "2023-06-01",
      },
      body: JSON.stringify({
        // The Claude model to use
        // claude-sonnet-4-20250514 is a great balance of speed and capability
        model: "claude-sonnet-4-20250514",

        // Maximum tokens (words/word pieces) in the response
        // 1000 is good for most conversational responses
        max_tokens: 1000,

        // The conversation messages
        // For now, we just send the user's single message
        messages: [
          {
            role: "user",
            content: args.userMessage,
          },
        ],
      }),
    });

    // ==================== HANDLE RESPONSE ====================
    // Check if the API request was successful
    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Claude API error (${response.status}): ${errorText}`);
    }

    // Parse the JSON response from Claude
    const data = await response.json();

    // ==================== EXTRACT AND RETURN TEXT ====================
    // Claude's response format includes an array of content blocks
    // The first block (index 0) contains the text response
    // We extract just the text to send back to the Android app
    if (data.content && data.content.length > 0 && data.content[0].text) {
      return data.content[0].text;
    } else {
      throw new Error("Unexpected response format from Claude API");
    }
  },
});
