\
@echo off
setlocal
echo Nexa Android APK builder
echo.
where gradle >nul 2>nul
if %errorlevel%==0 (
  gradle assembleDebug
  goto done
)
echo Gradle command was not found.
echo Open this folder in Android Studio, wait for Gradle Sync, then choose:
echo Build ^> Build APK(s)
echo.
pause
:done
endlocal
