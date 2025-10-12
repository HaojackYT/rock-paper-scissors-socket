package com.example.server;

import com.example.model.Gesture;
import com.example.model.Player;
import com.example.model.Result;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Non-blocking NIO server that accepts multiple clients, pairs them into games,
 * and manages simple line-based protocol.
 */
public class NioServer {
    private final int port;
    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final Charset charset = Charset.forName("UTF-8");

    // Waiting players queue
    private final Queue<Player> waiting = new ConcurrentLinkedQueue<>();
    // Map channel -> player
    private final Map<SocketChannel, Player> channelPlayerMap = new HashMap<>();

    public NioServer(int port) throws IOException {
        this.port = port;
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        log("Server started on port " + port);
    }

    public void start() throws IOException {
        while (true) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (!key.isValid()) continue;

                if (key.isAcceptable()) handleAccept(key);
                else if (key.isReadable()) handleRead(key);
                else if (key.isWritable()) handleWrite(key);
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel client = ssc.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
        sendLine(client, "INFO:Welcome to RPS NIO Server. Send JOIN:<name>");
        log("Accepted connection from " + client.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buf = (ByteBuffer) key.attachment();
        try {
            int read = client.read(buf);
            if (read == -1) {
                disconnect(client);
                return;
            }
            buf.flip();
            String data = charset.decode(buf).toString();
            buf.clear();

            // Split by lines in case multiple commands arrived
            String[] lines = data.split("\\r?\\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                processCommand(client, line.trim());
            }
        } catch (IOException e) {
            log("Read error: " + e.getMessage());
            disconnect(client);
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
        // Not used for queued writes in this simple server. Kept for extensibility.
        key.interestOps(SelectionKey.OP_READ);
    }

    private void processCommand(SocketChannel client, String line) throws IOException {
        log("<- " + line + " from " + client.getRemoteAddress());
        if (line.startsWith("JOIN:")) {
            String name = line.substring(5).trim();
            if (name.isEmpty()) sendLine(client, "INFO:Invalid name");
            else registerPlayer(client, name);
        } else if (line.startsWith("MOVE:")) {
            String move = line.substring(5).trim();
            Player p = channelPlayerMap.get(client);
            if (p == null) {
                sendLine(client, "INFO:You must JOIN first");
                return;
            }
            try {
                Gesture g = Gesture.valueOf(move.toUpperCase(Locale.ROOT));
                p.setGesture(g);
                sendLine(client, "INFO:Move recorded: " + g);
                checkAndResolve(p);
            } catch (IllegalArgumentException ex) {
                sendLine(client, "INFO:Unknown move. Use ROCK, PAPER or SCISSORS");
            }
        } else if (line.startsWith("PING")) {
            sendLine(client, "PONG");
        } else {
            sendLine(client, "INFO:Unknown command");
        }
    }

    private void registerPlayer(SocketChannel client, String name) throws IOException {
        Player player = new Player(name, client);
        channelPlayerMap.put(client, player);
        waiting.add(player);
        sendLine(client, "INFO:Registered as " + name);
        tryPairing();
    }

    private void tryPairing() throws IOException {
        while (waiting.size() >= 2) {
            Player p1 = waiting.poll();
            Player p2 = waiting.poll();
            if (p1 == null || p2 == null) break;
            p1.setOpponent(p2);
            p2.setOpponent(p1);

            sendLine(p1.getChannel(), "OPPONENT:" + p2.getName());
            sendLine(p2.getChannel(), "OPPONENT:" + p1.getName());
            sendLine(p1.getChannel(), "READY:Send MOVE:<ROCK|PAPER|SCISSORS>");
            sendLine(p2.getChannel(), "READY:Send MOVE:<ROCK|PAPER|SCISSORS>");
        }
    }

    private void checkAndResolve(Player p) throws IOException {
        Player opp = p.getOpponent();
        if (opp == null) return; // Not matched yet
        if (p.getGesture() != null && opp.getGesture() != null) {
            // Both played
            Result r = Result.fromGestures(p.getGesture(), opp.getGesture());
            // Send results
            if (r == Result.DRAW) {
                sendLine(p.getChannel(), "RESULT:DRAW:You=" + p.getGesture() + ":Opp=" + opp.getGesture());
                sendLine(opp.getChannel(), "RESULT:DRAW:You=" + opp.getGesture() + ":Opp=" + p.getGesture());
            } else if (r == Result.WIN) {
                sendLine(p.getChannel(), "RESULT:WIN:You=" + p.getGesture() + ":Opp=" + opp.getGesture());
                sendLine(opp.getChannel(), "RESULT:LOSE:You=" + opp.getGesture() + ":Opp=" + p.getGesture());
            } else { // LOSE
                sendLine(p.getChannel(), "RESULT:LOSE:You=" + p.getGesture() + ":Opp=" + opp.getGesture());
                sendLine(opp.getChannel(), "RESULT:WIN:You=" + opp.getGesture() + ":Opp=" + p.getGesture());
            }
            // Reset gestures and requeue players for another round
            p.setGesture(null);
            opp.setGesture(null);
            p.setOpponent(null);
            opp.setOpponent(null);
            waiting.add(p);
            waiting.add(opp);
            // Try pair again (might pair them with different players)
            tryPairing();
        }
    }

    private void sendLine(SocketChannel channel, String line) throws IOException {
        if (!channel.isOpen()) return;
        ByteBuffer out = ByteBuffer.wrap((line + "\n").getBytes(charset));
        while (out.hasRemaining()) channel.write(out);
        log("-> " + line + " to " + channel.getRemoteAddress());
    }

    private void disconnect(SocketChannel client) {
        try {
            Player p = channelPlayerMap.remove(client);
            if (p != null) {
                waiting.remove(p);
                Player opp = p.getOpponent();
                if (opp != null) {
                    try {
                        sendLine(opp.getChannel(), "INFO:Opponent disconnected");
                    } catch (IOException ignored) {}
                    opp.setOpponent(null);
                    waiting.add(opp);
                }
            }
            log("Disconnect " + client.getRemoteAddress());
            client.close();
        } catch (IOException e) {
            log("Error during disconnect: " + e.getMessage());
        }
    }

    private void log(String s) {
        System.out.println("[Server] " + s);
    }

    public static void main(String[] args) throws IOException {
        int port = 5000;
        NioServer server = new NioServer(port);
        server.start();
    }
}