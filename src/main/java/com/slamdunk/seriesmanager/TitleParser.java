package com.slamdunk.seriesmanager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TitleParser {
	/**
	 * Pattern pour un fichier au format *[show_name].S[season]E[episode].*
	 */
	private static final Pattern SERIES_TITLE_PATTERN_1 = Pattern.compile("(.*)\\.S(\\d?\\d)E(\\d\\d).*", Pattern.CASE_INSENSITIVE);

	/**
	 * Pattern pour un fichier au format *[show_name].[season]x[episode].*
	 */
	private static final Pattern SERIES_TITLE_PATTERN_2 = Pattern.compile("(.*)\\.(\\d?\\d)x(\\d\\d).*", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Pattern pour un fichier au format *[show_name].[season][episode].*
	 */
	private static final Pattern SERIES_TITLE_PATTERN_3 = Pattern.compile("(.*)\\.(\\d?\\d)(\\d\\d).*", Pattern.CASE_INSENSITIVE);
	
	public String show;
	public int season;
	public int episode;
	
	/**
	 * Extrait le nom de la série, le numéro de la saison et de l'épisode
	 * du titre de téléchargement spécifié
	 * @param title
	 * @return
	 */
	public boolean parse(String title) {
		// Vérifie le format du nom de fichier
		Matcher m = SERIES_TITLE_PATTERN_1.matcher(title);
		if (!m.matches()) {
			m = SERIES_TITLE_PATTERN_2.matcher(title);
			if (!m.matches()) {
				m = SERIES_TITLE_PATTERN_3.matcher(title);
				if (!m.matches()) {
					return false;
				}
			}
		}
		
		// Extraction du nom de la série et du numéro de l'épisode
		show = m.group(1).replaceAll("\\.", " ");
		season = Integer.parseInt(m.group(2));
		episode = Integer.parseInt(m.group(3));
		
		return true;
	}

	/**
	 * Retourne le nom du fichier vidéo correspondant au titre parsé
	 * contenu dans le répertoire indiqué
	 * @param directory
	 * @return
	 */
	public String locateVideoFile(String directory) {
		File dir = new File(directory);
		
		// Récupère les noms des fichiers vidéo
		String[] videos = dir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
					// Vérifie l'extension
				return (name.endsWith(".mp4") || name.endsWith(".avi"))
					// Evite les fichiers échantillon
					&& !name.contains("sample")
					// Vérifie la saison et l'épisode
					&& matchesParsedTitle(name);
			}
		});
		
		// Retourne le premier fichier qui correspond
		if (videos != null
		&& videos.length > 0) {
			return videos[0];
		}
		
		// On n'a rien trouvé
		return null;
	}
	
	/**
	 * Indique si le nom de fichier spécifié correspond au titre parsé
	 * (nom de série, numéro de saison et d'épisode)
	 * @param name
	 * @return
	 */
	public boolean matchesParsedTitle(String filename) {
		TitleParser test = new TitleParser();
		test.parse(filename);
		
		return test.show.equalsIgnoreCase(show)
			&& test.season == season
			&& test.episode == episode;
	}
}
