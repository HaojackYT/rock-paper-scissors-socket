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
    
    // Points required to win the match
    private static final int POINTS_TO_WIN = 3; 

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
            if (p.getOpponent() == null) {
                sendLine(client, "INFO:Waiting for an opponent. Or, if you just finished a game, send JOIN:<name> again to re-enter the queue.");
                return;
            }
            if (p.getGesture() != null) {
                sendLine(client, "INFO:Move already recorded. Waiting for opponent...");
                return;
            }

            try {
                Gesture g = Gesture.valueOf(move.toUpperCase(Locale.ROOT));
                p.setGesture(g);
                sendLine(client, "INFO:Move recorded: " + g + ". Waiting for opponent...");
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
        Player existingPlayer = channelPlayerMap.get(client);
        if (existingPlayer != null) {
            // If the player already exists, reset the status and put them into the queue (after game ends)
            if (existingPlayer.getOpponent() != null) {
                // Should not happen after the fix, but just in case
                sendLine(client, "INFO:You are currently in a game. Wait for it to finish or 'quit' the client.");
                return;
            }
            // Reset status if they were unpaired after a match
            existingPlayer.resetScore();
            if (!waiting.contains(existingPlayer)) {
                waiting.add(existingPlayer);
            }
            sendLine(client, "INFO:Re-queued as " + name);
        } else {
            // Create a new player
            Player player = new Player(name, client);
            channelPlayerMap.put(client, player);
            player.resetScore(); 
            waiting.add(player);
            sendLine(client, "INFO:Registered as " + name);
        }
        
        tryPairing();
    }

    private void tryPairing() throws IOException {
        waiting.removeIf(p -> !p.getChannel().isOpen() || p.getOpponent() != null); // Only pair players who are waiting and have no opponent
        
        while (waiting.size() >= 2) {
            Player p1 = waiting.poll();
            Player p2 = waiting.poll();
            if (p1 == null || p2 == null) break;
            
            p1.setOpponent(p2);
            p2.setOpponent(p1);

            sendLine(p1.getChannel(), "OPPONENT:" + p2.getName() + ":Score=" + p1.getScore() + "-" + p2.getScore());
            sendLine(p2.getChannel(), "OPPONENT:" + p1.getName() + ":Score=" + p2.getScore() + "-" + p1.getScore());
            sendLine(p1.getChannel(), "READY:Send MOVE:<ROCK|PAPER|SCISSORS>");
            sendLine(p2.getChannel(), "READY:Send MOVE:<ROCK|PAPER|SCISSORS>");
        }
    }

    private void checkAndResolve(Player p) throws IOException {
        Player opp = p.getOpponent();
        if (opp == null) return; 
        if (p.getGesture() != null && opp.getGesture() != null) {
            // Both players have made a move
            Result r = Result.fromGestures(p.getGesture(), opp.getGesture());
            
            // Declare and initialize pMessage and oppMessage
            String pMessage;
            String oppMessage; 

            if (r == Result.DRAW) {
                pMessage = "RESULT:DRAW:You=" + p.getGesture() + ":Opp=" + opp.getGesture();
                oppMessage = "RESULT:DRAW:You=" + opp.getGesture() + ":Opp=" + p.getGesture();
            } else if (r == Result.WIN) {
                p.incrementScore();
                pMessage = "RESULT:WIN:You=" + p.getGesture() + ":Opp=" + opp.getGesture();
                // If p wins, opp loses
                oppMessage = "RESULT:LOSE:You=" + opp.getGesture() + ":Opp=" + p.getGesture();
            } else { // LOSE (p loses, opp wins)
                opp.incrementScore();
                pMessage = "RESULT:LOSE:You=" + p.getGesture() + ":Opp=" + opp.getGesture();
                // If p loses, opp wins
                oppMessage = "RESULT:WIN:You=" + opp.getGesture() + ":Opp=" + p.getGesture();
            }

            // Send round result
            sendLine(p.getChannel(), pMessage + ":CurrentScore=" + p.getScore() + "-" + opp.getScore());
            sendLine(opp.getChannel(), oppMessage + ":CurrentScore=" + opp.getScore() + "-" + p.getScore());
            
            // Check for overall win
            boolean pWins = p.getScore() >= POINTS_TO_WIN;
            boolean oppWins = opp.getScore() >= POINTS_TO_WIN;
            
            if (pWins || oppWins) {
                // MATCH END - DO NOT AUTOMATICALLY REQUEUE
                String finalPMessage = pWins ? "GAME_END:YOU_WIN" : "GAME_END:YOU_LOSE";
                String finalOppMessage = oppWins ? "GAME_END:YOU_WIN" : "GAME_END:YOU_LOSE";
                
                sendLine(p.getChannel(), finalPMessage + ":FinalScore=" + p.getScore() + "-" + opp.getScore());
                sendLine(opp.getChannel(), finalOppMessage + ":FinalScore=" + opp.getScore() + "-" + p.getScore());
                
                // Reset score and unpair
                p.resetScore();
                opp.resetScore();
                p.setOpponent(null);
                opp.setOpponent(null);
                
                // Send instruction to the player that they must JOIN again to play another match
                sendLine(p.getChannel(), "INFO:The match has ended. Send JOIN:<name> to play again or 'quit' in the client to exit.");
                sendLine(opp.getChannel(), "INFO:The match has ended. Send JOIN:<name> to play again or 'quit' in the client to exit.");
                
            } else {
                // PREPARE FOR NEXT ROUND (keep the pair)
                sendLine(p.getChannel(), "INFO:Next round. Send MOVE:<ROCK|PAPER|SCISSORS>");
                sendLine(opp.getChannel(), "INFO:Next round. Send MOVE:<ROCK|PAPER|SCISSORS>");
            }
            
            // Reset Gesture for the new round
            p.setGesture(null);
            opp.setGesture(null);
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
                        sendLine(opp.getChannel(), "INFO:Opponent disconnected. Send JOIN:<name> to find a new opponent.");
                    } catch (IOException ignored) {}
                    
                    // The opponent of the disconnected player will be reset and unpaired
                    opp.setOpponent(null);
                    opp.resetScore();
                    // DO NOT add opp back to the waiting queue, require them to JOIN again
                    sendLine(opp.getChannel(), "INFO:Match ended due to opponent disconnect. Send JOIN:<name> to play again.");
                    
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