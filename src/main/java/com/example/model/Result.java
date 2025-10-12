package com.example.model;

public enum Result {
    WIN, LOSE, DRAW;

    public static Result fromGestures(Gesture a, Gesture b) {
        if (a == b) return DRAW;
        switch (a) {
            case ROCK:
                return (b == Gesture.SCISSORS) ? WIN : LOSE;
            case PAPER:
                return (b == Gesture.ROCK) ? WIN : LOSE;
            case SCISSORS:
                return (b == Gesture.PAPER) ? WIN : LOSE;
        }
        return DRAW;
    }
}