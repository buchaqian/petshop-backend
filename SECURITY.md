# Security Policy

## Supported Versions

The `main` branch is the active development branch and receives security fixes.

## Reporting a Vulnerability

Please do not open a public issue for exploitable vulnerabilities.

If you find a security problem, create a private report through GitHub Security Advisories if available, or contact the maintainer through GitHub. Include:

- Affected component or endpoint
- Steps to reproduce
- Expected impact
- Suggested fix, if you have one

## Security Scope

Areas that deserve special care in this project include:

- Sa-Token login and role checks
- Admin-only endpoints under `/api/admin/**`
- User-scoped cart, order, address, and distribution endpoints
- WeChat Mini Program login credential handling
- Order status transitions and commission calculation
- Local configuration and secret management

## Secret Handling

Never commit real database passwords, WeChat Mini Program secrets, API keys, production tokens, or deployment credentials. Use `config/application-local.yml` or environment variables for local secrets.
