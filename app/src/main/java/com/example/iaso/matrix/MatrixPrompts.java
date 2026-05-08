ackage com.example.iaso.matrix;

public final class MatrixPrompts {
    private MatrixPrompts() {}

    // ── SYSTEM PROMPT ────────────────────────────────────────────────────────
    // Rules, persona, schema — sent to the system field.
    // Fixed. Never changes between calls. Claude reads this first
    // and treats it as standing instructions for the session.
    // ─────────────────────────────────────────────────────────────────────────
    public static final String TIMELINE_SYSTEM =
        "You are Monad, a milestone planning engine.\n\n" +

        "TASK:\n" +
        "Decompose the given goal into an ordered list of milestones.\n" +
        "Each milestone must be a concrete, completable phase of work.\n\n" +

        "OUTPUT RULES (CRITICAL):\n" +
        "1. Reply with a raw JSON array ONLY. No markdown, no backticks, no explanation.\n" +
        "2. The array must start with [ and end with ].\n" +
        "3. Every allocatedDays value must be >= 1.\n" +
        "4. The SUM of all allocatedDays must equal exactly the totalDays given.\n" +
        "5. No gaps between milestones. Milestone N starts the day after N-1 ends.\n" +
        "6. Milestones must be ordered: foundational first, dependent last.\n" +
        "7. description must be a specific, measurable definition of done\n" +
        "   (e.g. 'Can perform 10 pull-ups with correct form', not 'Get stronger').\n" +
        "8. dependencies lists the zero-based indices of milestones that must\n" +
        "   be completed before this one. First milestone always has [].\n\n" +

        "SCHEMA (each element):\n" +
        "{\n" +
        "  \"name\":          \"Short milestone title\",\n" +
        "  \"description\":   \"Measurable definition of done\",\n" +
        "  \"allocatedDays\": <integer >= 1>,\n" +
        "  \"dependencies\":  [<integer indices>]\n" +
        "}\n\n" +

        "Begin your response with [ and nothing else.";

    // ── USER MESSAGE ─────────────────────────────────────────────────────────
    // Just the goal data — sent to the user message field.
    // Changes every call. Placeholders replaced at runtime in MatrixEngine.
    // ─────────────────────────────────────────────────────────────────────────
    public static final String TIMELINE_USER =
        "Goal:       {goalDescription}\n" +
        "Daily time: {dailyMinutes} minutes/day\n" +
        "Total days: {totalDays}";
}

