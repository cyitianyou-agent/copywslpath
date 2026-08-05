# Copy WSL Path for IntelliJ IDEA

这是 `copy-wsl-path` 的 IntelliJ IDEA 插件实现，支持 IntelliJ IDEA 2025.1 及以上版本。

## 功能

在 Project View 中右键文件或文件夹，进入 `Copy Path As` 二级菜单，可以使用：

- `Copy Path`：复制当前 Windows 原始路径。
- `Copy Real Path`：解析软链接或重解析点后复制真实路径。
- `Copy WSL Path`：将 Windows 盘符路径转换为 WSL 路径。
- `Copy Real WSL Path`：解析真实路径后再转换为 WSL 路径。

WSL 转换示例：

```text
E:\\MyWorks\\demo -> /mnt/e/MyWorks/demo
```

## 设置

打开 `Settings | Tools | Copy WSL Path`，可以分别控制：

- 是否显示复制成功通知。
- 是否显示四个右键菜单项。

所有开关默认开启。

## 构建环境

- Java 21
- Gradle Wrapper 8.13
- IntelliJ Platform Gradle Plugin 2.x
- 目标平台 IntelliJ IDEA 2025.2.6.1
- 最低兼容构建号 `251`（IntelliJ IDEA 2025.1）

## 构建命令

在 `idea/` 目录执行：

```bash
./gradlew test
./gradlew buildPlugin
./gradlew verifyPlugin
```

插件包输出到：

```text
build/distributions/copy-wsl-path-idea-0.1.0.zip
```

## 限制

- 仅支持 Windows 盘符绝对路径，例如 `C:\\Users\\demo`。
- 不支持 UNC 路径、相对路径和 Linux 原生路径。
- `Real` 类操作要求目标路径存在，并且真实路径解析失败时不会回退到原始路径。
