# VSCode 扩展设计：Explorer 右键复制 WSL 路径

- 日期：2026-04-08
- 状态：已评审（待实现）
- 主题：在 Windows 下使用 VSCode 时，为资源管理器中的文件/文件夹提供“复制 WSL 路径”右键能力

## 1. 目标与范围

### 1.1 目标

实现一个 VSCode 扩展，在资源管理器（Explorer）中文件或文件夹的右键菜单中新增菜单项：`Copy WSL Path`。

执行命令后：

1. 获取当前右键目标路径。
2. 转换为 WSL 路径格式。
3. 复制到系统剪贴板。
4. 按配置决定是否提示。

### 1.2 范围内

1. 仅支持 Explorer 右键入口。
2. 支持文件与文件夹。
3. 仅处理单个目标。
4. 提供可关闭的成功通知配置项。
5. 成功通知显示完整 WSL 路径。

### 1.3 范围外

1. 快捷键入口。
2. 命令面板独立入口。
3. 多选批量复制。
4. 编辑器标签页/空白区菜单入口。

## 2. 备选方案与取舍

### 2.1 方案 A（推荐）：TypeScript + 官方 VSCode Extension API

- 优点：类型安全、结构清晰、后续维护与发布标准化。
- 缺点：需要编译链与基础脚手架。

### 2.2 方案 B：纯 JavaScript 扩展

- 优点：上手快、配置简单。
- 缺点：缺少类型约束，长期维护成本更高。

### 2.3 方案 C：fork 现有扩展后改造

- 优点：短期代码量可能更小。
- 缺点：依赖上游结构，定制边界受限，后续升级与发布复杂。

### 2.4 结论

采用方案 A。理由：该需求虽小，但属于长期可复用工具，TypeScript 方案在可维护性和稳定性上更均衡。

## 3. 架构设计

### 3.1 组件划分

1. `extension` 激活模块：注册命令与生命周期管理。
2. `pathConverter` 转换模块：负责 Windows 路径到 WSL 路径的纯函数转换。
3. `config` 读取模块：读取 `copyWslPath.showNotification`。
4. `ui` 交互模块：成功/失败提示显示。

### 3.2 命令与菜单

- 命令 ID：`copyWslPath.copyFromExplorer`
- 菜单位置：`contributes.menus["explorer/context"]`
- 菜单文案：`Copy WSL Path`
- 显示策略：同时覆盖文件与文件夹场景。

### 3.3 数据流

1. 用户在 Explorer 对文件或文件夹右键点击菜单项。
2. VSCode 将当前资源 `Uri` 传给命令处理器。
3. 命令处理器提取本地路径。
4. 调用 `pathConverter` 执行转换。
5. 成功后写入剪贴板。
6. 读取配置并决定是否展示成功提示。
7. 失败时统一展示错误提示。

## 4. 路径转换规则

### 4.1 标准规则

输入格式（Windows）：`X:\a\b\c`

输出格式（WSL）：`/mnt/x/a/b/c`

转换步骤：

1. 校验是否为盘符绝对路径（`^[A-Za-z]:\\`）。
2. 盘符转小写。
3. `\\` 替换为 `/`。
4. 拼接 `/mnt/<drive>` 前缀。

### 4.2 示例

- 输入：`E:\MyWorks\ibas_cloud\ibas-typescript\test\apps`
- 输出：`/mnt/e/MyWorks/ibas_cloud/ibas-typescript/test/apps`

### 4.3 边界条件

1. `Uri` 为空或不可解析：报错并退出。
2. 非盘符绝对路径（如 UNC、相对路径）：报错并退出。
3. 文件与文件夹共用同一转换逻辑。
4. 不做额外编码转换，保持真实文件名字符。

## 5. 配置与交互

### 5.1 配置项

- 键名：`copyWslPath.showNotification`
- 类型：`boolean`
- 默认值：`true`
- 含义：复制成功后是否展示通知。

### 5.2 提示文案

成功提示（开启配置时）：

- `WSL Path Copied: <完整wsl路径>`

失败提示（始终展示）：

1. `No resource selected from Explorer context.`
2. `Only Windows drive paths like C:\\... are supported.`
3. `Failed to copy WSL path: <error>`

## 6. 测试策略

### 6.1 单元测试

围绕转换函数覆盖：

1. 正常盘符路径（大写盘符）。
2. 正常盘符路径（小写盘符）。
3. 非法输入（空字符串、相对路径、UNC）。

### 6.2 手工验证

1. 文件右键复制成功。
2. 文件夹右键复制成功。
3. 关闭通知配置后仅复制不弹成功提示。
4. 异常输入时展示预期错误信息。

### 6.3 构建验证

1. `npm run compile`
2. `npm run lint`（若脚手架包含）
3. Extension Development Host 中实测菜单与复制行为。

## 7. 约束与后续扩展

### 7.1 当前约束

1. 当前设计仅保证 Windows 盘符路径到 WSL 的映射。
2. 不支持批量复制。
3. 不扩展到其他上下文菜单。

### 7.2 可选扩展（后续）

1. 增加命令面板入口。
2. 增加多选批量复制（换行拼接）。
3. 增加“复制 Linux shell 转义路径”选项。

## 8. 实施验收标准

1. Explorer 右键文件可见并可执行 `Copy WSL Path`。
2. Explorer 右键文件夹可见并可执行 `Copy WSL Path`。
3. 剪贴板内容符合 `/mnt/<drive>/...` 规则。
4. `copyWslPath.showNotification` 默认为 `true` 且可关闭。
5. 成功提示展示完整 WSL 路径。
6. 错误场景有可理解提示，不发生未捕获异常。
