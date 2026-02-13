# Quick_0

# QuickCart Android App

Blinkit-style grocery delivery starter app built with Kotlin + Jetpack Compose + Firebase.

## Features
- Phone OTP login (Firebase Auth)
- Product listing from Firestore
- Cart management in Firestore
- Address -> Payment -> Order placement flow
- Live order tracking timeline
- Admin order panel with status progression

## Tech Stack
- Kotlin
- Jetpack Compose
- Navigation Compose
- Firebase Auth
- Cloud Firestore

## Project Path
`C:\WINDOWS\system32\ai-project`

## Run Steps
1. Open project in Android Studio.
2. Sync Gradle and run.
3. Optional for live Firebase mode: add `google-services.json` to `app/`.
4. Optional for live Firebase mode: enable Phone Auth and Firestore in Firebase Console.
5. Optional for live Firebase mode: add SHA-1/SHA-256 for your app in Firebase.

If Firebase is not configured yet, app auto-runs in demo mode.

## Terminal Build
Use:
```bash
./gradlew assembleDebug
```
Windows:
```powershell
.\gradlew.bat assembleDebug
```

## Firestore Rules
Rules file is at `firestore.rules`.

Deploy:
```bash
firebase deploy --only firestore:rules
```

## Admin Access
Set user role:
- Document: `users/{uid}`
- Field: `role = "admin"`

Vendor role:
- Document: `users/{uid}`
- Field: `role = "vendor"`

`admin` and `vendor` can open `Profile -> Manage Catalog` to add/edit/delete products.

## Payment Notes
- `COD`, `Razorpay`, `Stripe` options are present.
- For online payments, app now uses backend-created secure checkout sessions and server-side verification.
- Configure backend URL in `gradle.properties`:
  - `PAYMENT_BASE_URL=https://your-api-domain`
- Required backend endpoints:
  - `POST /payments/create-session` -> `{ sessionId, gateway, checkoutUrl }`
  - `POST /payments/verify-session` -> `{ status: "PAID" | "FAILED" }`

Backend starter is included at `backend/`:
- `backend/src/server.js`
- `backend/.env.example`
- `backend/README.md`

## Full Documentation
- `docs/APP_DOCUMENTATION.md`
- `docs/DATABASE_DOCUMENTATION.md`
- `docs/BACKEND_AND_DEPLOYMENT.md`
