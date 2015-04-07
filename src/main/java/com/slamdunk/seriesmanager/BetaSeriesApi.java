package com.slamdunk.seriesmanager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.slamdunk.seriesmanager.NetClientGet.HttpMethods;

public class BetaSeriesApi {
	private static final String API_VERSION = "2.4";
	private static final String API_KEY = "1670ece1c462";
	private static final String API_URL = "http://api.betaseries.com/";
	
	private String authToken;

	/**
	 * Cr�e l'URI permettant de requéter l'API à partir du nom de la méthode
	 * à appeler et des paramètres à envoyer
	 * @param apiMethod Méthode à appeler. Exemple : members/auth
	 * @param parameters Valeurs d'indice pairs = clés, impairs = valeurs.
	 * @return
	 */
	private String createQueryURI(String apiMethod, String... parameters) {
		StringBuilder query = new StringBuilder();
		// Construction de l'URL accédant à la méthode
		query.append(API_URL).append(apiMethod);
		
		// Ajout de la version et clé de l'api
		query.append("?v=").append(API_VERSION);
		query.append("&key=").append(API_KEY);
		
		// Ajout du token d'identification
		if (authToken != null) {
			query.append("&token=").append(authToken);
		}
		
		// Ajout des paramètres
		for (int curParameter = 0; curParameter < parameters.length; curParameter += 2) {
			query.append("&").append(parameters[curParameter]);
			query.append("=").append(parameters[curParameter + 1]);
		}
		return query.toString();
	}
	
	/**
	 * Authentifie l'utilisateur
	 * (Méthode POST members/auth)
	 * @param login
	 * @param password
	 * @return token
	 */
	public String auth(String login, String password) {
		// Crée la requête
		String query = createQueryURI(
			"members/auth",
			"login", login,
			"password", password);
		
		// Appelle le serveur
		String response = NetClientGet.queryServer(HttpMethods.POST, query);
		
		// Traite la réponse
		JSONObject json = (JSONObject) JSONSerializer.toJSON(response);
		authToken = json.getString("token");
		return authToken;
	}
	
	/**
	 * Retourne l'id d'une série
	 * (Méthode GET shows/search)
	 * @param title
	 * @return id
	 */
	public String getShowId(String title) {
		// Crée la requête
		String query;
		try {
			query = createQueryURI(
				"shows/search",
				"title", URLEncoder.encode(title, "UTF-8"),
				"summary", "true");
		} catch (UnsupportedEncodingException e) {
			Logger.add("Impossible de récupérer l'id du show " + title + " car l'encodage UTF-8 n'est pas supporté. " + e.getMessage());
			return "";
		}
		
		// Appelle le serveur
		String response = NetClientGet.queryServer(HttpMethods.GET, query);
		
		// Traite la réponse
		JSONObject json = (JSONObject) JSONSerializer.toJSON(response);
		JSONArray shows = json.getJSONArray("shows");
		
		if (shows.isEmpty()) {
			return null;
		}
		
		JSONObject firstSeries = (JSONObject)shows.get(0);
		return firstSeries.getString("id");
	}
	
	/**
	 * Récupère l'identifiant d'un épisode
	 * (Méthode GET episodes/search)
	 * @param showId
	 * @param season
	 * @param episode
	 * @return id
	 */
	public String getEpisodeId(String showId, int season, int episode) {
		// Crée la requête
		String query = createQueryURI(
			"episodes/search",
			"show_id", showId,
			"number", "S" + season + "E" + episode);
		
		// Appelle le serveur
		String response = NetClientGet.queryServer(HttpMethods.GET, query);
		
		// Traite la réponse
		JSONObject json = (JSONObject) JSONSerializer.toJSON(response);
		JSONObject jsonEpisode = json.getJSONObject("episode");
		return jsonEpisode.getString("id");
	}
	
	/**
	 * Marque un épisode comme téléchargé ou non-téléchargé
	 * (Méthode POST/DELETE episodes/downloaded)
	 * @param episodeId
	 * @param downloaded
	 * @return true si l'épisode a correctement été marqué
	 */
	public boolean setEpisodeDownloaded(String episodeId, boolean downloaded) {
		// Crée la requête
		String query = createQueryURI(
			"episodes/downloaded",
			"id", episodeId);
		
		// Appelle le serveur
		String response;
		if (downloaded) {
			response = NetClientGet.queryServer(HttpMethods.POST, query);
		} else {
			response = NetClientGet.queryServer(HttpMethods.DELETE, query);
		}
		
		// Traite la réponse
		JSONObject json = (JSONObject) JSONSerializer.toJSON(response);
		JSONObject episode = json.getJSONObject("episode");
		JSONObject user = episode.getJSONObject("user");
		return user.getBoolean("downloaded") == downloaded;
	}
	
	public static void main(String[] args) {
		final String login = "Opack";
		final String password = "b5faf6b6d45a9aa4ec610f1699190a5a";

		BetaSeriesApi betaSeries = new BetaSeriesApi();
		
		betaSeries.auth(login, password);
		String showId = betaSeries.getShowId("banshee");
		String episodeId = betaSeries.getEpisodeId(showId, 2, 2);
		betaSeries.setEpisodeDownloaded(episodeId, true);
	}
}
