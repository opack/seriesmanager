rem Répertoire dans lequel se trouve SeriesManager
set SERIES_MANAGER_HOME=C:/Users/Didier/SeriesManager

rem Suppression de l'éventuel dernier "\" dans le répertoire
set DIRECTORY=%2
set DIRECTORY=%DIRECTORY:\"="%

rem Lancement du programme
cd /d %SERIES_MANAGER_HOME%
"%JAVA_HOME%\bin\java.exe" -jar SeriesManager.jar %1 %DIRECTORY% preferences.json >> SeriesManager.log