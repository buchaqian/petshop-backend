# API Overview

This document summarizes the major API groups in Petshop Backend. The exact request and response models are documented in the controller classes and can be inspected through Knife4j when the application is running.

## Public App APIs

### User

- `POST /api/app/user/login`

Handles WeChat Mini Program login. The backend exchanges a Mini Program login code for an OpenID, creates or updates the local user record, and returns a Sa-Token login token.

### Products

- `GET /api/app/product/categories`
- `GET /api/app/product/list`
- `GET /api/app/product/detail/{id}`

Provides public product browsing APIs. Product list supports category, keyword, and pagination parameters.

### Banner

- `GET /api/app/banner/list`

Provides banner data for the Mini Program home page.

## Authenticated App APIs

### Cart

- `GET /api/app/cart/list`
- `POST /api/app/cart/add`
- `POST /api/app/cart/update`
- `POST /api/app/cart/delete`

Cart APIs are user-scoped and require login.

### Orders

- `POST /api/app/order/create`
- `GET /api/app/order/list`
- `GET /api/app/order/detail/{id}`
- `POST /api/app/order/cancel`

Order APIs cover order creation, list queries, detail lookup, and cancellation. Order status transitions are a core security and business-logic review area.

### Addresses

- `GET /api/app/address/list`
- `POST /api/app/address/add`
- `POST /api/app/address/update`
- `POST /api/app/address/delete`
- `POST /api/app/address/setDefault`

Address APIs are user-scoped and should never expose another user's address data.

### Distribution

- `POST /api/app/distribution/apply`
- `GET /api/app/distribution/apply/status`
- `GET /api/app/distribution/commission/list`
- `GET /api/app/distribution/commission/stats`

Distribution APIs handle distributor applications and commission data. Commission ownership and settlement status should be reviewed carefully.

## Admin APIs

Admin APIs require login and an `admin` role.

### Products

- `GET /api/admin/product/list`
- `POST /api/admin/product/add`
- `POST /api/admin/product/update`
- `POST /api/admin/product/status`
- `POST /api/admin/product/delete`

### Orders

- `GET /api/admin/order/list`
- `GET /api/admin/order/detail/{id}`
- `POST /api/admin/order/deliver`

### Distributors

- `GET /api/admin/distributor/apply/list`
- `POST /api/admin/distributor/apply/approve`
- `POST /api/admin/distributor/apply/reject`
- `GET /api/admin/distributor/list`

### Statistics

- `GET /api/admin/statistics/overview`
- `GET /api/admin/statistics/trend`
- `GET /api/admin/statistics/ranking`

## Authentication Notes

Sa-Token is configured in `SaTokenConfig`. Public product and login endpoints are excluded from login checks. Cart, order, address, and distribution endpoints require login. Admin endpoints require an `admin` role.
