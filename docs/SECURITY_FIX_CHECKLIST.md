# Security Fix Release Checklist

## Intent Redirection Vulnerability Fix
**Deadline**: April 18, 2026  
**Version**: 341003+ (increment from 341002)

---

## Pre-Release Testing

### Functional Testing
- [ ] Test media button shortcuts work correctly
  - [ ] Play/Pause button
  - [ ] Next track button
  - [ ] Previous track button
  - [ ] Stop button
- [ ] Test phone call shortcuts work
  - [ ] Direct dial shortcuts launch correctly
  - [ ] Permission prompts work (if needed)
- [ ] Test app launcher shortcuts
  - [ ] Can launch other apps
  - [ ] Shortcuts open correct apps
- [ ] Test folder shortcuts
  - [ ] Folders open correctly
  - [ ] All items in folder accessible
- [ ] Test widget functionality
  - [ ] Widgets update correctly
  - [ ] Button clicks work
  - [ ] Settings open correctly

### Security Testing
- [ ] Verify logs show security validation working
  - Check logcat for "ShortcutActivity: Intent from untrusted package" messages
- [ ] Try launching from external test app (should be blocked)
- [ ] Verify no dangerous flags are accepted

### Compatibility Testing
- [ ] Test on Android 13+ device (API 33+)
- [ ] Test on older Android device (API 31+)
- [ ] Test on different manufacturers (Samsung, Google Pixel, etc.)

---

## Build Process

### 1. Update Version
- [ ] Increment version code in `build.gradle.kts`
  ```kotlin
  versionCode = 341003  // or higher
  versionName = "X.X.X"
  ```
- [ ] Update version name if needed
- [ ] Commit version changes

### 2. Clean Build
```bash
cd /Users/algavris/dev/CarWidget
./gradlew clean
./gradlew assembleRelease  # or bundleRelease for AAB
```

### 3. Verify Build
- [ ] Build completes without errors
- [ ] No lint security warnings
- [ ] ProGuard mapping file generated
- [ ] APK/AAB signed correctly

---

## Upload to Google Play Console

### 1. Create Release
- [ ] Go to Google Play Console
- [ ] Navigate to Release > Production (or Internal Testing first)
- [ ] Create new release

### 2. Upload Build
- [ ] Upload signed APK or AAB
- [ ] Version code should be > 341002
- [ ] Wait for upload to complete

### 3. Release Notes
Add release notes mentioning the security fix:
```
What's new:
- Security improvements and bug fixes
- Enhanced app stability
- Performance optimizations
```

**Note**: Don't explicitly mention "Intent Redirection vulnerability" in user-facing notes.

### 4. Review and Rollout
- [ ] Review all information
- [ ] Start rollout (consider staged rollout: 10%, 25%, 50%, 100%)
- [ ] Submit for review

---

## Post-Release Monitoring

### First 24 Hours
- [ ] Monitor crash reports in Play Console
- [ ] Check for ANRs (Application Not Responding)
- [ ] Review user feedback/ratings
- [ ] Monitor logs for security violations

### First Week
- [ ] Check analytics for any drops in engagement
- [ ] Review all crash reports
- [ ] Monitor for any shortcut-related issues
- [ ] Respond to user reviews mentioning issues

### Metrics to Watch
- Crash-free users rate
- ANR rate
- User rating trends
- Reviews mentioning shortcuts or "not working"

---

## Rollback Plan

If critical issues are found:

1. **Immediate Actions**
   - [ ] Pause rollout in Play Console
   - [ ] Document the issue
   - [ ] Collect crash logs

2. **Investigation**
   - [ ] Identify root cause
   - [ ] Determine if security fix related
   - [ ] Create hotfix if needed

3. **Rollback** (if necessary)
   - [ ] Revert to previous version in git
   - [ ] Remove security fix temporarily
   - [ ] Add TODO to re-implement fix properly
   - [ ] Build and upload emergency release

**Note**: Rollback should be last resort. Try to fix forward if possible.

---

## Communication

### Internal Team
- [ ] Notify team about security fix
- [ ] Share testing results
- [ ] Document any issues found

### Users (if needed)
- [ ] Respond to reviews about issues
- [ ] Provide support through help channels
- [ ] Update FAQ if common questions arise

---

## Success Criteria

The release is successful when:

✅ Google Play accepts the new version  
✅ No increase in crash rate  
✅ All shortcuts continue to work  
✅ Security logs show validation working  
✅ No user reports of broken functionality  
✅ Stable for 7 days with no critical issues  

---

## Important Notes

### Security Fix Details
- The fix uses `IntentSanitizer` (Google's recommended approach)
- Validates calling package is our own app
- Blocks dangerous URI permission flags
- See `SECURITY_FIX_INTENT_REDIRECTION.md` for technical details

### Testing Priority
Focus testing on:
1. **Phone call shortcuts** - Uses sensitive permissions
2. **Media buttons** - Most common use case
3. **Third-party app launches** - Ensure still works

### Deadline
- **Hard deadline**: April 18, 2026
- **Recommended**: Release by March 2026 (gives buffer time)
- **Minimum testing**: 1 week before deadline

---

## Questions & Support

### Technical Questions
- Review: `SECURITY_FIX_INTENT_REDIRECTION.md`
- Check: Code comments in `ShortcutActivity.kt`
- Test: Run security validation manually

### Google Play Issues
- Google Play Console Help Center
- Android Developer Support
- Security Policy Documentation

---

**Last Updated**: January 18, 2026  
**Status**: Ready for testing and release

---

## Quick Commands Reference

```bash
# Clean build
./gradlew clean

# Build release AAB
./gradlew bundleRelease

# Build release APK
./gradlew assembleRelease

# Run lint checks
./gradlew lint

# Check for errors
./gradlew :app:compileDebugKotlin

# Generate mapping file
./gradlew :app:assembleRelease
# Find mapping.txt in: app/build/outputs/mapping/release/mapping.txt
```

---

Good luck with the release! 🚀
