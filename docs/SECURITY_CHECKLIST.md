# Security Checklist

This checklist tracks the main security areas for future review. It is intentionally practical and tied to the current codebase.

## Secret Management

- Real database passwords must stay out of Git.
- WeChat Mini Program app secrets must stay out of Git.
- Local secrets belong in `config/application-local.yml` or environment variables.
- Build outputs such as `target/` must not be committed.

## Authentication and Authorization

- Verify all `/api/admin/**` endpoints require login and the `admin` role.
- Verify cart, order, address, and distribution APIs are scoped to the current user.
- Confirm public endpoints do not expose private user or order data.
- Add tests for unauthorized and cross-user access attempts.

## Input Validation

- Validate pagination inputs.
- Validate product IDs, spec IDs, order IDs, and address IDs.
- Validate order creation payloads before changing stock or creating order records.
- Validate distributor application input.

## Business Logic

- Review order status transitions.
- Ensure users can only cancel their own eligible orders.
- Review admin delivery flow for invalid status changes.
- Review commission calculation and settlement boundaries.
- Ensure product stock is updated consistently during order creation and cancellation.

## Dependency and Build Security

- Keep Spring Boot, MyBatis-Plus, Sa-Token, Knife4j, Hutool, and MySQL connector versions current.
- Run CI for every push and pull request.
- Add dependency scanning when available.

## Logging and Error Handling

- Do not log secrets, tokens, OpenID values unnecessarily, or raw credentials.
- Return consistent API errors without exposing stack traces in production.
- Review global exception handling before deployment.
