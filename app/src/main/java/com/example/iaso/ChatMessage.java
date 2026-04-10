package com.example.iaso;

public class ChatMessage {
    private String role;       // "user" or "assistant"
    private String content;    // message text
    private long timestamp;    // for ordering in UI

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getRole() { return role; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }

    // Converts to JSON for sending to Convex
    public org.json.JSONObject toJson() throws org.json.JSONException {
        org.json.JSONObject obj = new org.json.JSONObject();
        obj.put("role", role);
        obj.put("content", content);
        return obj;
    }
}