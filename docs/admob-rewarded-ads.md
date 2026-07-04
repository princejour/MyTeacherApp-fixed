# AdMob Rewarded Ads Guide

## Purpose

Rewarded ads are used to unlock extra messages after the monthly free limit.

## Monthly Logic

```text
Messages 1 to 5: free access
Message 6 and above: locked until rewarded ad completion
```

## Development Test ID

Use the official Android rewarded test ad unit during development:

```text
ca-app-pub-3940256099942544/5224354917
```

## Production ID

Before release, replace the test ID with the real rewarded ad unit from AdMob:

```text
ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy
```

## Unlock Rule

The message must unlock only after the rewarded completion callback is received.

## Storage

Unlocked messages should be stored in Firebase so the same message stays unlocked after the app restarts.
