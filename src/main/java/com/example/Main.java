package com.example;

/**
 * Placeholder Main that can start server (or be extended). For convenience you can run:
 * java -cp target/classes com.example.Main server [port]
 * or
 * java -cp target/classes com.example.client.NioClient [host] [port]
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length >= 1 && args[0].equalsIgnoreCase("server")) {
            int port = 5000;
            if (args.length >= 2) port = Integer.parseInt(args[1]);
            com.example.server.NioServer.main(new String[]{String.valueOf(port)});
        } else {
            System.out.println("To start server: java -cp ... com.example.Main server [port]");
            System.out.println("To start client: java -cp ... com.example.client.NioClient [host] [port]");
        }
    }
}