package com.slamdunk.seriesmanager;

import static com.slamdunk.seriesmanager.Logger.Levels.ERROR;
import static com.slamdunk.seriesmanager.Logger.Levels.INFO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilenameParser {
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
	
	public String title;
	public int season;
	public int episode;
	
	public boolean parse(String filename) {
		// Vérifie le format du nom de fichier
		Matcher m = SERIES_TITLE_PATTERN_1.matcher(filename);
		if (!m.matches()) {
			m = SERIES_TITLE_PATTERN_2.matcher(filename);
			if (!m.matches()) {
				m = SERIES_TITLE_PATTERN_3.matcher(filename);
				if (!m.matches()) {
					Logger.add(ERROR, "Le nom de fichier " + filename + " ne correspond pas aux formats reconnus.");
					return false;
				}
			}
		}
		
		// Extraction du nom de la série et du numéro de l'épisode
		title = m.group(1).replaceAll("\\.", " ");
		season = Integer.parseInt(m.group(2));
		episode = Integer.parseInt(m.group(3));
		
		Logger.add(INFO, "Informations extraites :");
		Logger.add(INFO, "\t\tTitre   : " + title);
		Logger.add(INFO, "\t\tSaison  : " + season);
		Logger.add(INFO, "\t\tEpisode : " + episode);
		
		return true;
	}
}
