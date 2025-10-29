package com.mycompany.gamerockpaperscissors;

public class GameRockPaperScissors{
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new GameClientUI().setVisible(true);
        });
    }
}
