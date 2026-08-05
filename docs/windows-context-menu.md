# Windows 右键菜单方案（复制路径）

本文档说明如何在 Windows 资源管理器右键菜单中增加一级菜单 `复制路径`，并提供 4 个子菜单：

- `复制路径`
- `复制真实路径`
- `复制WSL路径`
- `复制真实WSL路径`

支持两个入口场景：

- 文件/文件夹被选中时的右键菜单
- 文件夹空白区域右键菜单

## 文件说明

- `scripts/install-context-menu.ps1`：安装菜单
- `scripts/uninstall-context-menu.ps1`：卸载菜单
- `scripts/copy-path.ps1`：具体复制逻辑

## 安装（当前用户，无需管理员）

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\install-context-menu.ps1
```

## 安装（所有用户，需要管理员）

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\install-context-menu.ps1 -allUsers
```

## 子菜单开关（默认全开）

安装脚本支持 4 个布尔参数。若未传入，默认全部显示。

- `-showRaw $true/$false`
- `-showReal $true/$false`
- `-showWsl $true/$false`
- `-showRealWsl $true/$false`

示例：只安装“复制路径”和“复制WSL路径”：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\install-context-menu.ps1 -showRaw $true -showReal $false -showWsl $true -showRealWsl $false
```

## 卸载（当前用户）

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\uninstall-context-menu.ps1
```

## 卸载（所有用户）

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\uninstall-context-menu.ps1 -allUsers
```

## 行为说明

- `复制路径`：复制右键目标原始路径
- `复制真实路径`：先解析真实物理路径再复制
- `复制WSL路径`：按 `X:\` -> `/mnt/x/` 规则转换后复制
- `复制真实WSL路径`：先解析真实路径，再转 WSL 路径

## 注意事项

- `复制WSL路径` 和 `复制真实WSL路径` 仅支持 Windows 盘符绝对路径。
- UNC 路径（如 `\\server\share\...`）会报错。
- 若执行策略限制脚本运行，请使用文档中的 `-ExecutionPolicy Bypass` 调用方式。
