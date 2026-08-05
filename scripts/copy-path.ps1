[CmdletBinding()]
param(
  [Parameter(Mandatory = $true)]
  [ValidateSet('Raw', 'Real', 'Wsl', 'RealWsl')]
  [string]$mode,

  [Parameter(Mandatory = $true)]
  [string]$target
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

function Show-ErrorDialog {
  param(
    [Parameter(Mandatory = $true)]
    [string]$message
  )

  Add-Type -AssemblyName System.Windows.Forms
  [System.Windows.Forms.MessageBox]::Show(
    $message,
    (New-CnText -codes @(0x590D, 0x5236, 0x8DEF, 0x5F84, 0x5931, 0x8D25)),
    [System.Windows.Forms.MessageBoxButtons]::OK,
    [System.Windows.Forms.MessageBoxIcon]::Error
  ) | Out-Null
}

function Convert-ToWslPath {
  param(
    [Parameter(Mandatory = $true)]
    [string]$winPath
  )

  if ([string]::IsNullOrWhiteSpace($winPath)) {
    throw 'The path is empty and cannot be converted to a WSL path.'
  }

  if ($winPath.StartsWith('\\')) {
    throw "UNC path is not supported: $winPath"
  }

  $matchVal = [System.Text.RegularExpressions.Regex]::Match(
    $winPath,
    '^(?<drive>[A-Za-z]):\\(?<rest>.*)$'
  )

  if (-not $matchVal.Success) {
    throw "Only Windows drive absolute paths are supported: $winPath"
  }

  $driveVal = $matchVal.Groups['drive'].Value.ToLowerInvariant()
  $restVal = $matchVal.Groups['rest'].Value -replace '\\', '/'

  if ([string]::IsNullOrEmpty($restVal)) {
    return "/mnt/$driveVal"
  }

  return "/mnt/$driveVal/$restVal"
}

function Resolve-RealPathValue {
  param(
    [Parameter(Mandatory = $true)]
    [string]$pathVal
  )

  $itemVal = Get-Item -LiteralPath $pathVal -ErrorAction Stop
  $resolvedVal = Resolve-Path -LiteralPath $itemVal.FullName -ErrorAction Stop
  return $resolvedVal.ProviderPath
}

try {
  $rawVal = $target
  $realVal = $null

  if ($mode -eq 'Raw') {
    Set-Clipboard -Value $rawVal
    exit 0
  }

  if ($mode -eq 'Real') {
    $realVal = Resolve-RealPathValue -pathVal $rawVal
    Set-Clipboard -Value $realVal
    exit 0
  }

  if ($mode -eq 'Wsl') {
    $wslVal = Convert-ToWslPath -winPath $rawVal
    Set-Clipboard -Value $wslVal
    exit 0
  }

  if ($mode -eq 'RealWsl') {
    $realVal = Resolve-RealPathValue -pathVal $rawVal
    $wslVal = Convert-ToWslPath -winPath $realVal
    Set-Clipboard -Value $wslVal
    exit 0
  }

  throw "Unknown mode: $mode"
} catch {
  $messageVal = $_.Exception.Message
  Show-ErrorDialog -message $messageVal
  Write-Error $messageVal
  exit 1
}
