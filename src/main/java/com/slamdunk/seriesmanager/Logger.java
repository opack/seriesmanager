package com.slamdunk.seriesmanager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Logger {
	private static final List<String> LOGS = new ArrayList<String>();
	
	public enum Levels {
		INFO,
		WARN,
		ERROR;
	}
	
	public static void add(Levels level, String message) {
		LOGS.add(level.name() + " : " + message);
	}
	
	public static void flushToFile(Path file) {
		try {
			// Crée le répertoire de logs si nécessaire
			Files.createDirectories(file.getParent());
			
			// Ecrit les logs
			Files.write(
				file,
				LOGS,
				Charset.defaultCharset(),
				StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			
			// Vide les logs
			LOGS.clear();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
