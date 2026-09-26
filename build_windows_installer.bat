@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

cd /d "%~dp0"

echo ===============================================================================
echo   Comet Windows 安装程序一键构建脚本 [JBR 集成版]
echo ===============================================================================
echo.

if exist "D:\tools\zig\zig.exe" (
    set "PATH=D:\tools\zig;!PATH!"
)
if exist "C:\Program Files (x86)\NSIS\makensis.exe" (
    set "PATH=C:\Program Files (x86)\NSIS;!PATH!"
)
if exist "C:\Program Files\NSIS\makensis.exe" (
    set "PATH=C:\Program Files\NSIS;!PATH!"
)
if exist "C:\Python312\python.exe" (
    set "PATH=C:\Python312;!PATH!"
)
if exist "D:\jdk17\bin\java.exe" (
    if "%JAVA_HOME%"=="" set "JAVA_HOME=D:\jdk17"
    set "PATH=D:\jdk17\bin;!PATH!"
)

set "BASH_EXE="
if exist "C:\Program Files\Git\bin\bash.exe" (
    set "BASH_EXE=C:\Program Files\Git\bin\bash.exe"
) else (
    for /f "delims=" %%i in ('where bash 2^>nul') do (
        if not defined BASH_EXE set "BASH_EXE=%%i"
    )
)

echo [*] 正在检查编译依赖工具...

if not defined BASH_EXE (
    echo [错误] 未找到 Git Bash [bash.exe]，请确认安装了 Git for Windows！
    goto :FAIL
)

where zig >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Zig 编译器 [zig.exe]！
    goto :FAIL
)

where makensis >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 NSIS 编译器 [makensis.exe]！
    goto :FAIL
)

where python >nul 2>&1
if errorlevel 1 (
    where python3 >nul 2>&1
    if errorlevel 1 (
        echo [错误] 未找到 Python 运行环境！
        goto :FAIL
    )
)

where java >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Java [java.exe]！请配置 JDK 17+ 到 PATH 或 JAVA_HOME。
    goto :FAIL
)

if not exist "build\jbr\windows_amd64\jmods" (
    echo [错误] 未找到 JBR 模块目录: build\jbr\windows_amd64\jmods
    echo 请先运行: ./run lib jbr windows/amd64 获取 JBR 运行时模块。
    goto :FAIL
)

if not exist "libcore\build\windows_amd64\husicore.dll" (
    echo [警告] 未检测到 libcore\build\windows_amd64\husicore.dll 核心库！
    echo 若构建报错，请先使用 anja 编译 Go 核心。
)

echo [OK] 编译工具链与依赖检查通过。
echo.

echo [1/4] 正在编译原生启动器 Launcher [Zig]...
cd launcher
if exist ".zig-cache" rd /s /q ".zig-cache" >nul 2>&1
if exist "zig-out" rd /s /q "zig-out" >nul 2>&1
zig build -Doptimize=ReleaseSmall -Dtarget=x86_64-windows
if errorlevel 1 (
    cd /d "%~dp0"
    echo [错误] Launcher 编译失败！
    goto :FAIL
)
cd /d "%~dp0"
echo [OK] Launcher 编译完成。
echo.

echo [2/4] 正在编译核心宿主 Shim [husi-core.exe]...
cd libcore\shim
zig build -Doptimize=ReleaseSmall -Dtarget=x86_64-windows
if errorlevel 1 (
    cd /d "%~dp0"
    echo [错误] husi-core Shim 编译失败！
    goto :FAIL
)
cd /d "%~dp0"
if not exist "libcore\build\windows_amd64" md "libcore\build\windows_amd64"
copy /y "libcore\shim\zig-out\bin\husi-core-windows-x86_64.exe" "libcore\build\windows_amd64\husi-core.exe" >nul
echo [OK] husi-core Shim 编译完成。
echo.

if "%1"=="--skip-gradle" goto :SKIP_GRADLE
if "%1"=="--quick" goto :SKIP_GRADLE

echo [3/4] 正在编译 Compose Desktop Release UberJar [Gradle]...
set BUILD_PLUGIN=none
set _JAVA_OPTIONS=-Dfile.encoding=UTF-8 -Dsun.java2d.d3d=false
call gradlew.bat -p composeApp packageReleaseUberJarForCurrentOS -PdesktopTarget=windows/amd64
if errorlevel 1 (
    echo [错误] UberJar 构建失败！
    goto :FAIL
)
goto :GRADLE_DONE

:SKIP_GRADLE
echo [3/4] 跳过 Gradle 编译步骤 [使用已有 UberJar]...

:GRADLE_DONE
echo [OK] Compose Desktop UberJar 就绪。
echo.

echo [4/4] 正在使用 NSIS 打包生成安装程序 [嵌入 JBR 21，LZMA 压缩]...
"%BASH_EXE%" -c "export PATH=\"$PWD/build/jbr/windows_amd64/bin:/c/Program Files (x86)/NSIS:/c/Program Files/NSIS:$PATH\"; ./release/windows/package.sh --target windows/amd64 --formats nsis --jbr-only --no-sign --jbr-jmods build/jbr/windows_amd64/jmods"
if errorlevel 1 (
    echo [错误] NSIS 安装程序打包失败！
    goto :FAIL
)

copy /y "composeApp\build\compose\packages\windows\Comet-*-windows-amd64-jbr-installer.exe" ".\" >nul

echo.
echo ===============================================================================
echo   构建成功！
echo ===============================================================================
for %%F in (.\Comet-*-windows-amd64-jbr-installer.exe) do (
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