# Firebase Setup Guide

## Required Firebase Services

- Firestore Database
- Cloud Messaging
- Firebase Authentication or app-level teacher authentication

## Android App Registration

Planned package name:

```text
com.walhero.myteacher
```

## Configuration File

Production builds require a valid `google-services.json` file for the package name above.

Do not commit sensitive private service account files.

## Suggested Firestore Collections

```text
classes
students
messages
parent_message_access
```

## Suggested Access Tracking

```text
parent_message_access/{studentId_month}
  studentId
  month
  freeOpenedMessageIds
  adUnlockedMessageIds
  updatedAt
```

## Security Notes

Before release, Firestore rules must prevent unauthorized access to message data.
