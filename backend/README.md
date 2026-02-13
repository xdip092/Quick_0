# QuickCart Payments Backend

Node/Express backend for the app payment flow.

## Endpoints
- `POST /payments/create-session`
- `POST /payments/verify-session`
- `GET /health`

## Setup
1. Copy `.env.example` to `.env`.
2. Fill provider credentials.
3. Install and run:

```bash
npm install
npm run dev
```

Server default: `http://localhost:8080`

## Request/Response

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

## Provider Notes
- Razorpay: creates Payment Link, verifies using Payment Link status.
- Stripe: creates Checkout Session, verifies using Checkout Session payment status.

## Production Notes
- Replace in-memory `sessions` map with Redis or DB.
- Add auth for backend endpoints.
- Add webhooks for faster payment confirmation.
