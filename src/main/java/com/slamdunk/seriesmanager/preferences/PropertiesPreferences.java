package com.slamdunk.seriesmanager.preferences;

import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PropertiesPreferences {
	public Map<String, String> properties;

	public void load(JSONObject json) {
		JSONArray propertiesPrefs = json.getJSONArray("properties");
        final int nbProperties = propertiesPrefs.size();
        
        properties = new LinkedHashMap<String, String>();
        
        for (int curProperty = 0; curProperty < nbProperties; curProperty++) {
        	JSONObject jsonProperty = propertiesPrefs.getJSONObject(curProperty);
        	if (jsonProperty.has("key")
        	&& jsonProperty.has("value")) {
        		properties.put(jsonProperty.getString("key"), jsonProperty.getString("value"));
        	}
        }
	}
	
	/**
	 * Remplace toutes les propriétés mentionnées dans la chaine source
	 * au format %PROPERTY_KEY% par leur valeur. Attention ! L'ordre
	 * de remplacement est celui dans lequel se trouvent les propriétés.
	 * @param source
	 * @return
	 */
	public String replace(String source) {
		String result = source;
		for (Map.Entry<String, String> property : properties.entrySet()) {
			result = result.replaceAll("%" + property.getKey() + "%", property.getValue());
		}
		return result;
	}
}
