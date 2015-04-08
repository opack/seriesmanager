package com.slamdunk.seriesmanager.configuration;

import net.sf.json.JSONObject;

public class BetaSeriesSettings {
	public String login;
	public String password;
	
	public boolean markAsDownloaded;
	
	/**
	 * Charge les valeurs depuis le json
	 * @param json
	 */
	public void load(JSONObject json) {
		if (!json.has("betaseries")) {
			return;
		}
		JSONObject betaseries = json.getJSONObject("betaseries");
		
		if (betaseries.has("login")) {
			login = betaseries.getString("login");
		}
		if (betaseries.has("md5-password")) {
			password = betaseries.getString("md5-password");
		}
		if (betaseries.has("markAsDownloaded")) {
			markAsDownloaded = betaseries.getBoolean("markAsDownloaded");
		}
	}
	
	/**
	 * Vérifie si les valeurs contenues sont correctes.
	 * Typiquement, un login et mdp doivent être présents si
	 * markAsDownloaded = true.
	 * @return
	 */
	public boolean isValid() {
		// S'il faut marquer la série comme téléchargée dans BetaSeries,
		// alors il faut absolument qu'on ait un login et mdp
		if (markAsDownloaded) {
			return login != null && !login.isEmpty()
				&& password != null && !password.isEmpty();
		}
		
		// Sinon, on se fiche de ces préférences
		return true;
	}
}
