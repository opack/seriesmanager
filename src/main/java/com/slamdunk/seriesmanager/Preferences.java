package com.slamdunk.seriesmanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;

public class Preferences {
	public String workingDirectory;
	
	public boolean isLogEnabled;
	public String logDir;
	
	public String betaseriesLogin;
	public String betaseriesPassword;
	public boolean betaseriesMarkAsDownloaded;
	
	public String showId;
	
	public List<String> copyDestinations;
	
	public Preferences() {
		copyDestinations = new ArrayList<String>();
	}
	
	/**
	 * Lit les mappings et les infos BetaSeries.
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void load(String file, String showTitle) {
		InputStream is;
		try {
			is = new FileInputStream(file);
	        String jsonTxt = IOUtils.toString(is);
	        JSONObject json = (JSONObject)JSONSerializer.toJSON(jsonTxt);
	        
	        // Déduction du répertoire de travail
	        workingDirectory = Paths.get(file).getParent().toString();
	        
	        // Récupération des préférences de log
	        JSONObject logsPrefs = json.getJSONObject("logs");
	        isLogEnabled = logsPrefs.getBoolean("enabled");
	        logDir = logsPrefs.getString("directory");
	        if (logDir == null) {
	        	logDir = "";
	        } else {
	        	logDir += "/";
	        }
	        
	        // Récupération des infos sur l'utilisateur BetaSeries
	        JSONObject betaseriesPrefs = json.getJSONObject("betaseries");
	        betaseriesLogin = betaseriesPrefs.getString("login");
	        betaseriesPassword = betaseriesPrefs.getString("md5-password");
	        betaseriesMarkAsDownloaded = betaseriesPrefs.getBoolean("markAsDownloaded");
	        
	        // Récupération des mappings pour les séries
	        JSONObject mappingsPrefs = json.getJSONObject("mappings");
	        JSONObject mapping = (JSONObject)mappingsPrefs.get(showTitle.toLowerCase());
	        if (mapping == null) {
	        	Logger.add("Série inexistante dans les mappings : " + showTitle);
	        } else {
	        	if (mapping.has("showId")) {
	        		showId = mapping.getString("showId");
	        	}
	        	JSONArray destinations = mapping.getJSONArray("destinations");
	        	for (int curDest = 0; curDest < destinations.size(); curDest++) {
	        		copyDestinations.add(destinations.getString(curDest));
	        	}
	        }
		} catch (JSONException e) {
			Logger.add("Un problème a été rencontré dans le fichier de préférences : " + e.getMessage());
		} catch (FileNotFoundException e) {
			Logger.add("Le fichier de préférences " + file + " n'a pas été trouvé.");
		} catch (IOException e) {
			Logger.add("Erreur lors de la lecture du fichier de préférences (" + e.getMessage() + ").");
		}
	}
}
