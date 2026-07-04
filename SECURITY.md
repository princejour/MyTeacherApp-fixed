# Security Policy

## Supported Versions

Security updates will focus on the latest published version of the app.

## Reporting a Security Issue

Security contact: to be added before release.

Please include:

- Description of the issue
- Steps to reproduce
- Affected version
- Screenshots or logs if available

## Security Rules

Production releases must follow these rules:

- Do not commit private signing keys.
- Do not commit production Firebase secrets.
- Do not expose service account files.
- Use secure Firestore rules.
- Use HTTPS-only external resources.
- Use test ads during development.
- Use production AdMob IDs only after testing is complete.

## Firebase Security

Firestore rules must restrict access so parents can only access messages linked to their own parent code, and teachers can only perform teacher-level actions after authentication.

## Release Security Checklist

Before each release:

- Confirm package name.
- Confirm signing key.
- Confirm versionCode is increased.
- Confirm Firebase configuration.
- Confirm AdMob mode.
- Confirm privacy policy link.
- Confirm Google Play Data Safety answers.
