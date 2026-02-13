# QuickCart Database Documentation (Firestore)

## 1. Firestore Collections

### 1.1 `products` (top-level)
Product catalog managed by vendor/admin.

Document ID:
- auto-generated or existing product id

Fields:
- `name` (string)
- `category` (string)
- `qtyLabel` (string)
- `price` (number)
- `strikePrice` (number|null)
- `imageUrl` (string)
- `inStock` (boolean)
- `updatedAt` (timestamp)

---

### 1.2 `users/{uid}`
User profile and role.

Fields:
- `role` (string): `customer` | `vendor` | `admin`
- `createdAt` (timestamp)

Subcollections:
- `cart`
- `orders`

---

### 1.3 `users/{uid}/cart/{productId}`
Current user cart lines.

Fields:
- `name` (string)
- `qtyLabel` (string)
- `price` (number)
- `quantity` (number)
- `updatedAt` (timestamp)

---

### 1.4 `users/{uid}/orders/{orderId}`
Placed orders.

Fields:
- `userId` (string)
- `status` (string): `PLACED | PACKED | OUT_FOR_DELIVERY | DELIVERED`
- `total` (number)
- `paymentMethod` (string): `COD | RAZORPAY | STRIPE`
- `paymentStatus` (string): `PAID | FAILED | PENDING`
- `createdAt` (timestamp)
- `updatedAt` (timestamp)
- `address` (map)
  - `fullName`
  - `phone`
  - `line1`
  - `line2`
  - `city`
  - `pincode`
- `items` (array of maps)
  - `productId`
  - `name`
  - `qtyLabel`
  - `price`
  - `quantity`

## 2. Role Model
- `customer`: purchase flows
- `vendor`: catalog management + purchase flows
- `admin`: full control, incl. order status updates

## 3. Security Rules Summary
Rules file: `firestore.rules`

Policy highlights:
- `products`:
  - read: any signed-in user
  - write: `admin` or `vendor`
- `users/{uid}`:
  - owner can read/write own profile
  - admin can read/write any profile
- `cart`:
  - owner/admin only
- `orders`:
  - owner/admin read
  - owner can create own order with `status == PLACED`
  - only admin can update/delete orders

## 4. Role Setup Procedure
In Firebase Console -> Firestore:
1. Go to `users/{uid}`
2. Set `role` field:
   - customer user: `customer`
   - vendor account: `vendor`
   - admin account: `admin`

## 5. Required Firebase Auth Providers
Enable in Authentication:
- `Phone`
- `Email/Password`

## 6. Common Queries Used by App
- All products:
  - `collection("products")`
- User role:
  - `collection("users").document(uid)`
- Cart realtime:
  - `collection("users").document(uid).collection("cart")`
- Order realtime:
  - `collection("users").document(uid).collection("orders").document(orderId)`
- Admin all orders:
  - `collectionGroup("orders")`

## 7. Data Validation Recommendations (Production)
- Enforce positive price values with Cloud Functions or backend validation
- Restrict category set to approved values
- Store audit fields:
  - `createdBy`, `updatedBy`
- Move payment confirmation source-of-truth to backend webhook writes
