import * as assert from 'node:assert/strict';
import { test } from 'node:test';

import { toWslPath } from './pathConverter';

test('converts uppercase drive path to wsl path', () => {
  const pathVal = 'E:\\MyWorks\\ibas_cloud\\ibas-typescript\\test\\apps';
  const wslVal = toWslPath(pathVal);

  assert.equal(wslVal, '/mnt/e/MyWorks/ibas_cloud/ibas-typescript/test/apps');
});

test('converts lowercase drive path to wsl path', () => {
  const pathVal = 'c:\\Users\\demo\\file.txt';
  const wslVal = toWslPath(pathVal);

  assert.equal(wslVal, '/mnt/c/Users/demo/file.txt');
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
