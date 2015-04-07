package com.slamdunk.seriesmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NetClientGet {
	enum HttpMethods {
		GET,
		POST,
		DELETE,
		PUT;
	}

	public static String queryServer(HttpMethods method, String query) {
		StringBuilder output = new StringBuilder();
		try {
			URL url = new URL(query);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method.name());
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed " + method + " " + query + ": HTTP error code : " + conn.getResponseCode() + " - " + conn.getResponseMessage());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String line;
			while ((line = br.readLine()) != null) {
				output.append(line);
			}

			conn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output.toString();
	}
}