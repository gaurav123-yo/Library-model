# Smart Library System Automator Script
# This script will check for Maven, download it if missing, and then compile and launch the project.

$ErrorActionPreference = "Stop"

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "  SMART LIBRARY SYSTEM BOOTSTRAP & LAUNCHER   " -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan

# 1. Verify Java is installed
if (Get-Command "java" -ErrorAction SilentlyContinue) {
    Write-Host "Java detected successfully." -ForegroundColor Green
} else {
    Write-Error "Java is not installed or not in system PATH. Please install Java 17+ and try again."
    Exit 1
}


# 2. Check for Maven
$mvnCmd = "mvn"
$localMvnDir = Join-Path $PSScriptRoot ".maven"
$localMvnZip = Join-Path $localMvnDir "maven.zip"
$localMvnBin = Join-Path $localMvnDir "apache-maven-3.9.6\bin\mvn.cmd"

if (Get-Command "mvn" -ErrorAction SilentlyContinue) {
    Write-Host "System Maven detected." -ForegroundColor Green
    $mvnCmd = "mvn"
} elseif (Test-Path $localMvnBin) {
    Write-Host "Local Maven installation detected." -ForegroundColor Green
    $mvnCmd = $localMvnBin
} else {
    Write-Host "Maven was not found. Downloading portable Apache Maven 3.9.6..." -ForegroundColor Yellow
    
    if (!(Test-Path $localMvnDir)) {
        New-Item -ItemType Directory -Force -Path $localMvnDir | Out-Null
    }

    $downloadUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
    Write-Host "Downloading from: $downloadUrl" -ForegroundColor Gray
    
    # Download zip using System.Net.WebClient or Invoke-WebRequest
    try {
        [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $downloadUrl -OutFile $localMvnZip -UseBasicParsing
        Write-Host "Download complete. Extracting..." -ForegroundColor Green
        
        # Expand Archive
        Expand-Archive -Path $localMvnZip -DestinationPath $localMvnDir -Force
        
        # Delete zip
        Remove-Item $localMvnZip -Force
        
        if (Test-Path $localMvnBin) {
            Write-Host "Maven extracted successfully." -ForegroundColor Green
            $mvnCmd = $localMvnBin
        } else {
            throw "Maven executable not found after extraction."
        }
    } catch {
        Write-Error "Failed to download and extract portable Maven: $_"
        Exit 1
    }
}

# 3. Clean and Run Tests
Write-Host ""
Write-Host "Step 1: Running unit tests..." -ForegroundColor Cyan
& $mvnCmd clean test
if ($LASTEXITCODE -ne 0) {
    Write-Host "Tests failed! Aborting launch." -ForegroundColor Red
    Exit $LASTEXITCODE
}
Write-Host "Tests passed successfully!" -ForegroundColor Green

# 4. Compile and Run App
Write-Host ""
Write-Host "Step 2: Compiling and launching GUI Application..." -ForegroundColor Cyan
Write-Host "(Note: If MongoDB is offline, you will see a connection setup dialog to enter your MongoDB connection details.)" -ForegroundColor Yellow
Write-Host "Launching..." -ForegroundColor Gray
& $mvnCmd compile exec:java
