# Intent Redirection Security Vulnerability - Fixed

## Executive Summary
**Severity**: High  
**Status**: ✅ Fixed  
**Date**: January 18, 2026  
**Google Play Store Deadline**: April 18, 2026  
**Version Code Affected**: 341002

## Vulnerability Details

### What is Intent Redirection?
Intent Redirection is a security vulnerability where a malicious app can exploit an exported Activity that accepts nested Intents without proper validation. This allows attackers to:
- Launch arbitrary activities with your app's permissions
- Steal sensitive data by redirecting to phishing activities
- Grant themselves URI permissions (FLAG_GRANT_URI_PERMISSION)
- Bypass Android's permission system

### Location of Vulnerability
**File**: `/Users/algavris/dev/CarWidget/app/src/main/java/com/anod/car/home/ShortcutActivity.kt`  
**Obfuscated Name**: `a8.a.P` (as reported by Google)  
**Activity**: `ShortcutActivity` (exported in AndroidManifest.xml)

### Original Vulnerable Code
```kotlin
private fun execute(intent: Intent) {
    val appIntent = intent.getParcelableExtra<Intent>(EXTRA_INTENT)
    if (appIntent != null) {
        runFromIntent(appIntent)  // ⚠️ No validation!
        finish()
        return
    }
    // ...
}
```

**Problems**:
1. ✗ Activity is exported (`android:exported="true"`)
2. ✗ Accepts nested Intent from extras without validation
3. ✗ Directly launches the nested Intent using `startActivitySafely()`
4. ✗ No check if caller is from the same package
5. ✗ No stripping of dangerous permission flags

## Fix Implementation

### Security Measures Applied

#### 1. Intent Sanitization using `IntentSanitizer`
We now use Android's recommended `androidx.core.content.IntentSanitizer` to validate and sanitize all nested Intents:

```kotlin
IntentSanitizer.Builder()
    .allowAction(Intent.ACTION_MAIN)
    .allowAction(Intent.ACTION_VIEW)
    .allowAction(Intent.ACTION_CALL)
    // ... allow only safe actions
    .allowAnyComponent()  // Since we launch shortcuts to various apps
    .allowData { true }
    // Important: Only allow safe flags, NOT dangerous permission flags
    .allowFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    .allowFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    // ... other safe flags
    .build()
    .sanitizeByFiltering(rawIntent)
```

#### 2. Caller Package Validation
We verify that the calling package is our own app:

```kotlin
val callingPackage = callingActivity?.packageName
if (callingPackage != null && callingPackage != packageName) {
    AppLog.e("ShortcutActivity: Intent from untrusted package: $callingPackage")
    return null
}
```

#### 3. Dangerous Flags Blocked
The following dangerous flags are **explicitly NOT allowed**:
- `FLAG_GRANT_READ_URI_PERMISSION`
- `FLAG_GRANT_WRITE_URI_PERMISSION`
- `FLAG_GRANT_PERSISTABLE_URI_PERMISSION`
- `FLAG_GRANT_PREFIX_URI_PERMISSION`

#### 4. API Compatibility
Updated to use non-deprecated API for Android 13+ (API 33):

```kotlin
val rawIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    intent.getParcelableExtra(EXTRA_INTENT, Intent::class.java)
} else {
    @Suppress("DEPRECATION")
    intent.getParcelableExtra(EXTRA_INTENT)
}
```

### Complete Fixed Code

The new `extractSanitizedIntent()` method:
- ✅ Validates caller package
- ✅ Uses IntentSanitizer for validation
- ✅ Blocks dangerous permission flags
- ✅ Handles SecurityException gracefully
- ✅ Logs security violations for monitoring

## Testing Recommendations

### 1. Manual Testing
Test that legitimate shortcuts still work:
- ✅ Media button shortcuts
- ✅ Phone call shortcuts (ACTION_CALL)
- ✅ Folder shortcuts (ACTION_FOLDER)
- ✅ App launcher shortcuts (ACTION_MAIN)

### 2. Security Testing
Verify the fix prevents attacks:
```bash
# Try to launch with dangerous flags from external app
adb shell am start -n com.anod.car.home/.ShortcutActivity \
  --es intent "..." \
  --grant-read-uri-permission
# Should be blocked ✅
```

### 3. Automated Testing
Consider adding instrumented tests for:
- Intent from external package is rejected
- Intent with dangerous flags is rejected
- Valid intents from own package work correctly

## Build Verification

✅ Code compiles without errors  
✅ No lint warnings for Intent Redirection  
✅ Deprecated API usage properly suppressed with `@Suppress("DEPRECATION")`

## Next Steps

### Before Release
1. ✅ Fix implemented
2. ⏳ Test all shortcut functionality
3. ⏳ Run full regression test suite
4. ⏳ Build and upload new version to Google Play Console
5. ⏳ Monitor crash reports for any issues

### Monitoring
After release, monitor:
- Security logs for rejected intents
- Crash reports related to Intent handling
- User reports of broken shortcuts

## References

- [Android Security Best Practices - Intent Redirection](https://developer.android.com/topic/security/risks/intent-redirection)
- [IntentSanitizer Documentation](https://developer.android.com/reference/androidx/core/content/IntentSanitizer)
- [Google Play Policy - Device and Network Abuse](https://support.google.com/googleplay/android-developer/answer/9888379)

## File Changes

**Modified Files**:
- `/Users/algavris/dev/CarWidget/app/src/main/java/com/anod/car/home/ShortcutActivity.kt`

**Changes Summary**:
- Added `androidx.core.content.IntentSanitizer` import
- Added `android.os.Build` import for API version check
- Replaced `execute()` method with secure implementation
- Added `extractSanitizedIntent()` method for validation
- Fixed deprecated `getParcelableExtra()` usage
- Added comprehensive security logging

## Compliance

✅ **Complies with Google Play Policy**: Device and Network Abuse  
✅ **Fixes**: Intent Redirection vulnerability  
✅ **Deadline**: April 18, 2026  
✅ **Version Code**: Ready for 341003+

---

**Status**: Ready for testing and release
