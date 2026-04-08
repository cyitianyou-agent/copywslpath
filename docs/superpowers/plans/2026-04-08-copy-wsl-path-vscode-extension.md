# Copy WSL Path VSCode Extension Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a VSCode extension that adds an Explorer context-menu action to copy the selected file/folder path as WSL style (`/mnt/<drive>/...`), with configurable success notification.

**Architecture:** The extension has a small command handler in `src/extension.ts` and a pure path converter in `src/pathConverter.ts`. The command validates Explorer URI input, converts Windows paths to WSL, writes to clipboard, and conditionally shows a full-path notification based on workspace configuration. Unit tests target the converter logic for edge cases.

**Tech Stack:** TypeScript, VSCode Extension API, Node.js built-in test runner (`node:test`), npm scripts.

---

## File Structure

- Create: `package.json`
- Create: `tsconfig.json`
- Create: `.vscodeignore`
- Create: `.gitignore`
- Create: `README.md`
- Create: `src/extension.ts`
- Create: `src/pathConverter.ts`
- Create: `src/pathConverter.test.ts`
- Create: `src/types.d.ts`

### Task 1: Bootstrap Extension Project Skeleton

**Files:**
- Create: `package.json`
- Create: `tsconfig.json`
- Create: `.vscodeignore`
- Create: `.gitignore`
- Create: `README.md`

- [ ] **Step 1: Create extension manifest and scripts**

```json
{
  "name": "copy-wsl-path",
  "displayName": "Copy WSL Path",
  "description": "Copy Explorer file/folder path as WSL path",
  "version": "0.0.1",
  "publisher": "local",
  "engines": {
    "vscode": "^1.90.0"
  },
  "categories": [
    "Other"
  ],
  "main": "./out/extension.js",
  "activationEvents": [
    "onCommand:copyWslPath.copyFromExplorer"
  ],
  "contributes": {
    "commands": [
      {
        "command": "copyWslPath.copyFromExplorer",
        "title": "Copy WSL Path"
      }
    ],
    "menus": {
      "explorer/context": [
        {
          "command": "copyWslPath.copyFromExplorer",
          "group": "navigation@50",
          "when": "explorerResourceIsFolder"
        },
        {
          "command": "copyWslPath.copyFromExplorer",
          "group": "navigation@50",
          "when": "!explorerResourceIsFolder"
        }
      ]
    },
    "configuration": {
      "title": "Copy WSL Path",
      "properties": {
        "copyWslPath.showNotification": {
          "type": "boolean",
          "default": true,
          "description": "Show notification with full copied WSL path after success."
        }
      }
    }
  },
  "scripts": {
    "compile": "tsc -p ./",
    "test": "npm run compile && node --test out/pathConverter.test.js"
  },
  "devDependencies": {
    "@types/node": "^22.10.2",
    "@types/vscode": "^1.90.0",
    "typescript": "^5.7.2"
  }
}
```

- [ ] **Step 2: Add TypeScript config and ignore files**

```json
{
  "compilerOptions": {
    "module": "commonjs",
    "target": "es2022",
    "outDir": "out",
    "lib": ["es2022"],
    "sourceMap": true,
    "rootDir": "src",
    "strict": true,
    "skipLibCheck": true,
    "types": ["node", "vscode"]
  },
  "include": ["src"]
}
```

```gitignore
node_modules/
out/
.vscode-test/
*.vsix
```

```text
.vscodeignore
src/**
**/*.ts
tsconfig.json
```

- [ ] **Step 3: Add README baseline usage docs**

```markdown
# Copy WSL Path

Right-click a file or folder in Explorer and select `Copy WSL Path`.

## Configuration

- `copyWslPath.showNotification` (default: `true`)
```

- [ ] **Step 4: Validate package manifest syntax**

Run: `npm run compile`
Expected: TypeScript compile attempts to run and reports missing source files until Task 2 creates them.

- [ ] **Step 5: Commit bootstrap files**

```bash
git add package.json tsconfig.json .gitignore .vscodeignore README.md
git commit -m "chore: bootstrap copy-wsl-path extension skeleton"
```

### Task 2: Implement and Test Path Converter via TDD

**Files:**
- Create: `src/pathConverter.ts`
- Create: `src/pathConverter.test.ts`

- [ ] **Step 1: Write failing tests for conversion and invalid input**

```ts
import test from 'node:test';
import assert from 'node:assert/strict';
import { toWslPath } from './pathConverter';

test('converts uppercase drive path to wsl path', () => {
  const actual = toWslPath('E:\\MyWorks\\ibas_cloud\\test\\apps');
  assert.equal(actual, '/mnt/e/MyWorks/ibas_cloud/test/apps');
});

test('converts lowercase drive path to wsl path', () => {
  const actual = toWslPath('c:\\Users\\demo\\file.txt');
  assert.equal(actual, '/mnt/c/Users/demo/file.txt');
});

test('throws for non-drive absolute path', () => {
  assert.throws(() => {
    toWslPath('\\\\server\\share\\path');
  }, /Only Windows drive paths/);
});

test('throws for empty input', () => {
  assert.throws(() => {
    toWslPath('');
  }, /Only Windows drive paths/);
});
```

- [ ] **Step 2: Run tests to verify failure**

Run: `npm run test`
Expected: FAIL with module not found or `toWslPath` missing.

- [ ] **Step 3: Implement minimal converter**

```ts
const winPathReg = /^([A-Za-z]):\\(.*)$/;

export function toWslPath(winPath: string): string {
  const pathVal = winPath.trim();
  const matchVal = winPathReg.exec(pathVal);

  if (!matchVal) {
    throw new Error('Only Windows drive paths like C:\\... are supported.');
  }

  const driveVal = matchVal[1].toLowerCase();
  const tailVal = matchVal[2].replace(/\\/g, '/');
  return `/mnt/${driveVal}/${tailVal}`;
}
```

- [ ] **Step 4: Run tests to verify pass**

Run: `npm run test`
Expected: PASS all converter tests.

- [ ] **Step 5: Commit converter and tests**

```bash
git add src/pathConverter.ts src/pathConverter.test.ts
git commit -m "test+feat: add windows to wsl path converter"
```

### Task 3: Implement Explorer Command and Notification Config

**Files:**
- Create: `src/extension.ts`
- Create: `src/types.d.ts`
- Modify: `src/pathConverter.ts`

- [ ] **Step 1: Write compile-level contract for command flow**

```ts
import * as vscode from 'vscode';
import { toWslPath } from './pathConverter';

export function activate(context: vscode.ExtensionContext): void {
  const cmd = vscode.commands.registerCommand('copyWslPath.copyFromExplorer', async (resource: vscode.Uri | undefined) => {
    // Command body implemented in next plan step.
  });
  context.subscriptions.push(cmd);
}

export function deactivate(): void {}
```

- [ ] **Step 2: Implement command logic with validation, clipboard, and notification toggle**

```ts
const settingKey = 'copyWslPath.showNotification';

function readShowNotification(): boolean {
  const cfg = vscode.workspace.getConfiguration();
  return cfg.get<boolean>(settingKey, true);
}

function pickFsPath(resource: vscode.Uri | undefined): string {
  if (!resource || !resource.fsPath) {
    throw new Error('No resource selected from Explorer context.');
  }
  return resource.fsPath;
}

export function activate(context: vscode.ExtensionContext): void {
  const cmd = vscode.commands.registerCommand('copyWslPath.copyFromExplorer', async (resource: vscode.Uri | undefined) => {
    try {
      const fsPathVal = pickFsPath(resource);
      const wslPathVal = toWslPath(fsPathVal);
      await vscode.env.clipboard.writeText(wslPathVal);

      if (readShowNotification()) {
        await vscode.window.showInformationMessage(`WSL Path Copied: ${wslPathVal}`);
      }
    } catch (error) {
      const msgVal = error instanceof Error ? error.message : String(error);
      await vscode.window.showErrorMessage(`Failed to copy WSL path: ${msgVal}`);
    }
  });

  context.subscriptions.push(cmd);
}
```

- [ ] **Step 3: Ensure extension compile types are stable**

```ts
// src/types.d.ts
// Keep this file for future VSCode API type augmentation.
export {};
```

- [ ] **Step 4: Run compile and tests**

Run: `npm run compile && npm run test`
Expected: compile succeeds and converter tests remain PASS.

- [ ] **Step 5: Commit command implementation**

```bash
git add src/extension.ts src/types.d.ts src/pathConverter.ts
git commit -m "feat: add explorer command to copy wsl path"
```

### Task 4: Manual Validation Checklist and Final Polish

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Add manual test section to README**

```markdown
## Manual Validation

1. Press `F5` in extension project.
2. In Extension Development Host, right-click a file in Explorer.
3. Click `Copy WSL Path`.
4. Paste clipboard and verify `/mnt/<drive>/...` format.
5. Set `copyWslPath.showNotification` to `false` and repeat to verify no success popup.
```

- [ ] **Step 2: Run final verification command set**

Run: `npm run compile && npm run test`
Expected: all PASS.

- [ ] **Step 3: Commit docs polish**

```bash
git add README.md
git commit -m "docs: add manual validation guide"
```
