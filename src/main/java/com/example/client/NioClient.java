package com.example.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Simple client that connects to the NIO server using SocketChannel (blocking mode for simplicity),
 * reads server lines on a background thread and sends commands typed by user.
 */
public class NioClient {
    private final String host;
    private final int port;
    private SocketChannel channel;
    private final Charset charset = Charset.forName("UTF-8");
    private MessageListener messageListener;

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public NioClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(host, port));
        channel.configureBlocking(true); // Simple blocking client
        System.out.println("Connected to server " + host + ":" + port);

        // Start reader thread
        Thread reader = new Thread(this::readerLoop);
        reader.setDaemon(true);
        reader.start();

        // Main input loop
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter your name: ");
            String name = scanner.nextLine().trim();
            sendLine("JOIN:" + name);

            while (true) {
                System.out.println("Enter move (rock, paper, scissors) or 'quit':");
                String line = scanner.nextLine().trim();
                if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                    System.out.println("Bye");
                    channel.close();
                    break;
                }
                // Allow convenient input like r/p/s
                String normalized = normalizeMove(line);
                if (normalized != null) {
                    sendLine("MOVE:" + normalized);
                } else {
                    System.out.println("Unknown command. Type rock/paper/scissors or quit");
                }
            }
        }
    }

    private void readerLoop() {
    ByteBuffer buf = ByteBuffer.allocate(1024);
    try {
        while (channel.isOpen()) {
            buf.clear();
            int r = channel.read(buf);
            if (r == -1) {
                System.out.println("Server closed connection");
                channel.close();
                break;
            }
            if (r == 0) continue;
            buf.flip();
            String s = charset.decode(buf).toString();
            // Print server lines
            String[] lines = s.split("\\r?\\n");
            for (String l : lines) {
                if (!l.isBlank()) {
                    System.out.println("[Server] " + l);

                    
                    if (messageListener != null) {
                        messageListener.onMessage(l);
                    }
                }
            }
        }
    } catch (IOException e) {
        System.out.println("Read thread error: " + e.getMessage());
    }
}


    public void sendLine(String line) throws IOException {
        if (channel == null || !channel.isOpen()) throw new IOException("Not connected");
        ByteBuffer out = ByteBuffer.wrap((line + "\n").getBytes(charset));
        while (out.hasRemaining()) channel.write(out);
    }

    private String normalizeMove(String in) {
        String s = in.trim().toLowerCase();
        return switch (s) {
            case "r", "rock" -> "ROCK";
            case "p", "paper" -> "PAPER";
            case "s", "scissors", "scissor" -> "SCISSORS";
            default -> null;
        };
    }

    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 5000;
        if (args.length >= 1) host = args[0];
        if (args.length >= 2) port = Integer.parseInt(args[1]);
        new NioClient(host, port).start();
    }
}