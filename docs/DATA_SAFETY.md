# Play Console – Data Safety answers

Basis: app code (no own collection) + Google's published disclosures for the bundled SDKs:
- AdMob: https://developers.google.com/admob/android/privacy/play-data-disclosure
- Play Billing: https://developer.android.com/google/play/billing/data-safety (re-check before submitting; SDK versions change).

| Question | Answer |
|---|---|
| Does your app collect or share any required user data types? | **Yes** (via AdMob SDK) |
| Is all user data encrypted in transit? | **Yes** (SDKs use HTTPS) |
| Do you provide a way for users to request data deletion? | No developer-held data. Answer per current form wording; ads data deletion is via Android "Delete advertising ID". |

## Data types (AdMob — `play-services-ads` 23.x)

| Data type | Collected | Shared | Purpose | Optional? |
|---|---|---|---|---|
| Location → Approximate location (from IP) | Yes | Yes | Advertising or marketing, Analytics, Fraud prevention/security | Required |
| App activity → App interactions (ad taps/views) | Yes | Yes | Advertising, Analytics, Fraud prevention | Required |
| App info and performance → Diagnostics / Crash logs (SDK) | Yes | Yes | Analytics, Fraud prevention | Required |
| Device or other IDs → Advertising ID / app set ID | Yes | Yes | Advertising, Analytics, Fraud prevention | Required |

Processed ephemerally: No. Data stays on device only for: game progress, scores, settings (not "collected" – never leaves device).

## Play Billing
Purchase history is handled by Google Play itself; per Google's guidance it is normally declared as
**Financial info → Purchase history: Collected, not shared, App functionality**. Verify against the current Billing data-safety page.

## Other forms
- Ads: **Contains ads = Yes**
- Target audience: **13+ only** (13–15, 16–17, 18+). Do **not** select under-13 age groups.
  Ball sort games can look child-appealing → keep store listing/screenshots neutral (no cartoon kids, no "for kids" text), otherwise Play may require Families policy.
- Content rating (IARC): puzzle, no violence/gambling/user interaction → expected "Everyone / PEGI 3".
- Privacy policy URL: https://fatihsahinbas.github.io/BallSort-Bubble/privacy/
