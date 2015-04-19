# seriesmanager

# Objectif
SeriesManager simplifie la gestion des séries téléchargées légalement. Il simplifie la copie depuis le répertoire de téléchargement vers différents répertoires (sauvegarde, répertoire de lecture du media center...) et marque l'épisode comme téléchargé dans BetaSeries si nécessaire.

# Utilisation
SeriesManager traite 1 fichier téléchargé à la fois. Il doit donc être lancé unitairement pour chaque fichier téléchargé ; cela permet notamment de le démarrer automatiquement à la fin du téléchargement comme le proposent uTorrent.

Pour lancer SeriesManager, exécuter la commande suivante :

```
java -jar SeriesManager.jar %SERIES_MANAGER_HOME% %DOWNLOAD_DIR% %TORRENT_TITLE%
```
où :
* SERIES_MANAGER_HOME : répertoire "maison" de l'application. C'est là que seront stockées les logs et que sera recherché le fichier de configuration (dans conf/settings.json).
* DOWNLOAD_DIR : répertoire dans lequel a été téléchargé le fichier
* TORRENT_TITLE : nom du torrent, duquel sera extrait le nom de la série, le numéro de la saison et de l'épisode.

Le programme peut également être lancé grâce au script SeriesManager.cmd, qui attend en paramètres DOWNLOAD_DIR et TORRENT_TITLE (dans cet ordre).

# Configuration
Le fichier de configuration indique à SeriesManager quelles séries sont gérées et comment elles doivent l'être. Le fichier examples/settings/help-settings.json fournit des explications sur le contenu attendu, et le fichier examples/settings/settings.json sert d'exemple.