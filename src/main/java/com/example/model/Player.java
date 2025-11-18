package com.example.model;

import java.nio.channels.SocketChannel;

public class Player {
    private final String name;
    private final SocketChannel channel;
    private Player opponent;
    private Gesture gesture;
    private int score = 0; 

    public Player(String name, SocketChannel channel) {
        this.name = name;
        this.channel = channel;
    }

    public String getName() { return name; }
    public SocketChannel getChannel() { return channel; }
    public Player getOpponent() { return opponent; }
    public void setOpponent(Player opponent) { this.opponent = opponent; }
    public Gesture getGesture() { return gesture; }
    public void setGesture(Gesture gesture) { this.gesture = gesture; }
    public int getScore() { return score; }
    public void resetScore() { this.score = 0; }
    public void incrementScore() { this.score++; }
}