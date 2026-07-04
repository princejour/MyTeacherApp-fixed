# My Teacher

Premium Android app for secure parent-teacher communication, student message delivery, monthly free message access, and rewarded-ad unlocks.

## Purpose

My Teacher helps teachers communicate with parents through a simple, secure, and mobile-first Android experience. Teachers can manage students, send messages, and parents can access their child-specific messages using a private access code.

## Core Features

- Teacher login
- Teacher password change
- Student list import
- Parent access code per student
- Secure parent message inbox
- Monthly free message limit
- Rewarded ad unlock after the free monthly limit
- Firebase-ready architecture
- Google Play preparation documents
- Premium documentation for store release

## Parent Message Access Logic

Parents can open 5 messages for free each month. Starting from the 6th message, the message is locked until the parent watches a rewarded ad. After the rewarded ad is completed successfully, the message becomes unlocked.

## Planned Android Package

```text
com.walhero.myteacher
```

## Planned Store Name

```text
My Teacher
```

## Repository Status

This repository is being prepared as the English premium version of the parent-teacher communication app. The documentation, store assets, privacy notes, and release checklist are prepared before the Android source code is added.

## Documentation

- [FAQ](FAQ.md)
- [Privacy Policy](PRIVACY_POLICY.md)
- [Terms of Use](TERMS_OF_USE.md)
- [Support](SUPPORT.md)
- [Security](SECURITY.md)
- [Changelog](CHANGELOG.md)
- [Store Listing](docs/store-listing.md)
- [Data Safety Notes](docs/data-safety-notes.md)
- [Screenshots Guide](docs/screenshots-guide.md)
- [Release Checklist](docs/release-checklist.md)
- [Firebase Setup](docs/firebase-setup.md)
- [AdMob Rewarded Ads](docs/admob-rewarded-ads.md)

## Important Notice

This app handles school communication data. Production release must use real Firebase configuration, proper Firestore security rules, a valid privacy policy, and Google Play Data Safety declarations that match the app behavior.

## License

MIT License. See [LICENSE](LICENSE).
