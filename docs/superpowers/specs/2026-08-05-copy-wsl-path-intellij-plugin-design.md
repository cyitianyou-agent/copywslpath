# Copy WSL Path IntelliJ Plugin Design

## 1. Goal

在当前 `copywslpath` 仓库中新增独立的 `idea/` IntelliJ IDEA 插件工程，面向 IntelliJ IDEA 2025.1+，提供与现有 VS Code 扩展一致的四种路径复制能力：

- Copy Path
- Copy Real Path
- Copy WSL Path
- Copy Real WSL Path

插件仅支持 Windows 盘符绝对路径，例如 `E:\\Work\\demo`，并将 WSL 路径转换为 `/mnt/e/Work/demo`。

## 2. Scope and Compatibility

- 插件工程位于仓库根目录的 `idea/`，与现有 VS Code 工程相互独立。
- 使用 Kotlin、Gradle Kotlin DSL 和 IntelliJ Platform Gradle Plugin 2.x。
- 目标运行环境为 IntelliJ IDEA 2025.1+。
- 使用 Java 21 编译。
- 插件 ID 使用 `com.cyitianyou.copywslpath`。
- 不复用 PowerShell 脚本；IDEA 插件直接使用 JVM 和 IntelliJ Platform API。
- 不扩展 UNC 路径、相对路径、批量复制或 Linux 原生路径转换。

## 3. User-facing Behavior

### 3.1 Project View Actions

在 Project View 中选中文件或目录后，右键菜单显示以下动作：

1. `Copy Path`：复制当前资源的原始 Windows 路径。
2. `Copy Real Path`：解析软链接或重解析点后，复制真实 Windows 路径。
3. `Copy WSL Path`：将当前 Windows 路径转换为 WSL 路径后复制。
4. `Copy Real WSL Path`：先解析真实路径，再转换为 WSL 路径后复制。

四种动作都只处理当前选中的单个资源。未选中资源、路径格式不支持、真实路径解析失败或剪贴板写入失败时，显示错误提示，不写入错误结果。

### 3.2 Settings

在 `Settings | Tools | Copy WSL Path` 中提供以下应用级设置，默认值全部为 `true`：

- `showNotification`：复制成功后是否显示包含完整结果路径的通知。
- `showCopyPathMenu`：是否显示 `Copy Path`。
- `showCopyRealPathMenu`：是否显示 `Copy Real Path`。
- `showCopyWslPathMenu`：是否显示 `Copy WSL Path`。
- `showCopyRealWslPathMenu`：是否显示 `Copy Real WSL Path`。

菜单动作在 `update()` 阶段根据设置更新可见性。设置改变后，下一次打开 Project View 菜单时生效。

## 4. Architecture

### 4.1 Gradle Project

`idea/` 将包含标准 IntelliJ Platform 插件工程文件：

- `build.gradle.kts`：Kotlin、IntelliJ Platform、测试和打包配置。
- `settings.gradle.kts`：项目名称和插件仓库配置。
- `gradle.properties`：JVM、Kotlin 和 IntelliJ Platform 构建属性。
- `src/main/resources/META-INF/plugin.xml`：插件元数据、Action、设置和通知组注册。
- `src/main/kotlin/com/cyitianyou/copywslpath/`：Kotlin 实现。
- `src/test/kotlin/com/cyitianyou/copywslpath/`：纯逻辑单元测试。

目标 IntelliJ 平台使用 2025.1 系列版本编译，插件最低兼容构建号为 `251`。不设置过早的最高版本限制，以允许后续 2025.1+ 更新版本安装；构建验证以目标平台提供的插件验证任务为准。

### 4.2 Core Logic

- `PathConverter` 是无状态纯逻辑组件，负责校验盘符绝对路径、统一盘符大小写、替换分隔符并拼接 `/mnt/<drive>/`。
- `PathResolver` 负责将 `VirtualFile` 路径转换为 `Path` 并调用 `toRealPath()`，失败时包装为用户可读异常。
- `CopyPathAction` 负责公共的资源提取、路径选择、剪贴板写入、通知和错误处理流程。
- 四个具体 Action 仅声明复制模式，避免四份流程代码发生行为漂移。

### 4.3 IntelliJ Integration

- 具体 Action 注册到 `ProjectViewPopupMenu`。
- Action 通过 `CommonDataKeys.VIRTUAL_FILE` 读取当前文件或目录。
- 剪贴板使用 IntelliJ `CopyPasteManager` 和 `StringSelection`。
- 成功消息使用已注册的 IntelliJ 通知组。
- 失败消息使用 IntelliJ 错误对话框或错误通知，确保异常不会静默丢失。
- 设置使用应用级持久化服务和 `Configurable` 设置页面。

## 5. Path Rules

输入必须匹配 Windows 盘符绝对路径格式 `^[A-Za-z]:\\(.*)$`。

转换规则：

1. 去除输入首尾空白。
2. 盘符转为小写。
3. 将反斜杠替换为正斜杠。
4. 输出 `/mnt/<lowercase-drive>/<path-tail>`。
5. 根路径 `C:\\` 按当前 VS Code 转换器的行为输出 `/mnt/c/`。

UNC 路径、相对路径、空路径和非 Windows 盘符路径统一拒绝。

## 6. Testing Strategy

采用测试先行方式实现 `PathConverter` 和 `PathResolver` 的纯逻辑行为，至少覆盖：

- 大写盘符路径转换。
- 小写盘符路径转换。
- Windows 根路径转换。
- 多级目录和文件路径转换。
- 空字符串拒绝。
- UNC 路径拒绝。
- 相对路径拒绝。
- 真实路径解析成功。
- 真实路径解析失败并保留可读错误原因。

构建验证包括：

```bash
cd idea
./gradlew test
./gradlew buildPlugin
./gradlew verifyPlugin
```

Action 注册、菜单显示、设置页面和系统剪贴板行为属于 IntelliJ 平台集成层，第一版通过插件编译验证和明确的代码边界覆盖；不引入重量级 IDE UI 自动化测试。

## 7. Error Handling

- 没有 Project View 资源时显示 `No resource selected from Project View.`。
- 不支持路径时显示明确的 Windows 盘符路径错误。
- 真实路径解析失败时显示 `Failed to resolve real path: ...`。
- 剪贴板操作失败时显示平台异常消息。
- 任意失败都不会回退到原始路径，避免用户误复制非预期结果。

## 8. Acceptance Criteria

1. `idea/` 可以独立执行 Gradle 测试、插件打包和插件验证。
2. IDEA 2025.1+ 的 Project View 文件和目录右键菜单可显示四个动作。
3. 四个动作的复制结果与现有 VS Code 扩展逻辑一致。
4. 软链接或重解析点可通过 Real 类动作解析到真实路径。
5. 设置页可以独立控制成功通知和四个菜单项的显示。
6. 不支持的路径和解析失败会显示错误，不会静默回退。
