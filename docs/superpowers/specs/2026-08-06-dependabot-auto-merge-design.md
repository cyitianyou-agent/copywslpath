# Dependabot 自动合并设计

## 目标

让 GitHub Dependabot 的补丁版本更新在项目测试通过后自动合并，同时保留 minor、major 和无法通过测试的更新供人工审核。

## 方案

- `.github/dependabot.yml` 监控根目录 npm、`idea/` Gradle 和 GitHub Actions 依赖。
- `.github/workflows/ci.yml` 在 Pull Request 上运行 VS Code 扩展测试和 IDEA 插件测试。
- `.github/workflows/dependabot-auto-merge.yml` 使用只读元数据查询识别 Dependabot PR。
- 仅当 Dependabot 输出 `version-update:semver-patch`，且 npm、IDEA 两个 CI 检查均成功后，才启用 squash auto-merge。

自动合并工作流使用 `pull_request_target`，不 checkout 或执行 PR 分支代码；它只读取检查状态并调用 GitHub CLI 设置合并。这样写权限不会暴露给依赖更新分支中的脚本。

## 边界

- minor、major 更新不会自动合并。
- 测试失败、检查超时或检查缺失时不会合并。
- 自动合并只处理作者为 `dependabot[bot]` 且目标仓库为当前项目的 PR。
