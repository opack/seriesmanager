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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.slamdunk.seriesmanager.preferences.Preferences;
public class SeriesManager {
	private static final Pattern SERIES_TITLE_PATTERN_1 = Pattern.compile("(.*)\\.S(\\d?\\d)E(\\d\\d).*", Pattern.CASE_INSENSITIVE);
	private static final Pattern SERIES_TITLE_PATTERN_2 = Pattern.compile("(.*)\\.(\\d?\\d)(\\d\\d).*", Pattern.CASE_INSENSITIVE);

	private final String sourceFilename;
	private final String sourceDirectory;
	
	private final String title;
	private final int season;
	private final int episode;
	
	private Preferences preferences;
	
	public SeriesManager(String filename, String directory, String preferencesFile) {
		Logger.add(INFO, "----------------------------------------------------------------------------");
		Logger.add(INFO, new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(new Date()));
		Logger.add(INFO, "Paramètres reçus :");
		Logger.add(INFO, "\t\tFichier    : " + filename);
		Logger.add(INFO, "\t\tRépertoire : " + directory);
		
		// Récupération des paramètres
		sourceFilename = filename;
		sourceDirectory = directory;
		
		// Vérifie le format du nom de fichier
		boolean filenameMatched = true;
		Matcher m = SERIES_TITLE_PATTERN_1.matcher(sourceFilename);
		if (!m.matches()) {
			m = SERIES_TITLE_PATTERN_2.matcher(sourceFilename);
			if (!m.matches()) {
				filenameMatched = false;
			}
		}
		
		// Extraction du nom de la série et du numéro de l'épisode
		if (filenameMatched) {
			title = m.group(1).replaceAll("\\.", " ");
			season = Integer.parseInt(m.group(2));
			episode = Integer.parseInt(m.group(3));
			
			// Lecture des préférences
			preferences = new Preferences();
			preferences.load(preferencesFile, title);
		} else {
			Logger.add(ERROR, "Le nom de fichier " + sourceFilename + " ne correspond pas au format attendu.");
			title = "";
			season = 0;
			episode = 0;
		}
		
		Logger.add(INFO, "Informations extraites :");
		Logger.add(INFO, "\t\tTitre   : " + title);
		Logger.add(INFO, "\t\tSaison  : " + season);
		Logger.add(INFO, "\t\tEpisode : " + episode);
	}
	
	public void process() {
		// Si le titre n'est pas correct, c'est qu'on n'est probablement
		// pas en présence d'une série.
		if (title.isEmpty()) {
			return;
		}
		
		if (!preferences.mapping.copyDestinations.isEmpty()) {
			// Copie du fichier vers la/les destinations adéquates
			try {
				performCopy(preferences.mapping.copyDestinations);
			} catch (IOException e) {
				Logger.add(ERROR, "Erreur lors de la copie du fichier vers les destination : " + e.getMessage());
			}
			
			// Mise à jour de BetaSeries
			boolean updateBetaSeries = true;
			if (preferences.betaseries.markAsDownloaded) {
				if (preferences.betaseries.login == null || preferences.betaseries.password == null) {
					Logger.add(WARN, "Mise à jour de BetaSeries impossible car le nom ou le mot de passe n'est pas renseigné.");
					updateBetaSeries = false;
				}
			} else {
				Logger.add(INFO, "La série ne sera pas marquée comme téléchargée dans BetaSeries conformément aux préférences.");
				updateBetaSeries = false;
			}
			if (updateBetaSeries) {
				boolean result = markAsDownloaded(preferences.betaseries.login, preferences.betaseries.password, preferences.mapping.betaseriesShowId, title, season, episode); 
				Logger.add(INFO, "Mise à jour de BetaSeries :");
				Logger.add(INFO, "\t\tId du show trouvé dans le mapping : " + preferences.mapping.betaseriesShowId);
				Logger.add(INFO, "\t\tRésultat de l'opération : " + result);
			}
		}
		
		// Si les logs sont activées, on les écrit
		if (preferences.logs.enabled) {
			Path logFile = Paths.get(preferences.workingDirectory, preferences.logs.directory + preferences.mapping.showName + ".log");
			Logger.flushToFile(logFile);
		}
	}

	private boolean markAsDownloaded(String betaseriesLogin, String betaseriesPassword, String showId, String showTitle, int season, int episode) {
		// Connexion à l'API de BetaSeries
		BetaSeriesApi betaSeries = new BetaSeriesApi();
		betaSeries.auth(betaseriesLogin, betaseriesPassword);
		
		// Récupération de l'id de l'épisode
		if (showId == null || showId.isEmpty()) {
			showId = betaSeries.getShowId(showTitle);
		}
		String episodeId = betaSeries.getEpisodeId(showId, season, episode);
		
		// Marquage de l'épisode comme téléchargé
		return betaSeries.setEpisodeDownloaded(episodeId, true);
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
			String extendedDestination = destination.replaceAll(Preferences.SHOW_VAR, preferences.mapping.showName);
			
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
		if (args.length < 3) {
			System.err.println("Le nombre de paramètres n'est pas correct.");
			System.err.println("Arguments attendus : [nom fichier téléchargé] [répertoire contenant ce fichier] [chemin complet vers le fichier de préférences]");
			System.err.println("Arguments reçus :");
			for (String arg : args) {
				System.err.println("SeriesManager.main() " + arg);
			}
			System.exit(1);
		}
		SeriesManager manager = new SeriesManager(args[0], args[1], args[2]);
		manager.process();
	}
}
