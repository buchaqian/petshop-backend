# Contributing

感谢你关注 Petshop Backend。这个项目目前由维护者持续整理和开源化，欢迎通过 issue 或 pull request 参与改进。

## What to Contribute

- 修复接口 bug、权限边界问题或参数校验问题
- 补充单元测试、集成测试或接口测试
- 改进 README、部署说明和 API 文档
- 优化订单、购物车、地址、分销佣金和管理端统计逻辑
- 报告安全风险，但请不要在公开 issue 中披露可利用细节

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
