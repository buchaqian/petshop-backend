# Petshop Backend

[![CI](https://github.com/buchaqian/petshop-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/buchaqian/petshop-backend/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Petshop Backend is a Spring Boot backend for a WeChat Mini Program pet food shop. It provides app-side shopping APIs, admin-side store management APIs, order handling, address management, distributor applications, commission records, and basic business statistics.

## Project Status

This repository is being prepared as an open-source reference implementation for small WeChat Mini Program commerce backends. The current codebase includes the main REST API modules, MySQL initialization scripts, local secret isolation, project governance files, GitHub issue/PR templates, and GitHub Actions CI.

Current focus areas:

- Add unit and integration tests for core services.
- Improve deployment documentation.
- Harden authorization checks and input validation.
- Document API behavior and business state transitions.
- Review order and distributor commission security boundaries.

## Features

- WeChat Mini Program login flow.
- Product categories, product listing, product details, and product specs.
- Shopping cart APIs.
- Order creation, listing, detail lookup, cancellation, and admin delivery flow.
- Address management APIs.
- Distributor application, commission records, and commission statistics.
- Admin product, order, distributor, and statistics APIs.
- Sa-Token based authentication and role checks.
- Knife4j / OpenAPI documentation support.

## Tech Stack

- Java 17
- Spring Boot 3.3.4
- MyBatis-Plus 3.5.7
- MySQL
- Sa-Token 1.37.0
- Knife4j 4.4.0
- Lombok
- Hutool

## Repository Structure

```text
src/main/java/com/petshop
|-- common       Common API response models and exception handling
|-- config       MyBatis-Plus, Sa-Token, Knife4j, and metadata config
|-- controller   App and admin REST controllers
|-- dto          Request DTOs
|-- entity       Database entities
|-- mapper       MyBatis-Plus mapper interfaces
|-- service      Business services
`-- vo           Response view objects

src/main/resources
|-- application.yml
`-- sql
    |-- init.sql
    `-- fix_comments.sql
```

## Documentation

- [API overview](docs/API_OVERVIEW.md)
- [Security checklist](docs/SECURITY_CHECKLIST.md)
- [Maintenance roadmap](docs/ROADMAP.md)
- [Contributing guide](CONTRIBUTING.md)
- [Security policy](SECURITY.md)

## Configuration

Public configuration lives in `src/main/resources/application.yml`. Local secrets should be stored in `config/application-local.yml`, which is ignored by Git.

Create local configuration from the example:

```bash
cp config/application-local.example.yml config/application-local.yml
```

Then fill in your own database password and WeChat Mini Program credentials.

Main environment variables:

```text
SERVER_PORT
DB_URL
DB_USERNAME
DB_PASSWORD
WX_MINIAPP_APPID
WX_MINIAPP_SECRET
SA_TOKEN_TIMEOUT
```

## Database

Create a MySQL database named `petshop`, then initialize it:

```bash
mysql --default-character-set=utf8mb4 -u root -p petshop < src/main/resources/sql/init.sql
```

If table comments are displayed incorrectly in your database client, run:

```bash
mysql --default-character-set=utf8mb4 -u root -p petshop < src/main/resources/sql/fix_comments.sql
```

For existing databases, apply incremental scripts from `src/main/resources/sql/migrations` in filename order.

## Run Locally

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8089/api` by default.

Knife4j API documentation is available at:

```text
http://localhost:8089/api/doc.html
```

## API Groups

- `/api/app/user/**` user login and profile APIs
- `/api/app/product/**` public product browsing APIs
- `/api/app/cart/**` shopping cart APIs
- `/api/app/order/**` order APIs
- `/api/app/address/**` address APIs
- `/api/app/distribution/**` distributor and commission APIs
- `/api/admin/product/**` admin product management APIs
- `/api/admin/order/**` admin order management APIs
- `/api/admin/distributor/**` admin distributor review APIs
- `/api/admin/statistics/**` admin statistics APIs

## Security

Do not commit real database passwords, WeChat Mini Program secrets, production tokens, API keys, or deployment credentials. Keep local secrets in ignored configuration files or environment variables.

Security-sensitive areas include login, admin authorization, user-scoped resources, order state transitions, commission logic, and local configuration handling. See [SECURITY.md](SECURITY.md) for reporting guidance.

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

Useful contribution areas:

- Add automated tests.
- Improve API documentation.
- Harden permission checks and parameter validation.
- Improve deployment instructions.
- Review order and distribution business logic.

## License

This project is licensed under the [MIT License](LICENSE).
