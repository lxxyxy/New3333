package controller;

import model.PlayerColor;
import view.ChessGameFrame;
import view.ChessboardComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class winFrame extends JFrame {
    private final int WIDTH=300;
    private final int HEIGTH=200;///
    public winFrame(PlayerColor playerColor){
        setTitle("胜利"); //设置标题
        setSize(WIDTH, HEIGTH);
        setLocationRelativeTo(null); // Center the window.
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(null);
        JLabel winLabel1 = new JLabel(String.format("恭喜 %s 获得胜利",playerColor));
        Font font = new Font(winLabel1.getFont().getName(), Font.BOLD, 27);
        winLabel1.setFont(font);
        winLabel1.setForeground(playerColor.getColor());
        winLabel1.setBounds(20, 10, 400, 70);
        add(winLabel1);
        addRestartButton();
    }

    private void addRestartButton() {
        JButton button = new JButton("再来一局");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //在此写入重新开局的方法
                dispose();
            }
        });
        button.setLocation(40, 90);
        button.setSize(190, 40);
        button.setFont(new Font("Rockwell", Font.BOLD, 20));
        add(button);
    }
    }

