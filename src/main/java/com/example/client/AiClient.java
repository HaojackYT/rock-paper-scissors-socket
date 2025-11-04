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
 * Client liên kết AI nhận diện cử chỉ tay (Kéo Búa Bao) với Game Server NIO.
 * Sử dụng OkHttp để gọi API AI và Jackson để phân tích JSON.
 */
public class AiClient {
    private final String gameHost;
    private final int gamePort;
    private final String aiUrl;
    
    // HTTP Client cho API AI
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper; // Jackson cho JSON

    private SocketChannel gameChannel;
    private final Charset charset = Charset.forName("UTF-8");

    public AiClient(String gameHost, int gamePort, String aiUrl) {
        this.gameHost = gameHost;
        this.gamePort = gamePort;
        this.aiUrl = aiUrl;
        this.okHttpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // Lớp để parse JSON response từ AI API (Jackson POJO)
    private static class AiResponse {
        public String gesture; // "KÉO", "BÚA", "BAO", "KHÔNG RÕ", "Không thấy tay"
        
        // Cần getters/setters cho Jackson
        public String getGesture() { return gesture; }
        public void setGesture(String gesture) { this.gesture = gesture; }
    }

    /**
     * Chuyển đổi cử chỉ tiếng Việt từ AI sang lệnh tiếng Anh cho Game Server.
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
                return null; // Cử chỉ không hợp lệ
        }
    }

    /**
     * Gửi ảnh tới AI API và nhận về cử chỉ (sử dụng OkHttp cho Multipart POST).
     */
    private String predictGestureFromImage(String imagePath) throws IOException {
        File file = new File(imagePath);
        if (!file.exists()) {
            throw new IOException("File ảnh không tồn tại: " + imagePath);
        }

        // Đọc loại media (MIME type) của file
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream"; // Mặc định nếu không tìm thấy
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
                 // Nếu lỗi 4xx/5xx, in ra chi tiết lỗi
                 String errorBody = response.body() != null ? response.body().string() : "No body";
                 throw new IOException("Lỗi gọi AI API (" + response.code() + "): " + errorBody);
            }

            String responseBody = response.body().string();
            AiResponse aiResponse = objectMapper.readValue(responseBody, AiResponse.class);
            return aiResponse.gesture;
        }
    }

    /**
     * Gửi lệnh tới Game Server thông qua SocketChannel.
     */
    private void sendLineToGameServer(String line) throws IOException {
        if (gameChannel == null || !gameChannel.isOpen()) throw new IOException("Không kết nối tới game server");
        ByteBuffer out = ByteBuffer.wrap((line + "\n").getBytes(charset));
        while (out.hasRemaining()) gameChannel.write(out);
        System.out.println("[Client] Gửi tới Game Server: " + line);
    }

    /**
     * Luồng chạy nền lắng nghe phản hồi từ Game Server.
     */
    private void readerLoop() {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        try {
            while (gameChannel.isOpen()) {
                buf.clear();
                int r = gameChannel.read(buf);
                if (r == -1) {
                    System.out.println("Server game đóng kết nối. Đang thoát.");
                    gameChannel.close();
                    break;
                }
                if (r == 0) continue;
                buf.flip();
                String s = charset.decode(buf).toString();
                
                // In phản hồi từ Server
                String[] lines = s.split("\\r?\\n");
                for (String l : lines) {
                    if (!l.isBlank()) System.out.println("[Game Server] " + l);
                }
            }
        } catch (IOException e) {
            System.out.println("Lỗi luồng đọc Game Server: " + e.getMessage());
        }
    }

    public void start() throws IOException {
        // 1. Kết nối Game Server
        gameChannel = SocketChannel.open();
        gameChannel.connect(new InetSocketAddress(gameHost, gamePort));
        gameChannel.configureBlocking(true); // Client chặn cho đơn giản
        System.out.println("--- Đã kết nối Game Server " + gameHost + ":" + gamePort + " ---");

        // Bắt đầu luồng đọc phản hồi từ Game Server
        Thread reader = new Thread(this::readerLoop);
        reader.setDaemon(true);
        reader.start();

        // 2. Vòng lặp chính để gửi lệnh JOIN và MOVE
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Nhập tên người chơi: ");
            String name = scanner.nextLine().trim();
            sendLineToGameServer("JOIN:" + name);

            while (gameChannel.isOpen()) {
                System.out.println("\n------------------------------------------------------");
                System.out.println("Nhập đường dẫn ảnh cử chỉ (ví dụ: C:/path/to/rock.jpg) (hoặc 'quit'):");
                String imagePath = scanner.nextLine().trim();

                if (imagePath.equalsIgnoreCase("quit") || imagePath.equalsIgnoreCase("exit")) {
                    System.out.println("Tạm biệt. Đóng kết nối...");
                    gameChannel.close();
                    break;
                }

                try {
                    // Gọi API AI
                    System.out.println("Đang gửi ảnh đến AI API...");
                    String vietnameseGesture = predictGestureFromImage(imagePath);
                    System.out.println("AI nhận diện: " + vietnameseGesture);

                    // Chuyển đổi và gửi lệnh MOVE tới Game Server
                    String gameMove = translateGesture(vietnameseGesture);

                    if (gameMove != null) {
                        sendLineToGameServer("MOVE:" + gameMove);
                    } else if (vietnameseGesture.equalsIgnoreCase("KHÔNG RÕ") || vietnameseGesture.equalsIgnoreCase("Không thấy tay")) {
                        System.out.println("!!! AI không nhận diện được cử chỉ. Vui lòng chụp lại ảnh rõ ràng hơn.");
                    } else {
                        System.out.println("!!! Cử chỉ trả về không hợp lệ: " + vietnameseGesture);
                    }

                } catch (Exception e) {
                    System.err.println("!!! Lỗi khi xử lý ảnh hoặc gọi API AI: " + e.getMessage());
                    System.err.println("!!! Đảm bảo AI Server (Python) đang chạy tại " + aiUrl);
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
        String aiUrl = "http://localhost:8000"; // Địa chỉ API AI (Python)

        if (args.length >= 1) gameHost = args[0];
        if (args.length >= 2) {
            try {
                gamePort = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {}
        }

        System.out.println("--- Khởi động AiClient ---");
        System.out.println("Game Server: " + gameHost + ":" + gamePort);
        System.out.println("AI API: " + aiUrl);
        
        new AiClient(gameHost, gamePort, aiUrl).start();
    }
}