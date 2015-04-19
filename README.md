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
* ```SERIES_MANAGER_HOME``` : répertoire "maison" de l'application. C'est là que seront stockées les logs et que sera recherché le fichier de configuration (dans ```conf/settings.json```).
* ```DOWNLOAD_DIR``` : répertoire dans lequel a été téléchargé le fichier
* ```TORRENT_TITLE``` : nom du torrent, duquel sera extrait le nom de la série, le numéro de la saison et de l'épisode. Le premier fichier vidéo correspondant à ces critères dans le répertoire ```DOWNLOAD_DIR``` sera copié dans le(s) répertoire(s) indiqués dans le fichier ```conf/settings.json```. Note : seuls les fichiers avec l'extension ```.avi``` ou ```.mp4``` et ne contenant pas le mot ```sample``` sont potentiellement sélectionnables.

Le programme peut également être lancé grâce au script SeriesManager.cmd, qui attend en paramètres ```DOWNLOAD_DIR``` et ```TORRENT_TITLE``` (dans cet ordre).

# Configuration
Le fichier de configuration indique à SeriesManager quelles séries sont gérées et comment elles doivent l'être. Le fichier ```examples/settings/help-settings.json``` fournit des explications sur le contenu attendu, et le fichier ```examples/settings/settings.json``` sert d'exemple.

## Tableau ```properties```
Contient des propriétés utilisées dans le fichier, comme des constantes. Pour le moment, ces propriétés ne sont utilisées que pour le champ ```mapping.copy-to```.

Les éléments du tableau sont des objets contenant les champs suivants :
* ```key``` : réprésente le nom de la constante qui sera réutilisé ailleurs dans le fichier ```settings.json```
* ```value``` : valeur à substituer à la clé. Celle-ci peut contenir des variables magiques comme ```#SHOW#```.

Exemple :
```json
"properties": [
	{
		"key": "MEDIA_CENTER",
		"value": "X:/Vidéos/Séries/#SHOW#",
	},
	{
		"key": "PARTAGE",
		"value": "C:/Partage/Séries/",
	}
]
```

## Objet ```logs```
Cet objet paramètre le comportement de la log associée à chaque série. La log générale et d'erreur est quant à elle stockée dans ```SERIES_MANAGER_HOME/SeriesManager.log```.

Il contient 2 champs :
* ```enabled``` : ```true``` pour activer l'écriture de la log dans un fichier, ```false``` pour la désactiver.
* ```directory``` : nom du répertoire, basé dans ```SERIES_MANAGER_HOME```, dans lequel écrire les fichiers de log.

Exemple :
```json
"logs": {
	"enabled":"true",
	"directory":"logs"
}
```

## Objet ```betaseries```
Contient les réglages permettant à SeriesManager de se connecter à BetaSeries pour marquer l'épisode comme téléchargé. Contient les champs suivants :
* ```markAsDownloaded``` : ```true``` s'il faut marquer les épisodes comme téléchargés, ```false``` sinon.
* ```login``` : si ```markAsDownloaded```=```true```, alors ce champ doit indiquer l'identifiant du compte BetaSeries.
* ```md5-password``` : si ```markAsDownloaded```=```true```, alors ce champ doit contenir le hash md5 du mot de passe du compte BetaSeries.

Exemple :
```json
"betaseries": {
	"markAsDownloaded": "true",
	"login": "brinchwiddle19012015",
	"md5-password": "1226547a788ea091a4e4f172d9590b06"
}
```

## Tableau ```mappings```
Les mappings indiquent à SeriesManager comment déterminer les fichiers appartenant à une série, et les répertoires où copier ces fichiers.

Chaque élément du tableau est un objet ayant la structure suivante :
* ```show``` : indique le nom de la série. Obligatoire, ce champ sert à l'affichage mais est aussi la valeur substituée si la variable magique ```#SHOW#``` est utilisée.
* ```betaseries-id``` : identifiant de la série. Cet identifiant est facultatif et s'il n'est pas renseigné cette valeur sera déterminée en effectuant une recherche sur BetaSeries. Pour gagner du temps et éviter des erreurs, renseigner ce champ est une bonne idée.
* ```title-includes``` : chaine de caractères qui doit se trouver dans le titre reçu en paramètre (```TORRENT_TITLE```) afin de valider que ```TORRENT_TITLE``` correspond bien à cette série. La casse n'est pas prise en compte.
* ```title-excludes``` : chaine de caractères qui ne soit pas se trouver dans le titre reçu en paramètre (```TORRENT_TITLE```) afin de valider que ```TORRENT_TITLE``` correspond bien à cette série. La casse n'est pas prise en compte.
* ```copy-to``` : tableau de chaines de caractères contenant les chemins vers lesquels copier le fichier à traiter. Ces chemins peuvent être absolus, utiliser les variables magiques ou les propriétés définies dans l'objet ```properties```. Voici quelques exemples valides :
  * ```"C:/series"``` : le fichier vidéo sera recopié dans ```C:/series```.
  * ```"C:/series/#SHOW#"``` : si la ```show```= ```"Arrow"```, alors le fichier vidéo sera recopié dans ```C:/series/Arrow```.
  * ```"%PARTAGE%"``` : si on considère l'exemple donné pour l'explication de l'objet ```properties```, alors le fichier vidéo sera recopié dans ```C:/Partage/Séries/```.

Exemple :
```json
"mappings": [
	{
		"show": "Arrow",
		"copy-to": [
			"C:/series/#SHOW#"
			"%PARTAGE%"
		]
	},
	{
		"show": "Banshee",
		"copy-to": [
			"%PARTAGE%"
		]
	},
	{
		"show": "Black Sails"
	}
]
```
Dans cet exemple, la série Black Sails s'appuie sur le mapping par défaut (voir section suivante) et les vidéos de cette séries seront donc copiées dans ```X:/Vidéos/Séries/Black Sails``` si leur titre contient ```black sails```.

## Objet ```default-mapping```
Définit des valeurs par défaut pour éviter que chaque mapping n'ait à réécrire les mêmes données.
Les champs qui peuvent être définis par défaut sont :
* ```title-includes```
* ```title-excludes```
* ```copy-to```

Exemple :
```json
"default-mapping": {
	"title-includes": "#SHOW#",
	"copy-to": [
		"%MEDIA_CENTER%"
	]
}
```
Dans cet exemple, on voit que par défaut, si ces valeurs ne sont pas redéfinies :
* un fichier vidéo correspondant à une certaine série contiendra le nom de cette série
* ce fichier vidéo sera copié dans ```X:/Vidéos/Séries/#SHOW#```, comme indiqué dans l'objet ```properties```.
