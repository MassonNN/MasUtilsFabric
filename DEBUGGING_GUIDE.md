# MasUtils Config Screen Debug Guide

## Problem Summary
The config screen is being called but not displaying. The logs show:
```
[Render thread/INFO]: masutils command executed
[Render thread/INFO]: Opening MasUtils config screen
```

But no screen appears on-screen.

## Changes Made

### 1. Enhanced Logging in MasUtilsConfigScreen
**File**: `src/main/java/ru/massonnn/masutils/client/config/MasUtilsConfigScreen.java`

Added comprehensive logging throughout the screen lifecycle:
- Screen creation
- Screen initialization (`init()`)
- Config loading
- Button creation
- Category rendering
- Exception catching and logging

### 2. Fixed Mixin Configuration
**File**: `src/main/resources/masutils.mixins.json`

Added missing `BeaconBlockEntityRendererInvoker` mixin that was being referenced but not registered, which was causing crashes.

## How to Debug

### Step 1: Run the Mod
```bash
./gradlew runClient
```

### Step 2: Execute Config Command
In-game, run:
```
/masutils config
```

### Step 3: Check Logs
Check `run/logs/latest.log` for these log messages (in order):

1. ✅ `MasUtilsConfigScreen created with parent:`
2. ✅ `MasUtilsConfigScreen.init() called`
3. ✅ `Screen dimensions: width=X, height=Y`
4. ✅ `Config is null, loading from manager`
5. ✅ `Config loaded successfully: true`
6. ✅ `Creating category button...`
7. ✅ `Creating done button...`
8. ✅ `Adding options for category: general` (or whatever category)
9. ✅ `MasUtilsConfigScreen.init() completed successfully`

### Step 4: Identify Issues

**If you see "MasUtilsConfigScreen.init() called" but NOT "MasUtilsConfigScreen.init() completed successfully":**
- Check for `Error during MasUtilsConfigScreen initialization` message
- Look for the exception stacktrace after it
- Report the exception

**If you see "Screen dimensions: width=0, height=0":**
- The screen hasn't been properly sized yet
- This might be a Minecraft lifecycle issue

**If you DON'T see "MasUtilsConfigScreen created with parent:":**
- The screen isn't being created at all
- Check `MasutilsCommand.java` to see if it's being called

**If screen dimensions are valid but screen still doesn't appear:**
- There might be a rendering exception
- Check for `Error rendering config screen` in logs

## Files Modified

1. `src/main/java/ru/massonnn/masutils/client/config/MasUtilsConfigScreen.java`
   - Added logger instance
   - Added logging to constructor, init(), render(), and close()
   - Added try-catch blocks around init() and render()

2. `src/main/resources/masutils.mixins.json`
   - Added `accessors.BeaconBlockEntityRendererInvoker` to client mixins

## Next Steps After Debugging

Once you identify the root cause using the logging:
1. Share the relevant log lines
2. Include any exception stacktraces
3. Share what screen dimensions are being reported
4. Share which log messages appear and which don't

This will help pinpoint exactly where the issue is occurring.
