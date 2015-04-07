package com.slamdunk.seriesmanager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeriesManager {
	private static final Pattern SERIES_TITLE_PATTERN = Pattern.compile("(.*)\\.S(\\d\\d)E(\\d\\d).*", Pattern.CASE_INSENSITIVE);
	private static final String TITLE_VAR = "#TITLE#";

	private final String sourceFilename;
	private final String sourceDirectory;
	
	private final String title;
	private final int season;
	private final int episode;
	
	private Preferences preferences;
	
	public SeriesManager(String filename, String directory, String preferencesFile) {
		Logger.add("--------------------------------------");
		Logger.add(new Date().toString());
		Logger.add("filename=" + filename + ", directory=" + directory);
		
		// Récupération des paramètres
		sourceFilename = filename;
		sourceDirectory = directory;
		
		// Extraction du nom de la série et du numéro de l'épisode
		Matcher m = SERIES_TITLE_PATTERN.matcher(sourceFilename);
		if (m.matches()) {
			title = m.group(1).replaceAll("\\.", " ");
			season = Integer.parseInt(m.group(2));
			episode = Integer.parseInt(m.group(3));
		} else {
			title = "";
			season = 0;
			episode = 0;
		}
		
		// Lecture des préférences
		preferences = new Preferences();
		preferences.load(preferencesFile, title);
		
		Logger.add("title=" + title + ", season=" + season + ", episode=" + episode);
	}
	
	public void process() {
		// Si le titre n'est pas correct, c'est qu'on n'est probablement
		// pas en présence d'une série.
		if (title.isEmpty()) {
			return;
		}
		
		if (!preferences.copyDestinations.isEmpty()) {
			// Copie du fichier vers la/les destinations adéquates
			try {
				performCopy(preferences.copyDestinations);
			} catch (IOException e) {
				Logger.add("Erreur lors de la copie du fichier vers les destination : " + e.getMessage());
			}
			
			// Mise à jour de BetaSeries
			boolean updateBetaSeries = true;
			if (preferences.betaseriesMarkAsDownloaded) {
				if (preferences.betaseriesLogin == null || preferences.betaseriesPassword == null) {
					Logger.add("Mise à jour de BetaSeries impossible car le nom ou le mot de passe n'est pas renseigné.");
					updateBetaSeries = false;
				}
			} else {
				Logger.add("La série ne sera pas marquée comme téléchargée dans BetaSeries conformément aux préférences.");
				updateBetaSeries = false;
			}
			if (updateBetaSeries) {
				boolean result = markAsDownloaded(preferences.betaseriesLogin, preferences.betaseriesPassword, preferences.showId, title, season, episode); 
				Logger.add("Mise à jour de BetaSeries : showId=" + preferences.showId + ", title=" + title + ", saison=" + season + ", épisode=" + episode + ", résultat=" + result);
			}
		}
		
		// Si les logs sont activées, on les écrit
		if (preferences.isLogEnabled) {
			Path logFile = Paths.get(preferences.workingDirectory, preferences.logDir + title + ".log");
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
		for (String destination : destinations) {
			// Remplacement des variables
			String extendedDestination = destination.replaceAll(TITLE_VAR, title);
			
			Path destinationFile = Paths.get(extendedDestination, sourceFilename);
			
			if (Files.notExists(destinationFile)) {
				Files.createDirectories(Paths.get(extendedDestination));
				
				Logger.add("Copie vers " + destinationFile);
				Files.copy(source, destinationFile);
			} else {
				Logger.add("Le fichier " + destinationFile + " existe déjà.");
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
