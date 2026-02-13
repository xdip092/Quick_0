# Backend and Deployment Documentation

## 1. Backend Location
- `backend/src/server.js`
- `backend/.env.example`
- `backend/package.json`

## 2. Purpose
Provides payment session creation and server-side verification for:
- Razorpay
- Stripe

## 3. API Endpoints

### `GET /health`
Returns backend health and provider config status.

### `POST /payments/create-session`
Request:
```json
{
  "userId": "uid123",
  "amount": 499,
  "currency": "INR",
  "gateway": "RAZORPAY"
}
```
Response:
```json
{
  "sessionId": "uuid",
  "gateway": "RAZORPAY",
  "checkoutUrl": "https://..."
}
```

### `POST /payments/verify-session`
Request:
```json
{
  "sessionId": "uuid"
}
```
Response:
```json
{
  "sessionId": "uuid",
  "status": "PAID"
}
```

## 4. Environment Variables
From `backend/.env.example`:
- `PORT`
- `ALLOWED_ORIGIN`
- `RAZORPAY_KEY_ID`
- `RAZORPAY_KEY_SECRET`
- `STRIPE_SECRET_KEY`
- `STRIPE_SUCCESS_URL`
- `STRIPE_CANCEL_URL`
- `DEFAULT_CURRENCY`

## 5. Backend Run
```powershell
cd C:\WINDOWS\system32\ai-project\backend
npm install
npm run dev
```

## 6. Android App Payment Config
In project `gradle.properties`:
```properties
PAYMENT_BASE_URL=http://10.0.2.2:8080
```

Use:
- `10.0.2.2` for Android emulator -> localhost mapping
- local network IP for real device testing

## 7. Firestore Rules Deploy
Project:
- `quickkart-dccef`

Deploy command:
```powershell
cd C:\WINDOWS\system32\ai-project
firebase deploy --only firestore:rules --project quickkart-dccef
```

## 8. APK Build and Install
Build:
```powershell
cd C:\WINDOWS\system32\ai-project
.\gradlew.bat assembleDebug
```

Install:
```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## 9. GitHub Upload (manual fallback)
If folder is not a git repo:
```powershell
cd C:\WINDOWS\system32\ai-project
git init
git add .
git commit -m "Initial QuickCart app + backend + docs"
git branch -M main
git remote add origin <YOUR_GITHUB_REPO_URL>
git push -u origin main
```
