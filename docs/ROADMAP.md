# Maintenance Roadmap

This roadmap lists the next improvements for Petshop Backend. It is not a promise of delivery dates, but it helps contributors understand project direction.

## Near Term

- Add unit tests for service-layer business logic.
- Add controller tests for login-required and admin-only endpoints.
- Document common request and response examples.
- Improve order status transition validation.
- Review user ownership checks for cart, order, address, and distribution APIs.

## Security and Reliability

- Add tests for unauthorized access and cross-user access.
- Add dependency scanning in GitHub Actions.
- Add production profile examples without secrets.
- Review exception handling and logging behavior.
- Document deployment hardening steps.

## Documentation

- Expand API examples in `docs/API_OVERVIEW.md`.
- Add local development screenshots or sample API calls.
- Add architecture notes for Mini Program frontend integration.
- Add release notes once the project starts versioned releases.

## Future Enhancements

- WeChat Pay integration notes.
- Cloud storage or object storage integration for product images.
- Distributor withdrawal flow.
- Search and filtering improvements.
- More complete admin statistics.
