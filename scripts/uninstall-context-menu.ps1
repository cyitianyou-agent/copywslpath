[CmdletBinding()]
param(
  [switch]$allUsers
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

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

function Remove-KeyIfExists {
  param(
    [Parameter(Mandatory = $true)]
    [string]$pathVal
  )

  if (Test-Path -LiteralPath $pathVal) {
    Remove-Item -LiteralPath $pathVal -Recurse -Force
  }
}

Ensure-AdminIfNeeded -needAdmin $allUsers

$classRoot = if ($allUsers) { 'Registry::HKEY_LOCAL_MACHINE\SOFTWARE\Classes' } else { 'Registry::HKEY_CURRENT_USER\Software\Classes' }
$cmdRoot = if ($allUsers) {
  'Registry::HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Explorer\CommandStore\shell'
} else {
  'Registry::HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Explorer\CommandStore\shell'
}

Remove-KeyIfExists -pathVal (Join-Path $classRoot '*\shell\CopyPathMenu')
Remove-KeyIfExists -pathVal (Join-Path $classRoot 'Directory\shell\CopyPathMenu')
Remove-KeyIfExists -pathVal (Join-Path $classRoot 'Directory\Background\shell\CopyPathMenu')
Remove-KeyIfExists -pathVal (Join-Path $classRoot 'CopyPathMenu')

Remove-KeyIfExists -pathVal (Join-Path $cmdRoot 'CopyPath.Raw')
Remove-KeyIfExists -pathVal (Join-Path $cmdRoot 'CopyPath.Real')
Remove-KeyIfExists -pathVal (Join-Path $cmdRoot 'CopyPath.Wsl')
Remove-KeyIfExists -pathVal (Join-Path $cmdRoot 'CopyPath.RealWsl')
Remove-KeyIfExists -pathVal (Join-Path $cmdRoot 'CopyPath.BgRaw')
Remove-KeyIfExists -pathVal (Join-Path $cmdRoot 'CopyPath.BgReal')
Remove-KeyIfExists -pathVal (Join-Path $cmdRoot 'CopyPath.BgWsl')
Remove-KeyIfExists -pathVal (Join-Path $cmdRoot 'CopyPath.BgRealWsl')
Remove-KeyIfExists -pathVal (Join-Path $cmdRoot 'CopyPathMenu')

Write-Host 'Windows context menu uninstallation completed.'
