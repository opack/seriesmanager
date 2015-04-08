package com.slamdunk.seriesmanager.configuration;

import net.sf.json.JSONObject;

public class LogSettings {
	public boolean enabled;
	public String directory;
	
	public void load(JSONObject json, PropertiesSettings properties) {
		if (json.has("logs")) {
			JSONObject logs = json.getJSONObject("logs");
			
			if (logs.has("enabled")) {
				enabled = logs.getBoolean("enabled");
			}
			
			if (logs.has("directory")) {
				directory = logs.getString("directory");
			}
		}
		
		applyDefaults(properties);
	}
	
	private void applyDefaults(PropertiesSettings properties) {
		if (directory == null) {
        	directory = "";
        } else {
        	// Remplace les éventuelles variables
        	directory = properties.replace(directory);
        	
        	// Assure qu'on a bien un / terminal
        	directory += "/";
        }
	}

	/**
	 * Vérifie que tout est correct
	 * @return
	 */
	public boolean isValid() {
		if (enabled) {
			return directory != null;
		}
		
		return true;
	}
}
