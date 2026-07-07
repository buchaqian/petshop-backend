# Contributing

Thank you for your interest in Petshop Backend. This project is being maintained as an open-source reference implementation for a small WeChat Mini Program commerce backend.

## What to Contribute

Good contribution areas include:

- Fix API bugs, authorization boundaries, or validation issues.
- Add unit tests, integration tests, or API tests.
- Improve README, deployment notes, and API documentation.
- Improve order, cart, address, distributor, commission, and admin statistics logic.
- Report security risks privately through the security process.

Please do not disclose exploitable vulnerability details in public issues.

## Development Setup

1. Fork and clone the repository.
2. Install Java 17 and Maven.
3. Copy local config:

```bash
cp config/application-local.example.yml config/application-local.yml
```

4. Fill in your local database and WeChat Mini Program credentials.
5. Initialize the database with `src/main/resources/sql/init.sql`.
6. Run the service:

```bash
mvn spring-boot:run
```

## Pull Request Checklist

- Keep secrets out of commits.
- Keep changes focused and explain the reason in the PR description.
- Update documentation when behavior, setup, or APIs change.
- Add or update tests when changing business logic.
- Make sure `mvn test` passes before requesting review.

## Commit Style

Use short, descriptive commit messages, for example:

```text
Fix order status validation
Add distributor application tests
Document local configuration
```
