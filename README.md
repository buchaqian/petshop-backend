# Petshop Backend

[![CI](https://github.com/buchaqian/petshop-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/buchaqian/petshop-backend/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Petshop Backend 是一个宠物粮食销售微信小程序后端服务，基于 Spring Boot 3、MyBatis-Plus、MySQL 和 Sa-Token 构建。项目覆盖用户端购物流程、店主管理后台、订单管理、地址管理、分销商申请与佣金统计等核心接口。

## Project Status

这个仓库正在持续开源化和完善中。目前已经包含主要业务接口、数据库初始化脚本、本地配置隔离、开源协作文件和 GitHub Actions CI。后续重点包括补充自动化测试、完善部署说明、改进接口文档和加强安全检查。

## Features

- 微信小程序用户登录与用户信息管理
- 商品分类、商品列表、商品详情和规格数据
- 购物车增删改查
- 订单创建、订单列表、订单详情和取消订单
- 收货地址管理
- 分销商申请、佣金记录和佣金统计
- 店主管理端商品、订单、分销商审核接口
- 管理端经营数据概览、销售趋势和商品销量排行
- Sa-Token 登录态与角色权限控制
- Knife4j / OpenAPI 接口文档

## Tech Stack

- Java 17
- Spring Boot 3.3.4
- MyBatis-Plus 3.5.7
- MySQL
- Sa-Token 1.37.0
- Knife4j 4.4.0
- Lombok
- Hutool

## Project Structure

```text
src/main/java/com/petshop
├── common       # Common API response models and exception handling
├── config       # MyBatis-Plus, Sa-Token, Knife4j and metadata config
├── controller   # App and admin REST controllers
├── dto          # Request DTOs
├── entity       # Database entities
├── mapper       # MyBatis-Plus mapper interfaces
├── service      # Business services
└── vo           # Response view objects

src/main/resources
├── application.yml
└── sql
    ├── init.sql
    └── fix_comments.sql
```

## Configuration

Public configuration lives in `src/main/resources/application.yml`. Local secrets should be stored in `config/application-local.yml`, which is ignored by Git.

Create your local config from the example:

```bash
cp config/application-local.example.yml config/application-local.yml
```

Then fill in your own database password and WeChat Mini Program credentials.

The main environment variables are:

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

Create a MySQL database named `petshop`, then initialize it with:

```bash
mysql --default-character-set=utf8mb4 -u root -p petshop < src/main/resources/sql/init.sql
```

If table comments are displayed incorrectly in your database client, run:

```bash
mysql --default-character-set=utf8mb4 -u root -p petshop < src/main/resources/sql/fix_comments.sql
```

## Run Locally

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8089/api` by default.

Knife4j API documentation is available at:

```text
http://localhost:8089/api/doc.html
```

## Main API Groups

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

Do not commit real database passwords, WeChat Mini Program secrets, production tokens, or deployment credentials. Keep local secrets in ignored configuration files or environment variables.

Security-sensitive areas include login, admin authorization, user-scoped resources, order state transitions, commission logic, and local configuration handling. See [SECURITY.md](SECURITY.md) for reporting guidance.

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

Useful contribution areas:

- Add automated tests
- Improve API documentation
- Harden permission checks and parameter validation
- Improve deployment instructions
- Review order and distribution business logic

## License

This project is licensed under the [MIT License](LICENSE).
