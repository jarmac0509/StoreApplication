package com.luxoft.store;

public class HttpServerTest {
	private static final String CONTEXT = "/";
	private static final int PORT = 8000;

	public static void main(String[] args) throws Exception {

		// Create a new SimpleHttpServer
		StoreHttpServer storeHttpServer = new StoreHttpServer(PORT, CONTEXT,
				new HttpRequestHandler());

		// Start the server
		storeHttpServer.start();
		System.out.println("Server is started and listening on port "+ PORT);
	}

}


