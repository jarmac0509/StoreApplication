package com.myapp.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class HttpServerTest {
    private static final String CONTEXT = "/";
    private static int PORT = 8000;

    public static void main(String[] args) throws Exception {
        InputStream in = HttpServerTest.class.getClassLoader().getResourceAsStream("config.properties");

        Properties properties = new Properties();
        properties.load(in);
        // Create a new SimpleHttpServer
        //System.out.println(properties.getProperty("serverPort"));
        PORT = Integer.parseInt(properties.getProperty("serverPort"));
        StoreHttpServer storeHttpServer = new StoreHttpServer(PORT, CONTEXT, new HttpRequestHandler());

        // Start the server
        storeHttpServer.start();
        System.out.println("Server is started and listening on port " + PORT);
    }

}


