package com.myapp.store;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

	public class StoreHttpServer {

		private HttpServer httpServer;

		public StoreHttpServer(int port, String context, HttpHandler handler) {
			try {
				//Create HttpServer which is listening on the given port 
				httpServer = HttpServer.create(new InetSocketAddress(port), 0);
				//Create a new context for the given context and handler
				httpServer.createContext(context, handler);
				//Create a default executor
				httpServer.setExecutor(null);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		public void start() {
			this.httpServer.start();
		}

	}