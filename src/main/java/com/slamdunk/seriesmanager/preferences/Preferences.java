package com.slamdunk.seriesmanager.preferences;

import static com.slamdunk.seriesmanager.Logger.Levels.ERROR;
import static com.slamdunk.seriesmanager.Logger.Levels.INFO;
import static com.slamdunk.seriesmanager.Logger.Levels.WARN;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;

import com.slamdunk.seriesmanager.Logger;
public class Preferences {
	public static final String SHOW_VAR = "#SHOW#";
	
	public PropertiesPreferences properties;
	
	public String workingDirectory;
	
	public LogPreferences logs;
	public BetaSeriesPreferences betaseries;
	public MappingPreferences mapping;
	
	/**
	 * Lit les mappings et les infos BetaSeries.
	 * @return true si le fichier a pu être chargé et qu'un mapping a été trouvé pour la série indiquée
	 */
	public boolean load(String file, String showTitle) {
		InputStream is;
		try {
			is = new FileInputStream(file);
	        String jsonTxt = IOUtils.toString(is);
	        JSONObject json = (JSONObject)JSONSerializer.toJSON(jsonTxt);
	        
	        // Déduction du répertoire de travail
	        workingDirectory = Paths.get(file).getParent().toString();
	        
	        // Récupération des propriétés
	        properties = new PropertiesPreferences();
	        properties.load(json);
	        
	        // Récupération des préférences de log
	        logs = new LogPreferences();
	        logs.load(json, properties);
	        
	        if (!logs.isValid()) {
	        	Logger.add(ERROR, "Les informations relatives aux logs sont incomplètes. Le traitement ne pourra pas se poursuivre.");
	        	return false;
	        }
	        
	        // Récupération des infos sur l'utilisateur BetaSeries
	        betaseries = new BetaSeriesPreferences();
	        betaseries.load(json);
	        if (!betaseries.isValid()) {
	        	Logger.add(ERROR, "Les informations relatives à BetaSeries sont incomplètes. Le traitement ne pourra pas se poursuivre.");
	        	return false;
	        }
	        
	        // Récupération du mapping par défaut
	        MappingPreferences defaultMapping = new MappingPreferences();
	        if (json.has("default-mapping")) {
		        defaultMapping.load(json.getJSONObject("default-mapping"), properties);
	        }
	        
	        // Récupération du mapping pour la série
	        mapping = null;
	        showTitle = showTitle.toLowerCase();
	        
	        JSONArray mappingsPrefs = json.getJSONArray("mappings");
	        final int nbMappings = mappingsPrefs.size();
	        
	        MappingPreferences loadedMapping = new MappingPreferences();
	        
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
		} catch (JSONException e) {
			Logger.add(ERROR, "Un problème a été rencontré dans le fichier de préférences : " + e.getMessage());
		} catch (FileNotFoundException e) {
			Logger.add(ERROR, "Le fichier de préférences " + file + " n'a pas été trouvé.");
		} catch (IOException e) {
			Logger.add(ERROR, "Erreur lors de la lecture du fichier de préférences (" + e.getMessage() + ").");
		}
		return true;
	}
}
