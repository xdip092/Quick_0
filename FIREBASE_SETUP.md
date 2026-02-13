# Firebase + Payments Setup

## 1) Configure live payment backend

Set `PAYMENT_BASE_URL` in `gradle.properties`.
If you use the included local backend starter, set:
`PAYMENT_BASE_URL=http://10.0.2.2:8080` (Android emulator)

App calls:
- `POST /payments/create-session`
- `POST /payments/verify-session`

Expected payload/response:
- create request: `{ userId, amount, currency, gateway }`
- create response: `{ sessionId, gateway, checkoutUrl }`
- verify request: `{ sessionId }`
- verify response: `{ status: "PAID" | "FAILED" }`

Checkout URL should be a provider page (Razorpay or Stripe hosted checkout).

## 1.1 Optional direct SDK path

### Razorpay (recommended Android flow)
1. Add Razorpay SDK dependency in `app/build.gradle.kts`.
2. Initialize Checkout with your key id.
3. Create backend endpoint to verify signature (`razorpay_payment_id`, `razorpay_order_id`, `razorpay_signature`).
4. Only mark `paymentStatus = PAID` after backend verification.

### Stripe (PaymentSheet flow)
1. Add Stripe Android SDK.
2. Build backend endpoint to create PaymentIntent.
3. Use PaymentSheet in app.
4. Confirm status from webhook/backend, then mark order as paid.

## 2) Enable admin role

Set role on user document:
`users/{uid}.role = "admin"`

Then app shows admin icon in top bar and opens `AdminOrdersScreen`.

## 3) Deploy Firestore rules

Rules file: `firestore.rules`

Use Firebase CLI:
`firebase deploy --only firestore:rules`

## Notes
- Firestore order status updates are real-time and reflected in customer tracking timeline.
- Status chain: `PLACED -> PACKED -> OUT_FOR_DELIVERY -> DELIVERED`.
