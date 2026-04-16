import * as fs from 'node:fs';

const drivePathReg = /^([A-Za-z]):\\(.*)$/;

/**
 * 将 Windows 盘符路径转换为 WSL 路径。
 *
 * 设计意图：
 * 1. 仅接受最明确、最常见的 `X:\...` 绝对路径，避免对 UNC 或相对路径做隐式推断。
 * 2. 盘符统一小写，保证输出稳定，例如 `E:` 与 `e:` 都映射到 `/mnt/e`。
 * 3. 只做分隔符替换，不改动业务路径字符，避免引入额外编码副作用。
 *
 * 边界约束：
 * - 输入为空、非盘符绝对路径时，抛出统一错误，交由调用方展示友好提示。
 */
export function toWslPath(winPath: string): string {
  const pathVal = winPath.trim();
  const matchVal = drivePathReg.exec(pathVal);

  if (!matchVal) {
    throw new Error('Only Windows drive paths like C:\\... are supported.');
  }

  const driveVal = matchVal[1].toLowerCase();
  const tailVal = matchVal[2].replace(/\\/g, '/');
  return `/mnt/${driveVal}/${tailVal}`;
}

/**
 * 解析 Windows 路径对应的真实路径（跟随软链接/重解析点）。
 *
 * 设计意图：
 * 1. 让“复制真实 WSL 路径”命令在多仓库软链接场景中拿到目标物理路径。
 * 2. 失败时明确抛错，不做回退，避免复制到非预期路径。
 *
 * 边界约束：
 * - 当解析失败时，统一转换为可读错误文案，交给上层直接提示用户。
 * - 默认使用 `fs.realpathSync.native`，并允许注入实现以便测试。
 */
export function resolveRealPath(
  winPath: string,
  readPath: (pathVal: string) => string = fs.realpathSync.native
): string {
  const pathVal = winPath.trim();

  try {
    return readPath(pathVal);
  } catch (error) {
    const msgVal = error instanceof Error ? error.message : String(error);
    throw new Error(`Failed to resolve real path: ${msgVal}`);
  }
}
