@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

cd /d "%~dp0"

echo ===============================================================================
echo   Comet Android APK 一键构建脚本 [arm64-v8a Release 版]
echo ===============================================================================
echo.

if exist "D:\jdk17\bin\java.exe" (
    if "%JAVA_HOME%"=="" set "JAVA_HOME=D:\jdk17"
    set "PATH=D:\jdk17\bin;!PATH!"
)

if "%ANDROID_HOME%"=="" (
    if exist "C:\Users\ADMIN\AppData\Local\Android\Sdk" (
        set "ANDROID_HOME=C:\Users\ADMIN\AppData\Local\Android\Sdk"
    )
)

if "%ANDROID_NDK_HOME%"=="" (
    if exist "%ANDROID_HOME%\ndk\29.0.14206865" (
        set "ANDROID_NDK_HOME=%ANDROID_HOME%\ndk\29.0.14206865"
    )
)

echo [*] 正在检查编译环境与依赖...

where java >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Java [java.exe]！请配置 JDK 17+ 到 PATH 或 JAVA_HOME。
    goto :FAIL
)

if not exist "%ANDROID_HOME%" (
    echo [错误] 未找到 Android SDK 目录！请设置 ANDROID_HOME 环境变量。
    goto :FAIL
)

if not exist "composeApp\libs\libcore.aar" (
    echo [错误] 未找到 composeApp\libs\libcore.aar 核心库！
    echo 请先参考 [构建.txt] 使用 anja 编译 libcore.aar。
    goto :FAIL
)

if not exist "release.keystore" (
    echo [错误] 未找到签名密钥 release.keystore！
    goto :FAIL
)

echo [OK] Android 编译环境与依赖检查通过。
echo.

echo [1/2] 正在编译 Android Release APK [Gradle assembleFossRelease]...
set BUILD_PLUGIN=none
set _JAVA_OPTIONS=-Dfile.encoding=UTF-8 -Dsun.java2d.d3d=false
call gradlew.bat :androidApp:assembleFossRelease
if errorlevel 1 (
    echo [错误] APK 编译构建失败！
    goto :FAIL
)
echo [OK] APK 编译完成。
echo.

echo [2/2] 正在导出安装包至项目根目录...
set "SRC_APK="
for %%A in (androidApp\build\outputs\apk\foss\release\Comet-*-arm64-v8a.apk) do (
    set "SRC_APK=%%~fA"
)

if not defined SRC_APK (
    echo [错误] 未在输出目录中找到生成的 APK 文件！
    goto :FAIL
)

copy /y "%SRC_APK%" ".\Comet-arm64-v8a.apk" >nul
if errorlevel 1 (
    echo [错误] 复制 APK 到根目录失败！
    goto :FAIL
)

echo.
echo ===============================================================================
echo   构建成功！
echo ===============================================================================
for %%F in (.\Comet-arm64-v8a.apk) do (
    set "FILE_SIZE=%%~zF"
    set /a "FILE_MB=!FILE_SIZE! / 1048576"
    echo   产物文件: %%~nxF
    echo   完整路径: %%~fF
    echo   文件大小: !FILE_MB! MB [!FILE_SIZE! 字节]
)
echo ===============================================================================
echo.
pause
exit /b 0

:FAIL
echo.
echo ===============================================================================
echo   构建失败，请检查上方错误输出信息。
echo ===============================================================================
echo.
pause
exit /b 1