package com.luxoft.store;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import au.com.bytecode.opencsv.CSVWriter;

public class HttpRequestHandler implements HttpHandler {
	Map<String, String> keys = new HashMap<>();
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
				logB.log(Level.INFO, "price of "+arr[1]+"="+price);
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

	}

	private String lowPricesList(String productId) {
		String result = "";
		for (String s : store.keySet()) {
			result += s + "=";
			result += store.get(s).get(productId) + ",";
		}
		System.out.println("result"+result);
		save(result);
		
		return result;
	}

	private void save(String result) {

		System.out.println("in save ");
		try {
			CSVWriter writer = new CSVWriter(new FileWriter("results.csv"));
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
		System.out.println("storeId"+storeId);
		System.out.println(store.get(storeId));
		Map<String,Integer>map=store.get(storeId);
		map.put(productId, price);

	}

	private String getStoreId(String sessionKey) {
		return keys.get(sessionKey);
	}

	private boolean validate(String arr) {
		Set<String> keySet = keys.keySet();
		if (!keySet.contains(arr)) {
			return false;
		}
		return true;

	}

	String login(String storeId) {
		System.out.println("in login storeId"+storeId);
		final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		SecureRandom rnd = new SecureRandom();
		StringBuilder sb = new StringBuilder(5);
		for (int i = 0; i < 5; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		keys.put(sb.toString(), storeId);
		return sb.toString();

	}
}
