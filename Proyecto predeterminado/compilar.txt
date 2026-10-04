@echo off
echo ========================================
echo   COMPILADOR HUNGERGAMES - AUTO
echo ========================================
echo.

REM Verificar Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java NO encontrado. Instala Java 21 primero.
    echo Descarga: https://adoptium.net/temurin/releases/?version=21
    pause
    exit /b 1
)

echo ✅ Java detectado:
java -version 2>&1 | findstr /R "version"
echo.

REM Descargar Maven Wrapper si no existe
if not exist ".mvn\wrapper\maven-wrapper.jar" (
    echo 📥 Descargando Maven Wrapper...
    mkdir .mvn\wrapper 2>nul
    curl -L -o .mvn\wrapper\maven-wrapper.jar "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar" 2>nul
    echo distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip > .mvn\wrapper\maven-wrapper.properties
    echo @echo off > mvnw.cmd
    echo java -jar ".mvn/wrapper/maven-wrapper.jar" %%* >> mvnw.cmd
)

echo 🔨 Compilando...
echo.

call mvnw.cmd clean package

echo.
if %errorlevel% equ 0 (
    echo ========================================
    echo   ✅ ¡EXITO! JAR creado en:
    echo   target\HungerGames-1.0.0.jar
    echo ========================================
) else (
    echo ========================================
    echo   ❌ ERROR en compilación
    echo ========================================
)
pause