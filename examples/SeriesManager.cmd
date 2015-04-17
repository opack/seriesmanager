rem Répertoire dans lequel se trouve SeriesManager et le fichier settings.json
set SERIES_MANAGER_HOME=C:/Users/Didier/SeriesManager

rem Récupération des paramètres issus de uTorrent
rem Dans les prefs uTorrent, lancer la commande suivante lorsqu'un torrent
rem est complété : "C:/Users/Didier/SeriesManager/SeriesManager.cmd" "%D" "%N"
set DOWNLOAD_DIR=%1
set TORRENT_TITLE=%2

rem Suppression de l'éventuel dernier "\" dans le répertoire
set DOWNLOAD_DIR=%DOWNLOAD_DIR:\"="%

rem Lancement du programme
cd /d %SERIES_MANAGER_HOME%
"%JAVA_HOME%\bin\java.exe" -jar SeriesManager.jar %SERIES_MANAGER_HOME% %DOWNLOAD_DIR% %TORRENT_TITLE%