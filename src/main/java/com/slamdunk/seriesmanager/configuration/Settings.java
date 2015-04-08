package com.slamdunk.seriesmanager.configuration;

import static com.slamdunk.seriesmanager.Logger.Levels.ERROR;
import static com.slamdunk.seriesmanager.Logger.Levels.INFO;
import static com.slamdunk.seriesmanager.Logger.Levels.WARN;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;

import com.slamdunk.seriesmanager.Logger;
public class Settings {
	private static final String SETTINGS_FILENAME = "conf/settings.json";
	public static final String SHOW_VAR = "#SHOW#";
	
	public PropertiesSettings properties;
	
	public LogSettings logs;
	public BetaSeriesSettings betaseries;
	public MappingSettings mapping;
	
	/**
	 * Lit les mappings et les infos BetaSeries.
	 * @return true si le fichier a pu être chargé et qu'un mapping a été trouvé pour la série indiquée
	 */
	public boolean load(String homeDirectory, String showTitle) {
		String settingsFile = homeDirectory + "/" + SETTINGS_FILENAME;
		
		InputStream is;
		try {
			is = new FileInputStream(settingsFile);
	        String jsonTxt = IOUtils.toString(is);
	        JSONObject json = (JSONObject)JSONSerializer.toJSON(jsonTxt);
	        
	        // Récupération des propriétés
	        properties = new PropertiesSettings();
	        properties.load(json);
	        
	        // Récupération des préférences de log
	        logs = new LogSettings();
	        logs.load(json, properties);
	        
	        if (!logs.isValid()) {
	        	Logger.add(ERROR, "Les informations relatives aux logs sont incomplètes. Le traitement ne pourra pas se poursuivre.");
	        	return false;
	        }
	        
	        // Récupération des infos sur l'utilisateur BetaSeries
	        betaseries = new BetaSeriesSettings();
	        betaseries.load(json);
	        if (!betaseries.isValid()) {
	        	Logger.add(ERROR, "Les informations relatives à BetaSeries sont incomplètes. Le traitement ne pourra pas se poursuivre.");
	        	return false;
	        }
	        
	        // Récupération du mapping par défaut
	        MappingSettings defaultMapping = new MappingSettings();
	        if (json.has("default-mapping")) {
		        defaultMapping.load(json.getJSONObject("default-mapping"), properties);
	        }
	        
	        // Récupération du mapping pour la série
	        mapping = null;
	        showTitle = showTitle.toLowerCase();
	        
	        JSONArray mappingsPrefs = json.getJSONArray("mappings");
	        final int nbMappings = mappingsPrefs.size();
	        
	        MappingSettings loadedMapping = new MappingSettings();
	        
	        for (int curMapping = 0; curMapping < nbMappings; curMapping++) {
	        	JSONObject jsonMapping = mappingsPrefs.getJSONObject(curMapping);
	        	
	        	// Chargement du mapping
	        	loadedMapping.reset();
	        	loadedMapping.load(jsonMapping, defaultMapping, properties);
	        	
	        	// Application des valeurs par défaut et vérification du format
	        	if (!loadedMapping.isValid()) {
	        		Logger.add(WARN, "Le mapping " + curMapping + " est mal formé et sera ignoré.");
	        		continue;
	        	}
	        	
	        	// Vérification de la correspondance avec le titre
	        	if (loadedMapping.matches(showTitle)) {
	        		mapping = loadedMapping;
	        		break;
	        	}
	        }
	        
	        // Si aucun mapping n'est applicable, on l'indique
	        if (mapping == null) {
	        	Logger.add(INFO, "Aucun mapping n'a été trouvé pour le show " + showTitle + ".");
	        	return false;
	        }
	        
	        return true;
		} catch (JSONException e) {
			Logger.add(ERROR, "Un problème a été rencontré dans le fichier de préférences : " + e.getMessage());
		} catch (FileNotFoundException e) {
			Logger.add(ERROR, "Le fichier de configuration " + settingsFile + " n'a pas été trouvé.");
		} catch (IOException e) {
			Logger.add(ERROR, "Erreur lors de la lecture du fichier de préférences (" + e.getMessage() + ").");
		}
		return false;
	}
}
