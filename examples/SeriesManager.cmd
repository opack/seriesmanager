rem Répertoire dans lequel se trouve SeriesManager et le fichier settings.json
set SERIES_MANAGER_HOME=C:/Users/Didier/SeriesManager

rem Récupération des paramètres issus de uTorrent
rem Dans les prefs uTorrent, lancer la commande suivante lorsqu'un torrent
rem est complété : "C:/Users/Didier/SeriesManager/SeriesManager.cmd" "%F" "%D" "%K"
set DOWNLOADED_FILE=%1
set DOWNLOAD_DIR=%2
set MULTI_DOWNLOAD_FLAG=%3

rem Suppression de l'éventuel dernier "\" dans le répertoire
set DOWNLOAD_DIR=%DOWNLOAD_DIR:\"="%

rem Lancement du programme
cd /d %SERIES_MANAGER_HOME%
"%JAVA_HOME%\bin\java.exe" -jar SeriesManager.jar %SERIES_MANAGER_HOME% %DOWNLOADED_FILE% %DOWNLOAD_DIR% %MULTI_DOWNLOAD_FLAG%