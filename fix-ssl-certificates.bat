@echo off
echo ========================================
echo Java SSL Certificate Fix Script
echo ========================================
echo.

REM Check if running as administrator
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo ERROR: This script requires Administrator privileges!
    echo Please right-click and select "Run as Administrator"
    pause
    exit /b 1
)

echo Java Home: D:\JAVA
echo Cacerts location: D:\JAVA\lib\security\cacerts
echo.

REM Backup existing cacerts
echo Creating backup of cacerts...
copy "D:\JAVA\lib\security\cacerts" "D:\JAVA\lib\security\cacerts.backup.%date:~-4,4%%date:~-10,2%%date:~-7,2%"
if %errorLevel% neq 0 (
    echo ERROR: Failed to create backup!
    pause
    exit /b 1
)
echo Backup created successfully!
echo.

REM Download and import Let's Encrypt root certificate
echo Downloading Let's Encrypt root certificate...
powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; try { Invoke-WebRequest -Uri 'https://letsencrypt.org/certs/isrgrootx1.pem' -OutFile 'isrgrootx1.pem' -UseBasicParsing; Write-Host 'Downloaded successfully' } catch { Write-Host 'Failed to download' }"

if exist isrgrootx1.pem (
    echo Converting and importing certificate...
    keytool -import -trustcacerts -keystore "D:\JAVA\lib\security\cacerts" -storepass changeit -noprompt -alias letsencryptroot -file isrgrootx1.pem
    del isrgrootx1.pem
    echo Let's Encrypt certificate imported!
) else (
    echo Failed to download certificate, skipping...
)
echo.

REM Try to sync with system certificates
echo Syncing with Windows certificate store...
keytool -importkeystore -srckeystore NUL -srcstoretype Windows-ROOT -destkeystore "D:\JAVA\lib\security\cacerts" -deststorepass changeit -noprompt 2>nul
echo.

echo ========================================
echo Certificate update completed!
echo ========================================
echo.
echo If the issue persists, try these alternatives:
echo 1. Disable antivirus SSL scanning temporarily
echo 2. Check if you're behind a corporate proxy
echo 3. Use HTTP repositories (less secure)
echo.
pause

