# QuickCart App Documentation

## 1. Overview
QuickCart is an Android grocery delivery app inspired by Blinkit-style UX.

Core capabilities:
- Multi-role login:
  - `USER` via Phone OTP
  - `VENDOR` via Email/Password
  - `ADMIN` via Email/Password
- Product browse/search/categories
- Cart and checkout
- Address capture
- Payment with backend session + verification
- Order tracking
- Admin order status management
- Vendor/Admin catalog management (add/edit/delete/in-stock)

## 2. Tech Stack
- Android: Kotlin + Jetpack Compose
- Navigation: Navigation Compose
- Backend: Node.js (Express)
- Auth: Firebase Authentication
- Data: Cloud Firestore
- Payments: Razorpay/Stripe via backend endpoints

## 3. App Structure
Main package: `com.example.quickcart`

Key modules:
- `ui/` screen and navigation layer
- `viewmodel/` state + orchestration (`AppViewModel`)
- `repository/` Firestore + payment APIs
- `auth/` Firebase auth wrapper
- `data/` models

Main entry:
- `app/src/main/java/com/example/quickcart/MainActivity.kt`
- `app/src/main/java/com/example/quickcart/ui/QuickCartApp.kt`

## 4. Navigation Routes
Defined in `QuickCartApp.kt`:
- `login`
- `home`
- `categories`
- `cart`
- `profile`
- `orders`
- `address`
- `payment`
- `admin`
- `vendor`
- `order/{orderId}`

Bottom tabs:
- Home
- Categories
- Cart
- Profile

## 5. Login and Role Flows

### USER
- Select `USER` tab on login screen.
- Login via phone OTP.
- Must have Firestore role `customer` (or default user role).

### VENDOR
- Select `VENDOR`.
- Login via email/password.
- Role validated against Firestore: must be `vendor`.

### ADMIN
- Select `ADMIN`.
- Login via email/password.
- Role validated against Firestore: must be `admin`.

If role mismatches selected type, app signs out and shows error.

## 6. Role Permissions in App
- `customer`:
  - browse, cart, checkout, payment, track order
- `vendor`:
  - all customer capabilities
  - manage catalog from `Profile -> Manage Catalog`
- `admin`:
  - all vendor capabilities
  - admin order status panel (`Admin Panel`)

## 7. Main Screens
- `LoginScreen`: role tabs + OTP/email login
- `HomeScreen`: search, category chips, promo cards, products, discount display
- `CategoriesScreen`: category-wise quick add
- `CartScreen`: line items, quantity controls, totals
- `AddressScreen`: shipping address
- `PaymentScreen`: select method, create payment session, open checkout, verify payment
- `OrderTrackingScreen`: live status timeline
- `ProfileScreen`: role-based links
- `OrdersScreen`: current order summary
- `AdminOrdersScreen`: order status progression
- `VendorDashboardScreen`: add/edit/delete products + stock toggle

## 8. Cart and Pricing UX
- Add-to-cart snackbar notification: `<product> added to cart`
- Discount old price rendered with strike-through
- Quantity visible in cart between `-` and `+` controls

## 9. Build and Run
Project root:
`C:\WINDOWS\system32\ai-project`

Build debug APK:
```powershell
.\gradlew.bat assembleDebug
```

APK output:
- `app/build/outputs/apk/debug/app-debug.apk`

Install to device:
```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## 10. Required Config
- `app/google-services.json` for Firebase
- `gradle.properties`:
  - `PAYMENT_BASE_URL=<your-backend-url>`

## 11. Important Files
- Navigation: `app/src/main/java/com/example/quickcart/ui/QuickCartApp.kt`
- State: `app/src/main/java/com/example/quickcart/viewmodel/AppViewModel.kt`
- Firestore repo: `app/src/main/java/com/example/quickcart/repository/StoreRepository.kt`
- Payment repo: `app/src/main/java/com/example/quickcart/repository/PaymentRepository.kt`
- Auth repo: `app/src/main/java/com/example/quickcart/auth/AuthRepository.kt`
