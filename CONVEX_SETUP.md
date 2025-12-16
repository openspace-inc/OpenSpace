# Convex Backend Setup Guide

This guide explains how to set up and deploy the Convex backend that connects your Android app to Claude AI.

## Overview

The architecture looks like this:

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│   Android App   │ ───▶ │  Convex Cloud   │ ───▶ │   Claude API    │
│   (workhorse)   │ ◀─── │  (claude.ts)    │ ◀─── │   (Anthropic)   │
└─────────────────┘      └─────────────────┘      └─────────────────┘
```

Your Android app sends messages to Convex, which securely calls Claude's API (keeping your API key safe), and returns the response.

## Prerequisites

1. **Node.js** (version 18 or higher): Download from https://nodejs.org
2. **Anthropic API Key**: Get one from https://console.anthropic.com

## Step-by-Step Setup

### Step 1: Install Dependencies

Open a terminal in the project root (where `package.json` is located) and run:

```bash
npm install
```

This installs the Convex library.

### Step 2: Install Convex CLI (if not already installed)

```bash
npm install -g convex
```

### Step 3: Log in to Convex

```bash
npx convex login
```

This opens a browser window to authenticate with Convex.

### Step 4: Initialize or Link Your Convex Project

If this is a new project:
```bash
npx convex dev
```

If you already have a Convex deployment (like `neighborly-chihuahua-847`):
```bash
npx convex dev --once
```

### Step 5: Set Your Anthropic API Key

This is the most important step! Your API key is stored securely in Convex's environment variables (not in your code).

```bash
npx convex env set ANTHROPIC_API_KEY "your-api-key-here"
```

Replace `your-api-key-here` with your actual Anthropic API key.

### Step 6: Deploy the Backend

```bash
npx convex deploy
```

This deploys your `claude.ts` function to Convex cloud.

## Verifying the Setup

### Test from Command Line

You can test your deployment using curl:

```bash
curl -X POST https://neighborly-chihuahua-847.convex.cloud/api/action \
  -H "Content-Type: application/json" \
  -d '{"path": "claude:getClaudeResponse", "args": {"userMessage": "Hello, Claude!"}}'
```

You should get a JSON response with Claude's reply.

### Test from Android

1. Build and run your Android app
2. Navigate to the Workhorse screen
3. Type a message and tap send
4. You should see Claude's response appear

## Troubleshooting

### "ANTHROPIC_API_KEY not configured" Error

Run the environment variable command again:
```bash
npx convex env set ANTHROPIC_API_KEY "your-api-key-here"
```

### "Network error" in Android App

1. Make sure your Android device/emulator has internet access
2. Check that the INTERNET permission is in AndroidManifest.xml
3. Verify the Convex URL in `ConvexApiHelper.java` matches your deployment

### "Server error: 500"

Check the Convex dashboard logs at https://dashboard.convex.dev for detailed error messages.

### Response Format Issues

If Claude's response isn't displaying correctly, check that:
1. The Convex function is returning plain text (not JSON)
2. The Android app is correctly parsing the `value` field from Convex's response

## Files Reference

| File | Description |
|------|-------------|
| `convex/claude.ts` | Convex action that calls Claude API |
| `convex.json` | Convex configuration file |
| `package.json` | Node.js dependencies |
| `app/.../ConvexApiHelper.java` | Android helper for Convex calls |
| `app/.../workhorse.java` | Android chat activity |

## Security Notes

- **Never** put your API key directly in code
- The API key is stored securely in Convex environment variables
- Your Android app never sees the API key - only Convex does
- Convex handles HTTPS encryption automatically

## Costs

- **Convex**: Free tier includes 1M function calls/month
- **Claude API**: Pay-per-use, check https://anthropic.com/pricing

## Need Help?

- Convex Docs: https://docs.convex.dev
- Anthropic Docs: https://docs.anthropic.com
- Claude API Reference: https://docs.anthropic.com/claude/reference
