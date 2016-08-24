package com.luxoft.store;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import au.com.bytecode.opencsv.CSVWriter;

public class HttpRequestHandler implements HttpHandler {
	Map<String, String> keys = new ConcurrentHashMap<>();
	Map<String, Long> seesionValidity = new ConcurrentHashMap<>();
	Map<String, Map<String, Integer>> store = new HashMap<>();
	String response = "";
	private Logger logB;
	private Logger logger;
	{
		initialize();
	}

	public void handle(HttpExchange t) throws IOException {

		boolean isKeyvalid = false;

		// Create a response form the request query parameters
		URI uri = t.getRequestURI();
		String[] arr = uri.toString().split("[/=]");

		if (arr[2].equals("login"))
			response = login(arr[1]);
		else if (arr[2].equals("price")) {
			isKeyvalid = validate(arr[5]);
			if (isKeyvalid) {
				Integer price = Integer.parseInt(arr[3]);
				logB = Logger.getLogger("LogB");
				try {

					LogManager.getLogManager().readConfiguration(new FileInputStream("logger.properties"));
				} catch (SecurityException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
				logB.log(Level.INFO, "price of " + arr[1] + "=" + price);
				updatePrice(arr[5], arr[1], price);
				response = "price updated";
			}

			else
				response = "not valid key";
		} else if (arr[2].equals("lowpriceslist")) {
			response = lowPricesList(arr[1]);
		}

		t.sendResponseHeaders(200, response.length());

		// Write the response string
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	private void initialize() {
		Map<String, Integer> innerMap = new HashMap<>();
		Map<String, Integer> innerMap2 = new HashMap<>();
		innerMap.put("1", 200);
		innerMap.put("2", 500);
		innerMap.put("3", 1000);
		store.put("1", innerMap);
		innerMap2.put("1", 150);
		innerMap2.put("2", 250);
		store.put("2", innerMap2);

		Thread t = new Thread(new ChceckSession());
		t.start();

	}

	private String lowPricesList(String productId) {
		String result = "";
		int numberOfResults = 0;
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("resources/config.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		numberOfResults = Integer.parseInt(properties.getProperty("topReults"));
		Iterator<String> itr2 = store.keySet().iterator();
		while (numberOfResults > 0) {

			String key = itr2.next();
			result += key + "=";
			result += store.get(key).get(productId) + ",";
			numberOfResults--;

		}
		// for (String s : store.keySet()) {
		// result += s + "=";
		// result += store.get(s).get(productId) + ",";
		// }
		System.out.println("result" + result);
		save(result);

		return result;
	}

	private void save(String result) {
		String csvFileName = "";
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("resources/config.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		csvFileName = properties.getProperty("csvName");
		System.out.println("in save ");
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(csvFileName + ".csv"));
			System.out.println(result);
			String[] results = result.split(",");
			writer.writeNext(results);

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void updatePrice(String sessionKey, String productId, Integer price) {
		String storeId = getStoreId(sessionKey);
		System.out.println("storeId" + storeId);
		System.out.println(store.get(storeId));
		Map<String, Integer> map = store.get(storeId);
		map.put(productId, price);

	}

	private String getStoreId(String sessionKey) {
		return keys.get(sessionKey);
	}

	private boolean validate(String arr) {
		Set<String> keySet = keys.keySet();
		Date date = new Date();
		int timeInMinutes = 0;
		long timeInMiliseconds = 0;
		// System.out.println(TimeUnit.MINUTES.toMillis(15));
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("resources/config.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		timeInMinutes = Integer.parseInt(properties.getProperty("session"));
		timeInMiliseconds = TimeUnit.MINUTES.toMillis(timeInMinutes);
		if (!keySet.contains(arr)) {
			return false;
		}
		if (date.getTime() - timeInMiliseconds > seesionValidity.get(arr)) {
			// System.out.println("in validate");
			return false;
		}
		return true;

	}

	private String login(String storeId) {
		Date date = new Date();

		// System.out.println("in login storeId"+storeId);
		final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		SecureRandom rnd = new SecureRandom();
		StringBuilder sb = new StringBuilder(5);
		for (int i = 0; i < 5; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		keys.put(sb.toString(), storeId);
		seesionValidity.put(sb.toString(), date.getTime());
		return sb.toString();

	}

	class ChceckSession implements Runnable {

		@Override
		public void run() {
			Properties properties = new Properties();
			try {
				properties.load(new FileInputStream("resources/config.properties"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int timeInMinutes = Integer.parseInt(properties.getProperty("session"));
			long timeInMiliseconds = TimeUnit.MINUTES.toMillis(timeInMinutes);
			while (true) {
				Date date = new Date();
				for (String sessionKey : seesionValidity.keySet()) {
					
					long currentTime = date.getTime();
					long sessionStarted = seesionValidity.get(sessionKey);
					 if(currentTime-timeInMiliseconds>sessionStarted){
						 seesionValidity.remove(sessionKey);
						 keys.remove(sessionKey);
						 
					 }
					 try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 System.out.println(keys);
					 System.out.println(seesionValidity);
				}

			}
		}
	}
}
