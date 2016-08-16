package com.luxoft.store;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class ConfigLoggers {
	public ConfigLoggers() {

		try {
			Logger la = Logger.getLogger("LogA");

			la.addHandler(new ConsoleHandler());

			try {
				la.addHandler(new FileHandler("price.log"));
	
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}

	}
}
