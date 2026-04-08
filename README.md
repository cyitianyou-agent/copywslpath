# Copy WSL Path

Right-click a file or folder in Explorer and select `Copy WSL Path`.

## Configuration

- `copyWslPath.showNotification` (default: `true`)

## Manual Validation

1. Press `F5` in extension project.
2. In Extension Development Host, right-click a file in Explorer.
3. Click `Copy WSL Path`.
4. Paste clipboard and verify `/mnt/<drive>/...` format.
5. Set `copyWslPath.showNotification` to `false` and repeat to verify no success popup.
