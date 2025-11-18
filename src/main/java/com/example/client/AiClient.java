package com.example.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Scanner;

/**
 * Client links the Hand Gesture Recognition AI (Rock, Paper, Scissors) with a NIO Game Server.
 * Uses OkHttp for AI API calls and Jackson for JSON parsing.
 */
public class AiClient {
    private final String gameHost;
    private final int gamePort;
    private final String aiUrl;
    
    // HTTP Client for AI API
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper; // Jackson for JSON

    private SocketChannel gameChannel;
    private final Charset charset = Charset.forName("UTF-8");

    public AiClient(String gameHost, int gamePort, String aiUrl) {
        this.gameHost = gameHost;
        this.gamePort = gamePort;
        this.aiUrl = aiUrl;
        this.okHttpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // Class to parse the JSON response from the AI API (Jackson POJO)
    private static class AiResponse {
        // "KÉO" (SCISSORS), "BÚA" (ROCK), "BAO" (PAPER), "KHÔNG RÕ" (UNCLEAR), "Không thấy tay" (NO HAND)
        public String gesture; 
        
        // Needed for Jackson deserialization
        public String getGesture() { return gesture; }
        public void setGesture(String gesture) { this.gesture = gesture; }
    }

    /**
     * Converts the Vietnamese gesture from the AI to the English command for the Game Server.
     */
    private String translateGesture(String vietnameseGesture) {
        if (vietnameseGesture == null) return null;
        switch (vietnameseGesture.toUpperCase(Locale.ROOT)) {
            case "BÚA":
                return "ROCK";
            case "BAO":
                return "PAPER";
            case "KÉO":
                return "SCISSORS";
            default:
                return null; // Invalid gesture
        }
    }

    /**
     * Sends the image to the AI API and receives the gesture (using OkHttp for Multipart POST).
     */
    private String predictGestureFromImage(String imagePath) throws IOException {
        File file = new File(imagePath);
        if (!file.exists()) {
            throw new IOException("Image file not found: " + imagePath);
        }

        // Read the media type (MIME type) of the file
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream"; // Default if not found
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse(mimeType))) 
                .build();

        Request request = new Request.Builder()
                .url(aiUrl + "/predict")
                .post(requestBody)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                 // If 4xx/5xx error, print detailed error
                 String errorBody = response.body() != null ? response.body().string() : "No body";
                 throw new IOException("AI API call failed (" + response.code() + "): " + errorBody);
            }

            String responseBody = response.body().string();
            AiResponse aiResponse = objectMapper.readValue(responseBody, AiResponse.class);
            return aiResponse.gesture;
        }
    }

    /**
     * Sends a command line to the Game Server via SocketChannel.
     */
    private void sendLineToGameServer(String line) throws IOException {
        if (gameChannel == null || !gameChannel.isOpen()) throw new IOException("Not connected to game server"); 
        ByteBuffer out = ByteBuffer.wrap((line + "\n").getBytes(charset));
        while (out.hasRemaining()) gameChannel.write(out);
        System.out.println("[Client] Sent to Game Server: " + line); 
    }

    /**
     * Background thread loop to listen for responses from the Game Server.
     */
    private void readerLoop() {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        try {
            while (gameChannel.isOpen()) {
                buf.clear();
                int r = gameChannel.read(buf);
                if (r == -1) {
                    System.out.println("Game server closed connection. Exiting.");
                    gameChannel.close();
                    break;
                }
                if (r == 0) continue;
                buf.flip();
                String s = charset.decode(buf).toString();
                
                // Print response from Server
                String[] lines = s.split("\\r?\\n");
                for (String l : lines) {
                    if (!l.isBlank()) System.out.println("[Game Server] " + l); 
                }
            }
        } catch (IOException e) {
            System.out.println("Game Server read loop error: " + e.getMessage()); 
        }
    }

    public void start() throws IOException {
        // 1. Connect to Game Server
        gameChannel = SocketChannel.open();
        gameChannel.connect(new InetSocketAddress(gameHost, gamePort));
        gameChannel.configureBlocking(true); // Blocking client for simplicity
        System.out.println("--- Connected to Game Server " + gameHost + ":" + gamePort + " ---"); 

        // Start the thread to read responses from the Game Server
        Thread reader = new Thread(this::readerLoop);
        reader.setDaemon(true);
        reader.start();

        // 2. Main loop to send JOIN and MOVE commands
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter player name: "); 
            String name = scanner.nextLine().trim();
            sendLineToGameServer("JOIN:" + name);

            while (gameChannel.isOpen()) {
                System.out.println("\n------------------------------------------------------");
                System.out.println("Enter gesture image path (e.g., C:/path/to/rock.jpg) (or 'quit'):"); 
                String imagePath = scanner.nextLine().trim();

                if (imagePath.equalsIgnoreCase("quit") || imagePath.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye. Closing connection..."); 
                    gameChannel.close();
                    break;
                }

                try {
                    // Call the AI API
                    System.out.println("Sending image to AI API..."); 
                    String vietnameseGesture = predictGestureFromImage(imagePath);
                    System.out.println("AI recognized: " + vietnameseGesture); 

                    // Translate and send the MOVE command to the Game Server
                    String gameMove = translateGesture(vietnameseGesture);

                    if (gameMove != null) {
                        sendLineToGameServer("MOVE:" + gameMove);
                    } else if (vietnameseGesture.equalsIgnoreCase("KHÔNG RÕ") || vietnameseGesture.equalsIgnoreCase("Không thấy tay")) {
                        System.out.println("!!! AI could not recognize the gesture. Please take a clearer picture."); 
                    } else {
                        System.out.println("!!! Invalid gesture returned: " + vietnameseGesture); 
                    }

                } catch (Exception e) {
                    System.err.println("!!! Error processing image or calling AI API: " + e.getMessage()); 
                    System.err.println("!!! Ensure AI Server (Python) is running at " + aiUrl); 
                }
            }
        } finally {
            if (gameChannel != null && gameChannel.isOpen()) {
                gameChannel.close();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        String gameHost = "localhost";
        int gamePort = 5000;
        String aiUrl = "http://localhost:8000"; 

        // Parse arguments for host and port if provided
        if (args.length >= 1) gameHost = args[0];
        if (args.length >= 2) {
            try {
                gamePort = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {}
        }

        System.out.println("--- Starting AiClient ---"); 
        System.out.println("Game Server: " + gameHost + ":" + gamePort); 
        System.out.println("AI API: " + aiUrl); 
        
        new AiClient(gameHost, gamePort, aiUrl).start();
    }
}