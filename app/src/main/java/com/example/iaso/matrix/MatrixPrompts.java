package com.example.iaso.matrix;

/**
 * MatrixPrompts — Centralised prompt constants for the Matrix 1.0 AI pipeline.
 *
 * <p>All system prompts used by {@link MatrixEngine} are defined here as {@code static final}
 * string constants so they can be reviewed, tested, and iterated on independently of the
 * engine logic that sends them. No business logic lives in this class.
 *
 * <p>This class is not instantiable. All members are static.
 */
public final class MatrixPrompts {

    /** Utility class — do not instantiate. */
    private MatrixPrompts() {
    }

    /**
     * System prompt for the goal-to-milestone decomposition step.
     *
     * <p>Instructs Claude to act as a deterministic decomposition engine and return a
     * strict JSON array of milestone objects. Key constraints enforced by the prompt:
     * <ul>
     *   <li>Output is a raw JSON array — no markdown fences, no prose wrapper.</li>
     *   <li>Each object contains {@code name}, {@code description}, {@code allocatedDays},
     *       and {@code dependencies} fields.</li>
     *   <li>{@code allocatedDays} values across all milestones must sum to exactly
     *       {@code totalDays} as specified in the user message.</li>
     *   <li>Milestone descriptions must be outcome-oriented and measurable.</li>
     *   <li>Ordering must be foundational-first — each milestone builds on prior ones.</li>
     *   <li>Between 2 and 10 milestones; count chosen relative to goal complexity.</li>
     * </ul>
     *
     * <p>This constant is consumed exclusively by {@link MatrixEngine#generateTimeline}.
     * To iterate on prompt behaviour, edit this string and rebuild — no engine code changes
     * are required.
     */
    public static final String MATRIX_TIMELINE_PROMPT =
            "You are a precise goal decomposition engine. Your only job is to break a user's goal into an ordered sequence of milestones that fit exactly within their stated timeline and daily time commitment.\n\n" +
            "RULES:\n" +
            "1. Output ONLY a raw JSON array. No markdown fences, no preamble, no explanation.\n" +
            "2. The array must contain objects with exactly these fields:\n" +
            "   - \"name\": short milestone title (string)\n" +
            "   - \"description\": specific, measurable definition of done — what completion looks like in concrete terms (string)\n" +
            "   - \"allocatedDays\": number of calendar days for this milestone (integer >= 1)\n" +
            "   - \"dependencies\": array of zero-based indices of milestones that must complete before this one starts (integer array, empty if none)\n" +
            "3. allocatedDays values across all milestones MUST sum to exactly the totalDays provided by the user.\n" +
            "4. Milestones must be sequential and ordered from foundational to advanced — earlier milestones build the skills and habits required by later ones.\n" +
            "5. Each milestone description must be outcome-oriented and specific. Avoid vague language like 'learn about' or 'get familiar with'. State what the user will have produced, demonstrated, or proven by the end.\n" +
            "6. Do not include a buffer or review milestone unless it is genuinely useful for the goal type.\n" +
            "7. Minimum 2 milestones, maximum 10. Choose a count appropriate to the totalDays and goal complexity.\n" +
            "8. Never produce duplicate milestone names.\n\n" +
            "OUTPUT FORMAT (strict):\n" +
            "[{\"name\":\"...\",\"description\":\"...\",\"allocatedDays\":N,\"dependencies\":[]}, ...]";
}