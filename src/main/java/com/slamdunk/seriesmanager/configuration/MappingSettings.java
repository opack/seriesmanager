package com.slamdunk.seriesmanager.configuration;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class MappingSettings {

	public String showName;
	public String betaseriesShowId;
	
	public String includeFilter;
	public String excludeFilter;
	
	public List<String> copyDestinations;
	
	public MappingSettings() {
		copyDestinations = new ArrayList<String>();
	}
	
	public void reset() {
		showName = null;
		betaseriesShowId = null;
		
		includeFilter = null;
		excludeFilter = null;
		
		copyDestinations.clear();
	}
	
	public void load(JSONObject jsonMapping, PropertiesSettings properties) {
		load(jsonMapping, null, properties);
	}
	
	/**
	 * Charge le mapping indiqué sans fournir de valeurs par défaut
	 * ni faire de tests de validité
	 * @param jsonMapping
	 * @param properties 
	 */
	public void load(JSONObject jsonMapping, MappingSettings defaultValues, PropertiesSettings properties) {
		if (jsonMapping.has("show")) {
			showName = jsonMapping.getString("show");
		}
		
		if (jsonMapping.has("betaseries-id")) {
    		betaseriesShowId = jsonMapping.getString("betaseries-id");
    	}
		
		if (jsonMapping.has("title-includes")) {
    		includeFilter = jsonMapping.getString("title-includes");
    	}
		
		if (jsonMapping.has("title-excludes")) {
        	excludeFilter = jsonMapping.getString("title-excludes");
    	}
		
		if (jsonMapping.has("copy-to")) {
        	JSONArray destinations = jsonMapping.getJSONArray("copy-to");
        	for (int curDest = 0; curDest < destinations.size(); curDest++) {
        		String destination = destinations.getString(curDest);
        		String propsReplaced = properties.replace(destination);
        		copyDestinations.add(propsReplaced);
        	}
    	}
		
		if (defaultValues != null) {
			applyDefaults(defaultValues);
		}
	}
	
	private void applyDefaults(MappingSettings defaultMapping) {
		if (includeFilter == null
		&& defaultMapping.includeFilter != null) {
			includeFilter = defaultMapping.includeFilter;
//			Logger.add(INFO, "Champ title-includes manquant pour " + showName + ". Utilisation du mapping par défaut.");
		}
		
		if (excludeFilter == null
		&& defaultMapping.excludeFilter != null) {
			excludeFilter = defaultMapping.excludeFilter;
//			Logger.add(INFO, "Champ title-excludes non-défini pour " + showName + ". Utilisation du mapping par défaut.");
		}
		
		// Ajout des mappings par défaut, même si on en a définit
		if (!defaultMapping.copyDestinations.isEmpty()) {
			copyDestinations.addAll(defaultMapping.copyDestinations);
//			Logger.add(INFO, "Champ copy-to non-défini pour " + showName + ". Utilisation du mapping par défaut.");
		}
	}

	/**
	 * Vérifie la validité du mapping chargé et corrige les valeurs manquantes
	 * en appliquant les valeurs par défaut
	 * @return
	 */
	public boolean isValid() {
		// Le nom du show est obligatoire
		if (showName == null) {
//    		Logger.add(WARN, "Champ show manquant. Mapping invalide.");
    		return false;
    	}
		
		// Vérifie la présence d'un filtre d'inclusion
		if (includeFilter == null) {
//			Logger.add(WARN, "Champ title-includes manquant pour " + showName + " et aucun mapping par défaut défini. Mapping invalide.");
			return false;
		}
		
		// Tous les champs obligatoires sont renseignés
		return true;
	}
	
	/**
	 * Indique si le mapping correspond au titre
	 * @param showTitle Titre du show reçu, en minuscules
	 * @return
	 */
	public boolean matches(String showTitle) {
		// Vérification du filtre d'inclusion
		
		// Remplace les variables
		String filter = includeFilter.replaceAll(Settings.SHOW_VAR, showName).toLowerCase();
		
		// Vérification de la présence du filtre dans le titre
		if (!showTitle.contains(filter)) {
    		return false;
    	}

		// Vérification du filtre d'exclusion
		if (excludeFilter != null) {
			// Remplace les variables
			filter = excludeFilter.replaceAll(Settings.SHOW_VAR, showName).toLowerCase();
			
			// Vérification de l'absence du filtre dans le titre
	    	if (showTitle.contains(filter)) {
	    		return false;
	    	}
		}
    	
    	// Tous les filtres sont respectés
    	return true;
	}

}
