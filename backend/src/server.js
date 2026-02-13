const express = require("express");
const cors = require("cors");
const dotenv = require("dotenv");
const axios = require("axios");
const Stripe = require("stripe");
const { v4: uuidv4 } = require("uuid");

dotenv.config();

const app = express();
app.use(express.json());
app.use(cors({ origin: process.env.ALLOWED_ORIGIN || "*" }));

const PORT = Number(process.env.PORT || 8080);
const DEFAULT_CURRENCY = process.env.DEFAULT_CURRENCY || "INR";

const stripe =
  process.env.STRIPE_SECRET_KEY && process.env.STRIPE_SECRET_KEY.trim()
    ? new Stripe(process.env.STRIPE_SECRET_KEY)
    : null;

// In-memory session store for starter setup.
// Use Redis/DB in production.
const sessions = new Map();

app.get("/health", (_req, res) => {
  res.json({
    ok: true,
    service: "quickcart-payments-backend",
    providers: {
      razorpay: Boolean(process.env.RAZORPAY_KEY_ID && process.env.RAZORPAY_KEY_SECRET),
      stripe: Boolean(stripe)
    }
  });
});

app.post("/payments/create-session", async (req, res) => {
  try {
    const { userId, amount, currency = DEFAULT_CURRENCY, gateway } = req.body || {};
    if (!userId || !amount || !gateway) {
      return res.status(400).json({ error: "userId, amount, and gateway are required" });
    }
    if (amount <= 0) {
      return res.status(400).json({ error: "amount must be > 0" });
    }

    const normalizedGateway = String(gateway).toUpperCase();
    if (normalizedGateway !== "RAZORPAY" && normalizedGateway !== "STRIPE") {
      return res.status(400).json({ error: "gateway must be RAZORPAY or STRIPE" });
    }

    const sessionId = uuidv4();
    let checkoutUrl = "";
    let providerReferenceId = "";

    if (normalizedGateway === "RAZORPAY") {
      providerReferenceId = await createRazorpayPaymentLink({
        sessionId,
        amount,
        currency
      }).then((result) => {
        checkoutUrl = result.checkoutUrl;
        return result.referenceId;
      });
    } else {
      providerReferenceId = await createStripeCheckoutSession({
        sessionId,
        amount,
        currency
      }).then((result) => {
        checkoutUrl = result.checkoutUrl;
        return result.referenceId;
      });
    }

    sessions.set(sessionId, {
      sessionId,
      userId,
      gateway: normalizedGateway,
      amount,
      currency,
      providerReferenceId,
      status: "PENDING",
      createdAt: Date.now()
    });

    return res.json({
      sessionId,
      gateway: normalizedGateway,
      checkoutUrl
    });
  } catch (error) {
    return res.status(500).json({ error: normalizeError(error) });
  }
});

app.post("/payments/verify-session", async (req, res) => {
  try {
    const { sessionId } = req.body || {};
    if (!sessionId) {
      return res.status(400).json({ error: "sessionId is required" });
    }

    const existing = sessions.get(sessionId);
    if (!existing) {
      return res.status(404).json({ error: "Session not found" });
    }

    let status = "PENDING";
    if (existing.gateway === "RAZORPAY") {
      status = await verifyRazorpayPaymentLink(existing.providerReferenceId);
    } else if (existing.gateway === "STRIPE") {
      status = await verifyStripeCheckoutSession(existing.providerReferenceId);
    }

    existing.status = status;
    sessions.set(sessionId, existing);

    return res.json({
      sessionId,
      status
    });
  } catch (error) {
    return res.status(500).json({ error: normalizeError(error) });
  }
});

async function createRazorpayPaymentLink({ sessionId, amount, currency }) {
  const keyId = process.env.RAZORPAY_KEY_ID;
  const keySecret = process.env.RAZORPAY_KEY_SECRET;
  if (!keyId || !keySecret) {
    throw new Error("Missing RAZORPAY_KEY_ID/RAZORPAY_KEY_SECRET");
  }

  const response = await axios.post(
    "https://api.razorpay.com/v1/payment_links",
    {
      amount: Math.round(amount * 100),
      currency: currency.toUpperCase(),
      reference_id: sessionId,
      description: `QuickCart order payment (${sessionId})`
    },
    {
      auth: {
        username: keyId,
        password: keySecret
      }
    }
  );

  return {
    referenceId: response.data.id,
    checkoutUrl: response.data.short_url
  };
}

async function verifyRazorpayPaymentLink(paymentLinkId) {
  const keyId = process.env.RAZORPAY_KEY_ID;
  const keySecret = process.env.RAZORPAY_KEY_SECRET;
  if (!keyId || !keySecret) {
    throw new Error("Missing RAZORPAY_KEY_ID/RAZORPAY_KEY_SECRET");
  }

  const response = await axios.get(`https://api.razorpay.com/v1/payment_links/${paymentLinkId}`, {
    auth: {
      username: keyId,
      password: keySecret
    }
  });

  const status = String(response.data.status || "").toLowerCase();
  return status === "paid" ? "PAID" : "PENDING";
}

async function createStripeCheckoutSession({ sessionId, amount, currency }) {
  if (!stripe) {
    throw new Error("Missing STRIPE_SECRET_KEY");
  }

  const successUrl = process.env.STRIPE_SUCCESS_URL;
  const cancelUrl = process.env.STRIPE_CANCEL_URL;
  if (!successUrl || !cancelUrl) {
    throw new Error("Missing STRIPE_SUCCESS_URL/STRIPE_CANCEL_URL");
  }

  const session = await stripe.checkout.sessions.create({
    mode: "payment",
    success_url: successUrl,
    cancel_url: cancelUrl,
    metadata: {
      appSessionId: sessionId
    },
    line_items: [
      {
        quantity: 1,
        price_data: {
          currency: currency.toLowerCase(),
          unit_amount: Math.round(amount * 100),
          product_data: {
            name: "QuickCart Order"
          }
        }
      }
    ]
  });

  return {
    referenceId: session.id,
    checkoutUrl: session.url
  };
}

async function verifyStripeCheckoutSession(stripeSessionId) {
  if (!stripe) {
    throw new Error("Missing STRIPE_SECRET_KEY");
  }

  const session = await stripe.checkout.sessions.retrieve(stripeSessionId);
  return session.payment_status === "paid" ? "PAID" : "PENDING";
}

function normalizeError(error) {
  if (error?.response?.data?.error?.description) {
    return error.response.data.error.description;
  }
  if (error?.response?.data?.error?.message) {
    return error.response.data.error.message;
  }
  return error?.message || "Unknown server error";
}

app.listen(PORT, () => {
  console.log(`Payment backend running on http://localhost:${PORT}`);
});
