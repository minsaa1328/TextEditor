@echo off
setlocal EnableExtensions EnableDelayedExpansion
title Kotlin ADB Compile Listener (SAFE)

REM ---- HARD-CODED PATHS ----
set "ADB_EXE=C:\Users\minsa_\AppData\Local\Android\Sdk\platform-tools\adb.exe"
set "KOTLINC_EXE=C:\kotlinc\bin\kotlinc.bat"
set "JAVA_EXE=C:\Program Files (x86)\Common Files\Oracle\Java\java8path\java.exe"
set "DEVICE_DIR=/sdcard/Android/data/com.example.kotlintexteditor/files"

echo ==============================
echo  SAFE listener starting...
echo ==============================

echo [CHECK] adb...
"%ADB_EXE%" version || (echo [ERROR] adb not found.& pause & exit /b 1)

echo [CHECK] java...
"%JAVA_EXE%" -version >nul 2>&1 || (echo [ERROR] java not found.& pause & exit /b 1)

echo [CHECK] kotlinc...
CALL "%KOTLINC_EXE%" -version >nul 2>&1 || (echo [ERROR] kotlinc not found.& pause & exit /b 1)

echo [CHECK] device...
"%ADB_EXE%" start-server >nul 2>&1
set "DEVICE_FOUND="
for /f "skip=1 tokens=1" %%D in ('"%ADB_EXE%" devices') do (
  if not "%%D"=="" if not "%%D"=="List" set "DEVICE_FOUND=1"
)
if not defined DEVICE_FOUND (
  echo [ERROR] No device detected. Authorize USB debugging and try again.
  pause
  exit /b 1
)

echo [OK] Entering watch loop. Press Ctrl+C to stop.
echo.

:loop
echo [LOOP] Polling device for compile.kt...
"%ADB_EXE%" pull "%DEVICE_DIR%/compile.kt" "compile.kt" >nul 2>&1
if not exist "compile.kt" (
  timeout /t 2 >nul
  goto :loop
)

for %%I in ("compile.kt") do set "CODESIZE=%%~zI"
if "!CODESIZE!"=="0" (
  del /f /q "compile.kt" >nul 2>&1
  "%ADB_EXE%" shell rm "%DEVICE_DIR%/compile.kt" >nul 2>&1
  timeout /t 1 >nul
  goto :loop
)

echo.
echo [BUILD] Compiling compile.kt...
> "compiler_output.txt" echo ----------------- Compiler Output -----------------
CALL "%KOTLINC_EXE%" "compile.kt" -include-runtime -d "output.jar" >> "compiler_output.txt" 2>&1
type "compiler_output.txt"
echo ---------------------------------------------------

findstr /i /c:"error:" /c:"e: " "compiler_output.txt" >nul
if errorlevel 1 (
  echo [RUN] No compile errors. Executing jar...
  "%JAVA_EXE%" -jar "output.jar" > "runtime_output.txt" 2>&1
) else (
  echo [INFO] Compiler errors found. Skipping run.
  > "runtime_output.txt" echo No runtime due to compilation errors.
)

echo [PUSH] Sending results back to device...
"%ADB_EXE%" push "compiler_output.txt" "%DEVICE_DIR%/compiler_output.txt" >nul
"%ADB_EXE%" push "runtime_output.txt"  "%DEVICE_DIR%/runtime_output.txt"  >nul

del /f /q "compile.kt" "output.jar" >nul 2>&1
"%ADB_EXE%" shell rm "%DEVICE_DIR%/compile.kt" >nul 2>&1

timeout /t 1 >nul
goto :loop
