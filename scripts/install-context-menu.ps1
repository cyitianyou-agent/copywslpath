[CmdletBinding()]
param(
  [switch]$allUsers,
  [bool]$showRaw = $true,
  [bool]$showReal = $true,
  [bool]$showWsl = $true,
  [bool]$showRealWsl = $true
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function New-CnText {
  param(
    [Parameter(Mandatory = $true)]
    [int[]]$codes
  )

  $chars = $codes | ForEach-Object { [char]$_ }
  return -join $chars
}

$menuRootText = New-CnText -codes @(0x590D, 0x5236, 0x8DEF, 0x5F84)
$menuRawText = $menuRootText
$menuRealText = New-CnText -codes @(0x590D, 0x5236, 0x771F, 0x5B9E, 0x8DEF, 0x5F84)
$menuWslText = New-CnText -codes @(0x590D, 0x5236, 0x0057, 0x0053, 0x004C, 0x8DEF, 0x5F84)
$menuRealWslText = New-CnText -codes @(0x590D, 0x5236, 0x771F, 0x5B9E, 0x0057, 0x0053, 0x004C, 0x8DEF, 0x5F84)

function Ensure-AdminIfNeeded {
  param(
    [Parameter(Mandatory = $true)]
    [bool]$needAdmin
  )

  if (-not $needAdmin) {
    return
  }

  $userVal = [Security.Principal.WindowsIdentity]::GetCurrent()
  $principalVal = New-Object Security.Principal.WindowsPrincipal($userVal)
  $adminVal = $principalVal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

  if (-not $adminVal) {
    throw 'Parameter -allUsers requires running PowerShell as Administrator.'
  }
}

function Ensure-Key {
  param(
    [Parameter(Mandatory = $true)]
    [string]$pathVal
  )

  if (-not (Test-Path -LiteralPath $pathVal)) {
    New-Item -Path $pathVal -Force | Out-Null
  }
}

function Set-KeyValue {
  param(
    [Parameter(Mandatory = $true)]
    [string]$pathVal,
    [Parameter(Mandatory = $true)]
    [string]$nameVal,
    [Parameter(Mandatory = $true)]
    [string]$valueVal
  )

  New-ItemProperty -Path $pathVal -Name $nameVal -Value $valueVal -PropertyType String -Force | Out-Null
}

function Set-DefaultValue {
  param(
    [Parameter(Mandatory = $true)]
    [string]$pathVal,
    [Parameter(Mandatory = $true)]
    [string]$valueVal
  )

  Set-Item -Path $pathVal -Value $valueVal
}

function Remove-KeyIfExists {
  param(
    [Parameter(Mandatory = $true)]
    [string]$pathVal
  )

  if (Test-Path -LiteralPath $pathVal) {
    Remove-Item -LiteralPath $pathVal -Recurse -Force
  }
}

function Build-CommandText {
  param(
    [Parameter(Mandatory = $true)]
    [string]$modeName,
    [Parameter(Mandatory = $true)]
    [string]$argToken
  )

  $scriptVal = Join-Path $PSScriptRoot 'copy-path.ps1'
  return "powershell.exe -NoProfile -ExecutionPolicy Bypass -File `"$scriptVal`" -mode $modeName -target `"$argToken`""
}

function Add-VerbItem {
  param(
    [Parameter(Mandatory = $true)]
    [string]$shellRoot,
    [Parameter(Mandatory = $true)]
    [string]$verbKey,
    [Parameter(Mandatory = $true)]
    [string]$menuName,
    [Parameter(Mandatory = $true)]
    [string]$modeName,
    [Parameter(Mandatory = $true)]
    [string]$argToken
  )

  $verbPath = Join-Path $shellRoot $verbKey
  $cmdPath = Join-Path $verbPath 'command'
  Ensure-Key -pathVal $verbPath
  Ensure-Key -pathVal $cmdPath
  Set-KeyValue -pathVal $verbPath -nameVal 'MUIVerb' -valueVal $menuName
  $cmdText = Build-CommandText -modeName $modeName -argToken $argToken
  Set-DefaultValue -pathVal $cmdPath -valueVal $cmdText
}

function Register-CascadeAtTarget {
  param(
    [Parameter(Mandatory = $true)]
    [string]$classRoot,
    [Parameter(Mandatory = $true)]
    [string]$targetPath,
    [Parameter(Mandatory = $true)]
    [string]$argToken
  )

  $menuPath = Join-Path $classRoot $targetPath
  Ensure-Key -pathVal $menuPath
  Set-KeyValue -pathVal $menuPath -nameVal 'MUIVerb' -valueVal $menuRootText
  Remove-KeyIfExists -pathVal (Join-Path $menuPath 'command')
  Remove-KeyIfExists -pathVal (Join-Path $menuPath 'shell')

  $extPath = Join-Path $menuPath 'ExtendedSubCommandsKey'
  $shellPath = Join-Path $extPath 'shell'
  Ensure-Key -pathVal $extPath
  Ensure-Key -pathVal $shellPath

  $addedVal = 0

  if ($showRaw) {
    Add-VerbItem -shellRoot $shellPath -verbKey 'copypath_raw' -menuName $menuRawText -modeName 'Raw' -argToken $argToken
    $addedVal += 1
  }

  if ($showReal) {
    Add-VerbItem -shellRoot $shellPath -verbKey 'copypath_real' -menuName $menuRealText -modeName 'Real' -argToken $argToken
    $addedVal += 1
  }

  if ($showWsl) {
    Add-VerbItem -shellRoot $shellPath -verbKey 'copypath_wsl' -menuName $menuWslText -modeName 'Wsl' -argToken $argToken
    $addedVal += 1
  }

  if ($showRealWsl) {
    Add-VerbItem -shellRoot $shellPath -verbKey 'copypath_realwsl' -menuName $menuRealWslText -modeName 'RealWsl' -argToken $argToken
    $addedVal += 1
  }

  if ($addedVal -eq 0) {
    throw 'At least one submenu must be enabled (showRaw/showReal/showWsl/showRealWsl).'
  }
}

Ensure-AdminIfNeeded -needAdmin $allUsers

$classRoot = if ($allUsers) { 'Registry::HKEY_LOCAL_MACHINE\SOFTWARE\Classes' } else { 'Registry::HKEY_CURRENT_USER\Software\Classes' }

Register-CascadeAtTarget -classRoot $classRoot -targetPath '*\shell\CopyPathMenu' -argToken '%1'
Register-CascadeAtTarget -classRoot $classRoot -targetPath 'Directory\shell\CopyPathMenu' -argToken '%1'
Register-CascadeAtTarget -classRoot $classRoot -targetPath 'Directory\Background\shell\CopyPathMenu' -argToken '%V'

Write-Host 'Windows context menu installation completed.'
