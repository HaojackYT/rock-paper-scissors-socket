package com.mycompany.gamerockpaperscissors;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.ImageIcon;


public final class GameClientUI extends javax.swing.JFrame {
    public GameClientUI() {
        initComponents();
        setResizable(false);
        
        jTextArea1.setEditable(false);
        jTextArea1.setFocusable(false);
        
        jTextArea1.setText("Chọn Đá - Bao - Kéo để bắt đầu!");


        
        jLabel_Player.setBorder(gray_border);
        jLabel_BOT.setBorder(gray_border);
        jLabel_Rock.setBorder(gray_border);
        jLabel_Paper.setBorder(gray_border);
        jLabel_Scissors.setBorder(gray_border);
        
        
        displayImage(rock, jLabel_Rock);
        displayImage(paper, jLabel_Paper);
        displayImage(scissors, jLabel_Scissors);
        
        
        list.add(rock);
        list.add(paper);
        list.add(scissors);
        
    }
    
    public void Point(String Player, String BOT)
    {
        if(Player.equals(BOT))
        {}
        
        
        
        //1
        else if(Player.equals(rock))
        {
            if(BOT.equals(scissors))
            {
                Player_wins++;
                PlayerPoint.setText(String.valueOf(Player_wins));              
            }
            else
            {
               BOT_wins++;
               BOTPoint.setText(String.valueOf(BOT_wins));                                       
            }       
        }
        
        //2
         else if(Player.equals(paper))
        {
            if(BOT.equals(rock))
            {
                Player_wins++;
                PlayerPoint.setText(String.valueOf(Player_wins));              
            }
            else
            {
               BOT_wins++;
               BOTPoint.setText(String.valueOf(BOT_wins));                                       
            }       
        }
         
        //3
         else if(Player.equals(scissors))
        {
            if(BOT.equals(paper))
            {
                Player_wins++;
                PlayerPoint.setText(String.valueOf(Player_wins));              
            }
            else
            {
               BOT_wins++;
               BOTPoint.setText(String.valueOf(BOT_wins));                                       
            }       
        }
    
    }
    
    private void checkWinner(String player, String bot) {
    String result = "";

    if (player.equals(bot)) {
        result = "Bạn Hòa!";
    } 
    else if (
        (player.equals(rock) && bot.equals(scissors)) ||
        (player.equals(paper) && bot.equals(rock)) ||
        (player.equals(scissors) && bot.equals(paper))
    ) {
        result = "Bạn thắng!";
    } 
    else {
        result = "Bạn thua!";
    }

    jTextArea1.setText(result);
}

        Border gray_border = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.DARK_GRAY);
        Border orange_border = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.ORANGE);
        
       
        //image
        String rock = "/image/rock.png";
        String paper = "/image/paper.png";
        String scissors = "/image/scissors.png";
        
        
        ArrayList<String> list = new ArrayList<>();
        int Player_wins = 0, BOT_wins = 0;
        Random random = new Random();
        String random_selection;
        
    public void displayImage(String imagePath, JLabel label)
    {
        ImageIcon imgIco = new ImageIcon(getClass().getResource(imagePath));
        
        Image img = imgIco.getImage().getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);
         
        label.setIcon(new ImageIcon(img));
    }
    
    
   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel_BOT = new javax.swing.JLabel();
        jLabel_Player = new javax.swing.JLabel();
        jLabel_Paper = new javax.swing.JLabel();
        jLabel_Scissors = new javax.swing.JLabel();
        jLabel_Rock = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        PlayerPoint = new javax.swing.JLabel();
        BOTPoint = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel_BOT.setBackground(new java.awt.Color(204, 255, 204));
        jLabel_BOT.setOpaque(true);

        jLabel_Player.setBackground(new java.awt.Color(204, 255, 204));
        jLabel_Player.setOpaque(true);

        jLabel_Paper.setBackground(new java.awt.Color(255, 204, 204));
        jLabel_Paper.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel_Paper.setOpaque(true);
        jLabel_Paper.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_PaperMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel_PaperMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel_PaperMouseExited(evt);
            }
        });

        jLabel_Scissors.setBackground(new java.awt.Color(204, 0, 204));
        jLabel_Scissors.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel_Scissors.setOpaque(true);
        jLabel_Scissors.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_ScissorsMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel_ScissorsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel_ScissorsMouseExited(evt);
            }
        });

        jLabel_Rock.setBackground(new java.awt.Color(255, 153, 0));
        jLabel_Rock.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel_Rock.setOpaque(true);
        jLabel_Rock.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_RockMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel_RockMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel_RockMouseExited(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(204, 0, 0));
        jLabel6.setText("BOT");

        jLabel7.setBackground(new java.awt.Color(204, 0, 255));
        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(153, 0, 153));
        jLabel7.setText("Player");

        jLabel8.setBackground(new java.awt.Color(255, 102, 102));
        jLabel8.setFont(new java.awt.Font("Harlow Solid Italic", 0, 48)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 102));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Game Rock-Paper-Scissors");
        jLabel8.setOpaque(true);

        jButton1.setText("Reset");
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        jButton2.setText("Exit");
        jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        jTextArea1.setBackground(new java.awt.Color(153, 255, 204));
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        PlayerPoint.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        PlayerPoint.setForeground(new java.awt.Color(255, 0, 255));
        PlayerPoint.setText("0");

        BOTPoint.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        BOTPoint.setForeground(new java.awt.Color(255, 0, 0));
        BOTPoint.setText("0");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        jLabel3.setText(" -");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel_Player, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(73, 73, 73)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(0, 37, Short.MAX_VALUE)
                                .addComponent(PlayerPoint, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(33, 33, 33)
                                .addComponent(jLabel3)
                                .addGap(117, 117, 117))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(BOTPoint, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton2)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel_BOT, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel_Rock, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(162, 162, 162)
                        .addComponent(jLabel_Paper, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel_Scissors, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 541, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(15, 15, 15))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1)
                            .addComponent(jButton2)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(66, 66, 66)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(PlayerPoint)
                                .addComponent(BOTPoint)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel_Player, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel_BOT, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel6))))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_Paper, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_Rock, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_Scissors, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel_RockMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_RockMouseEntered
        jLabel_Rock.setBorder(orange_border);
    }//GEN-LAST:event_jLabel_RockMouseEntered

    private void jLabel_RockMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_RockMouseExited
        jLabel_Rock.setBorder(gray_border);
    }//GEN-LAST:event_jLabel_RockMouseExited

    private void jLabel_RockMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_RockMouseClicked
        displayImage(rock, jLabel_Player);
        random_selection = list.get(random.nextInt(list.size()));
        displayImage(random_selection, jLabel_BOT);
        checkWinner(rock, random_selection);
        Point(rock, random_selection);
    }//GEN-LAST:event_jLabel_RockMouseClicked

    private void jLabel_ScissorsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_ScissorsMouseClicked
        displayImage(scissors, jLabel_Player);
        random_selection = list.get(random.nextInt(list.size()));
        displayImage(random_selection, jLabel_BOT);
        checkWinner(scissors, random_selection);
        Point(scissors, random_selection);
    }//GEN-LAST:event_jLabel_ScissorsMouseClicked

    private void jLabel_ScissorsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_ScissorsMouseEntered
        jLabel_Scissors.setBorder(orange_border);
    }//GEN-LAST:event_jLabel_ScissorsMouseEntered

    private void jLabel_ScissorsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_ScissorsMouseExited
        jLabel_Scissors.setBorder(gray_border);
    }//GEN-LAST:event_jLabel_ScissorsMouseExited

    private void jLabel_PaperMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_PaperMouseExited
        jLabel_Paper.setBorder(gray_border);
    }//GEN-LAST:event_jLabel_PaperMouseExited

    private void jLabel_PaperMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_PaperMouseEntered
        jLabel_Paper.setBorder(orange_border);
    }//GEN-LAST:event_jLabel_PaperMouseEntered

    private void jLabel_PaperMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_PaperMouseClicked
        displayImage(paper, jLabel_Player);
        random_selection = list.get(random.nextInt(list.size()));
        displayImage(random_selection, jLabel_BOT);
        checkWinner(paper, random_selection);
        Point(paper, random_selection);
    }//GEN-LAST:event_jLabel_PaperMouseClicked

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        
        Player_wins = 0;
        BOT_wins = 0;
        
      
        PlayerPoint.setText(String.valueOf(Player_wins));
        BOTPoint.setText(String.valueOf(BOT_wins));
        
       
        jLabel_Player.setIcon(null);
        jLabel_BOT.setIcon(null);
        
        
        jTextArea1.setText("Chọn Đá - Bao - Kéo để bắt đầu!");
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked

        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn thoát game?",
                "Xác nhận thoát",
                javax.swing.JOptionPane.YES_NO_OPTION
        );

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }//GEN-LAST:event_jButton2MouseClicked

  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel BOTPoint;
    private javax.swing.JLabel PlayerPoint;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel_BOT;
    private javax.swing.JLabel jLabel_Paper;
    private javax.swing.JLabel jLabel_Player;
    private javax.swing.JLabel jLabel_Rock;
    private javax.swing.JLabel jLabel_Scissors;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
