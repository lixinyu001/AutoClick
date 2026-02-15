@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ========================================
echo   AutoClicker APK 构建工具
echo ========================================
echo.

cd /d "%~dp0"

:: 检查Java环境
echo [1/5] 检查Java环境...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误：未找到Java
    echo.
    echo 请先安装Java JDK 17：
    echo 下载地址：https://adoptium.net/temurin/releases/?version=17
    echo.
    echo 安装后，请配置JAVA_HOME环境变量：
    echo JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot
    echo 并将 %JAVA_HOME%\bin 添加到Path环境变量
    echo.
    pause
    exit /b 1
)

:: 显示Java版本
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VERSION=%%i
echo ✅ Java环境正常 (!JAVA_VERSION!)
echo.

:: 检查Android SDK
echo [2/5] 检查Android SDK...
if not defined ANDROID_HOME (
    echo ❌ 错误：未找到ANDROID_HOME环境变量
    echo.
    echo 请配置ANDROID_HOME环境变量：
    echo ANDROID_HOME = C:\Users\你的用户名\AppData\Local\Android\Sdk
    echo.
    echo 并将以下路径添加到Path环境变量：
    echo %%ANDROID_HOME%%\platform-tools
    echo %%ANDROID_HOME%%\cmdline-tools\latest\bin
    echo.
    pause
    exit /b 1
)
echo ✅ Android SDK环境正常
echo.

:: 检查Gradle Wrapper
echo [3/5] 检查Gradle Wrapper...
if not exist "gradlew.bat" (
    echo ❌ 错误：未找到gradlew.bat
    pause
    exit /b 1
)
echo ✅ Gradle Wrapper正常
echo.

:: 生成debug.keystore
echo [4/5] 生成debug.keystore...
if not exist "app\debug.keystore" (
    keytool -genkey -v -keystore app\debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US" >nul 2>&1
    if %errorlevel% neq 0 (
        echo ❌ 错误：生成keystore失败
        echo 请检查keytool是否可用
        pause
        exit /b 1
    )
    echo ✅ debug.keystore生成成功
) else (
    echo ✅ debug.keystore已存在
)
echo.

:: 构建APK
echo [5/5] 构建Release APK...
echo.
echo 正在构建，请稍候...
echo.

call gradlew.bat assembleRelease --stacktrace
if %errorlevel% neq 0 (
    echo.
    echo ❌ 错误：构建失败
    echo.
    echo 请检查：
    echo 1. 网络连接是否正常
    echo 2. Android SDK是否完整安装
    echo 3. 依赖项是否可以正常下载
    echo.
    pause
    exit /b 1
)
echo.

:: 检查APK是否生成
if not exist "app\build\outputs\apk\release\app-release-unsigned.apk" (
    echo ❌ 错误：APK文件未生成
    pause
    exit /b 1
)

:: 显示构建结果
echo ========================================
echo   ✅ 构建成功！
echo ========================================
echo.
echo APK文件信息：
dir "app\build\outputs\apk\release\*.apk" /b
echo.
echo 文件大小：
for %%F in ("app\build\outputs\apk\release\*.apk") do echo %%~zF 字节
echo.
echo APK文件位置：
echo %CD%\app\build\outputs\apk\release\
echo.

:: 询问是否打开文件夹
echo 按任意键打开APK文件夹...
pause >nul
explorer "app\build\outputs\apk\release"

endlocal
