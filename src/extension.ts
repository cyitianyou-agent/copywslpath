import * as vscode from 'vscode';

import { resolveRealPath, toWslPath } from './pathConverter';

const cmdKey = 'copyWslPath.copyFromExplorer';
const cmdRealKey = 'copyWslPath.copyRealFromExplorer';
const cmdRawKey = 'copyWslPath.copyRawFromExplorer';
const cmdRealRawKey = 'copyWslPath.copyRealRawFromExplorer';
const showKey = 'copyWslPath.showNotification';
const noResMsg = 'No resource selected from Explorer context.';

/**
 * 读取用户配置：复制成功后是否展示通知。
 *
 * 设计意图：
 * - 默认返回 true，保证开箱即用时用户能立即看到复制结果。
 * - 将配置读取集中在单函数中，避免散落常量和重复 key。
 */
function readShow(): boolean {
  const cfgVal = vscode.workspace.getConfiguration();
  return cfgVal.get<boolean>(showKey, true);
}

/**
 * 从 Explorer 右键命令参数中提取本地文件路径。
 *
 * 边界约束：
 * - 当命令被异常触发且没有资源参数时，直接抛出可读错误，
 *   由上层统一弹窗。
 */
function pickPath(resource: vscode.Uri | undefined): string {
  if (!resource || !resource.fsPath) {
    throw new Error(noResMsg);
  }

  return resource.fsPath;
}

/**
 * 执行“复制 WSL 路径”主流程。
 *
 * 关键约束：
 * - `needReal` 为 true 时必须先解析真实路径，失败直接抛错，不回退。
 * - 复制成功后通知文案按命令语义区分，避免用户误判来源路径。
 */
async function copyPath(resource: vscode.Uri | undefined, needReal: boolean): Promise<void> {
  const rawVal = pickPath(resource);
  const pathVal = needReal ? resolveRealPath(rawVal) : rawVal;
  const wslVal = toWslPath(pathVal);

  await vscode.env.clipboard.writeText(wslVal);

  if (readShow()) {
    const tagVal = needReal ? 'Real WSL Path Copied' : 'WSL Path Copied';
    await vscode.window.showInformationMessage(`${tagVal}: ${wslVal}`);
  }
}

/**
 * 执行“复制原始系统路径”主流程。
 *
 * 关键约束：
 * - `needReal` 为 true 时先做真实路径解析，再写入剪贴板。
 * - 该流程不做 WSL 路径转换，直接保留当前操作系统原始路径格式。
 */
async function copyRawPath(resource: vscode.Uri | undefined, needReal: boolean): Promise<void> {
  const rawVal = pickPath(resource);
  const pathVal = needReal ? resolveRealPath(rawVal) : rawVal;

  await vscode.env.clipboard.writeText(pathVal);

  if (readShow()) {
    const tagVal = needReal ? 'Real Path Copied' : 'Path Copied';
    await vscode.window.showInformationMessage(`${tagVal}: ${pathVal}`);
  }
}

/**
 * 扩展激活入口。
 *
 * 关键步骤：
 * 1. 读取右键目标路径。
 * 2. 调用转换器生成 WSL 路径。
 * 3. 写入系统剪贴板。
 * 4. 按配置展示包含完整路径的成功提示。
 * 5. 任何异常均展示错误提示，保证用户可见。
 */
export function activate(context: vscode.ExtensionContext): void {
  const cmdRawVal = vscode.commands.registerCommand(cmdRawKey, async (resource: vscode.Uri | undefined) => {
    try {
      await copyRawPath(resource, false);
    } catch (error) {
      const messageVal = error instanceof Error ? error.message : String(error);
      await vscode.window.showErrorMessage(messageVal);
    }
  });

  const cmdRealRawVal = vscode.commands.registerCommand(cmdRealRawKey, async (resource: vscode.Uri | undefined) => {
    try {
      await copyRawPath(resource, true);
    } catch (error) {
      const messageVal = error instanceof Error ? error.message : String(error);
      await vscode.window.showErrorMessage(messageVal);
    }
  });

  const cmdVal = vscode.commands.registerCommand(cmdKey, async (resource: vscode.Uri | undefined) => {
    try {
      await copyPath(resource, false);
    } catch (error) {
      const messageVal = error instanceof Error ? error.message : String(error);
      await vscode.window.showErrorMessage(messageVal);
    }
  });

  const cmdRealVal = vscode.commands.registerCommand(cmdRealKey, async (resource: vscode.Uri | undefined) => {
    try {
      await copyPath(resource, true);
    } catch (error) {
      const messageVal = error instanceof Error ? error.message : String(error);
      await vscode.window.showErrorMessage(messageVal);
    }
  });

  context.subscriptions.push(cmdRawVal, cmdRealRawVal, cmdVal, cmdRealVal);
}

export function deactivate(): void {
  // 当前扩展无长期资源占用，这里保留空实现以对齐 VSCode 扩展生命周期约定。
}
