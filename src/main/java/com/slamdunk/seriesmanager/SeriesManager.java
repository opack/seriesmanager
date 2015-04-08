package com.slamdunk.seriesmanager;

import static com.slamdunk.seriesmanager.Logger.Levels.ERROR;
import static com.slamdunk.seriesmanager.Logger.Levels.INFO;
import static com.slamdunk.seriesmanager.Logger.Levels.WARN;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.slamdunk.seriesmanager.configuration.Settings;
public class SeriesManager {
	private String homeDirectory;
	private String sourceFilename;
	private String sourceDirectory;
	private boolean multiFilesDownload;
	
	private Settings preferences;
	
	public SeriesManager(String home, String filename, String directory, boolean multi) {
		Logger.add(INFO, "----------------------------------------------------------------------------");
		Logger.add(INFO, new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(new Date()));
		Logger.add(INFO, "Paramètres reçus :");
		Logger.add(INFO, "\t\tSeriesManager Home : " + home);
		Logger.add(INFO, "\t\tFichier            : " + filename);
		Logger.add(INFO, "\t\tRépertoire         : " + directory);
		Logger.add(INFO, "\t\tMulti-fichiers     : " + multi);
		
		// Récupération des paramètres
		homeDirectory = home;
		sourceFilename = filename;
		sourceDirectory = directory;
		multiFilesDownload = multi;
	}
	
	public boolean process() {
		// Si on a un download multi-fichiers, alors on doit rechercher le nom du fichier
		// vidéo dans le répertoire indiqué
		if (multiFilesDownload) {
			sourceFilename = FilenameParser.locateVideoFile(sourceDirectory);
			if (sourceFilename == null) {
				return false;
			}
		}
		
		// Extrait les infos sur la série à partir du nom du fichier à traiter
		FilenameParser parser = new FilenameParser();
		if (!parser.parse(sourceFilename)) {
			return false;
		}
		
		// Lecture des préférences générales et de celles pour ce show
		preferences = new Settings();
		if (!preferences.load(homeDirectory, parser.title)) {
			return false;
		}
		
		// Copie du fichier vers la/les destinations adéquates
		if (!preferences.mapping.copyDestinations.isEmpty()) {
			try {
				performCopy(preferences.mapping.copyDestinations);
			} catch (IOException e) {
				Logger.add(ERROR, "Erreur lors de la copie : " + e.getClass().getSimpleName() + " - " + e.getMessage());
			}
		}
			
		// Mise à jour de BetaSeries
		if (preferences.betaseries.markAsDownloaded) {
			markAsDownloaded(preferences.betaseries.login, preferences.betaseries.password, preferences.mapping.betaseriesShowId, parser.title, parser.season, parser.episode); 
		} else {
			Logger.add(INFO, "La série ne sera pas marquée comme téléchargée dans BetaSeries conformément aux préférences.");
		}
		
		// Si les logs sont activées, on les écrit
		if (preferences.logs.enabled) {
			Path logFile = Paths.get(homeDirectory, preferences.logs.directory, preferences.mapping.showName + ".log");
			Logger.flushToFile(logFile);
		}
		return true;
	}

	private void markAsDownloaded(String betaseriesLogin, String betaseriesPassword, String showId, String showTitle, int season, int episode) {
		// Connexion à l'API de BetaSeries
		BetaSeriesApi betaSeries = new BetaSeriesApi();
		betaSeries.auth(betaseriesLogin, betaseriesPassword);
		
		// Récupération de l'id de l'épisode
		if (showId == null || showId.isEmpty()) {
			showId = betaSeries.getShowId(showTitle);
		}
		String episodeId = betaSeries.getEpisodeId(showId, season, episode);
		
		// Marquage de l'épisode comme téléchargé
		boolean result = betaSeries.setEpisodeDownloaded(episodeId, true);
		
		Logger.add(INFO, "Mise à jour de BetaSeries :");
		Logger.add(INFO, "\t\tId du show trouvé dans le mapping : " + preferences.mapping.betaseriesShowId);
		Logger.add(INFO, "\t\tRésultat de l'opération : " + result);
	}

	/**
	 * Copie le fichier vers les destinations indiquées
	 * @param file Fichier à copier
	 * @param destinations
	 * @throws IOException 
	 */
	private void performCopy(List<String> destinations) throws IOException {
		Path source = Paths.get(sourceDirectory, sourceFilename);
		Logger.add(INFO, "Copies ");
		for (String destination : destinations) {
			// Remplacement des variables
			String extendedDestination = destination.replaceAll(Settings.SHOW_VAR, preferences.mapping.showName);
			
			// Détermine le répertoire de destination
			Path destinationFile = Paths.get(extendedDestination, sourceFilename);
			
			// Copie du fichier
			if (Files.notExists(destinationFile)) {
				Files.createDirectories(Paths.get(extendedDestination));
				
				Files.copy(source, destinationFile);
				Logger.add(INFO, "\t\tCopie effectuée vers " + extendedDestination);
			} else {
				Logger.add(WARN, "\t\tCopie annulée vers " + extendedDestination + " car le fichier existe déjà");
			}
		}
	}
	
	/**
	 * Arg0 : nom du fichier téléchargé
	 * Arg1 : répertoire contenant ce fichier
	 * Arg2 : chemin complet vers le fichier de préférences
	 * @param args
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws IOException, URISyntaxException {
		if (args.length < 4) {
			System.err.println("Le nombre de paramètres n'est pas correct.");
			
			System.err.println("Arguments attendus (dans l'ordre) :");
			System.err.println("\t[chemin racine de Series Manager]");
			System.err.println("\t[nom fichier téléchargé]");
			System.err.println("\t[répertoire contenant ce fichier]");
			System.err.println("\t[flag de download multiple (simple|multi)]");
			
			System.err.println("Arguments reçus :");
			for (String arg : args) {
				System.err.println(arg);
			}
			System.exit(1);
		}
		
		SeriesManager manager = new SeriesManager(args[0], args[1], args[2], "multi".equals(args[3]));
		if (!manager.process()) {
			// Une erreur irrécupérable s'est produite. On log vers le fichier de logs par défaut
			String runPath = System.getProperty("user.dir", "");
			Path logFile = Paths.get(runPath, "SeriesManager.log");
			Logger.flushToFile(logFile);
		}
	}
}
