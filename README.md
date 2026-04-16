# Copy WSL Path

`Copy WSL Path` 是一个 VSCode 扩展，用于在资源管理器（Explorer）中右键复制文件或文件夹的 WSL 路径。

## 功能说明

- `Copy WSL Path`  
  将当前资源路径直接转换为 WSL 路径并复制到剪贴板。  
  示例：`E:\MyWorks\demo` -> `/mnt/e/MyWorks/demo`

- `Copy Real WSL Path`  
  先解析软链接/重解析点对应的真实物理路径，再转换为 WSL 路径并复制。  
  适合多仓库软链接开发场景。若真实路径解析失败，会直接报错，不回退到原始路径。

## 适用范围与限制

- 仅支持 Windows 盘符绝对路径（如 `C:\...`、`E:\...`）。
- 不支持 UNC 路径（如 `\\server\share\...`）及非盘符绝对路径。

## 配置项

- `copyWslPath.showNotification`（默认：`true`）  
  控制复制成功后是否弹出通知。

## 本地调试

1. 在扩展项目中按 `F5` 启动 Extension Development Host。
2. 在新窗口的 Explorer 中右键文件或文件夹。
3. 选择 `Copy WSL Path` 或 `Copy Real WSL Path`。
4. 粘贴剪贴板并检查是否为 `/mnt/<drive>/...` 格式。

## 打包

```bash
npm install
npm run package
```

执行后会在项目根目录生成 `copy-wsl-path-<version>.vsix`。

## 安装 VSIX

```bash
code --install-extension copy-wsl-path-<version>.vsix
```
