package view;


import controller.GameController;
import controller.winFrame;
import model.PlayerColor;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 这个类表示游戏过程中的整个游戏界面，是一切的载体
 */
public class ChessGameFrame extends JFrame {
    //    public final Dimension FRAME_SIZE ;
    public static JLabel roundLabel;
    private final int WIDTH;
    private final int HEIGTH;
    boolean isSelected = false;
    private final int ONE_CHESS_SIZE;
    private JLabel timerLabel;
    private Timer timer;
    public int remainingSeconds;

    private ChessboardComponent chessboardComponent;
    public GameController gameController;
    public static boolean isMusicPlaying = false;
    public Clip clip;

    public ChessGameFrame(int width, int height) {
        setTitle("斗兽棋"); //设置标题
        this.WIDTH = width;
        this.HEIGTH = height;
        this.ONE_CHESS_SIZE = (HEIGTH * 4 / 5) / 9;

        setSize(WIDTH, HEIGTH);
        setLocationRelativeTo(null); // Center the window.
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); //设置程序关闭按键，如果点击右上方的叉就游戏全部关闭了
        setLayout(null);


        addChessboard();
        addRound();
        addRuleButton();
        addLoadButton();
        addSaveButton();
        addRestartButton();
        addReturnButton();
        addMusicButton();
        addOfferLoseButton();
        addTimeLabel();
        startTimer();
        addImage();
    }


    private void addTimeLabel() {
        timerLabel = new JLabel("30s");
        timerLabel.setLocation(HEIGTH - 700, HEIGTH / 10 + 80); // 设置倒计时标签的位置
        timerLabel.setSize(700, 700); // 设置倒计时标签的大小
        timerLabel.setFont(new Font("Arial", Font.BOLD, 79)); // 设置倒计时标签的字体样式
        timerLabel.setForeground(Color.WHITE); // 设置字体颜色为蓝色
        add(timerLabel);
    }

    private void startTimer() {
        remainingSeconds = 60;
        updateTimerLabel();

        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                remainingSeconds--;
                if (remainingSeconds <= 20) {
                    timerLabel.setForeground(Color.RED);
                    updateTimerLabel();
                } else {
                    updateTimerLabel();
                }

                if (remainingSeconds == 0) {
                    timer.stop();
                    winFrame winFrame = new winFrame(getGameController().getCurrentPlayer());
                    winFrame.setVisible(true);
                }
            }
        });
        timer.start();
    }

    public PlayerColor oppositePlayerColor(PlayerColor playerColor) {
        if (playerColor.equals(PlayerColor.BLUE))
            return PlayerColor.RED;
        else return
                PlayerColor.BLUE;
    }


    public void updateTimerLabel() {
        timerLabel.setText(remainingSeconds + "s");
    }


    public void addMusicButton() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("resource\\music-button.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedImage finalImage = image;
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g.create();

                // 设置圆形的位置和大小
                int circleSize = Math.min(getWidth(), getHeight()) - 10;
                int x = (getWidth() - circleSize) / 2;
                int y = (getHeight() - circleSize) / 2;

                // 绘制圆形背景图像
                g2d.drawImage(finalImage, x, y, circleSize, circleSize, null);

                g2d.dispose();
            }
        };

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //写入音效（如果开则关，关则开）
                if (isMusicPlaying) {
                    stopMusic(); // 关闭音乐
                } else {
                    playMusic(); // 播放音乐
                }
                isMusicPlaying = !isMusicPlaying;

            }
        });

        button.setBorderPainted(false); // 移除边框
        button.setContentAreaFilled(false); // 移除内容区域填充
        button.setLocation(HEIGTH + 140, HEIGTH / 10 - 80);
        button.setSize(62, 62); // 调整按钮的大小为60x60像素
        button.setFont(new Font("Rockwell", Font.BOLD, 20));

        // 添加鼠标事件监听器
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 修改按钮的按下效果
                button.setLocation(HEIGTH + 140, (HEIGTH / 10 - 80) + 5);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 恢复按钮的原始位置
                button.setLocation(HEIGTH + 140, HEIGTH / 10 - 80);
            }
        });

        add(button);
    }

    public void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

    public void playMusic() {
        try {
            // 加载音频文件
            File audioFile = new File("resource\\bgm .wav");

            // 创建音频输入流
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            // 获取音频格式
            AudioFormat format = audioStream.getFormat();

            // 创建数据行信息
            DataLine.Info info = new DataLine.Info(Clip.class, format);

            // 打开数据行
            this.clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioStream);

            // 播放音乐
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    //加入重新开始按钮
    private void addRestartButton() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("resource\\restart-button.tif"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JButton button = new JButton(new ImageIcon(image));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //在此写入重新开局的方法
                chessboardComponent.getGameController().restart();

            }
        });
        button.setLocation(HEIGTH + 50, HEIGTH / 10 + 270);
        button.setSize(200, 60);
        button.setFont(new Font("Rockwell", Font.BOLD, 20));
        add(button);
    }

    public void addRuleButton() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("resource\\rule-button.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedImage finalImage = image;
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g.create();

                // 设置圆形的位置和大小
                int circleSize = Math.min(getWidth(), getHeight()) - 10;
                int x = (getWidth() - circleSize) / 2;
                int y = (getHeight() - circleSize) / 2;

                // 绘制圆形背景图像
                g2d.drawImage(finalImage, x, y, circleSize, circleSize, null);

                g2d.dispose();
            }
        };

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RuleFrame ruleFrame = new RuleFrame(400, 400);
                ruleFrame.setVisible(true);
                isSelected = true;
            }
        });

        button.setBorderPainted(false); // 移除边框
        button.setContentAreaFilled(false); // 移除内容区域填充
        button.setLocation(HEIGTH + 200, HEIGTH / 10 - 80);
        button.setSize(60, 60); // 调整按钮的大小为60x60像素
        button.setFont(new Font("Rockwell", Font.BOLD, 20));

        // 添加鼠标事件监听器
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 修改按钮的按下效果
                button.setLocation(HEIGTH + 200, (HEIGTH / 10 - 80) + 5);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 恢复按钮的原始位置
                button.setLocation(HEIGTH + 200, HEIGTH / 10 - 80);
            }
        });

        add(button);
    }

    private void addImage() {
        ImageIcon icon1 = new ImageIcon("resource\\bg.png");
        //创建一个JLabel的对象（管理容器）
        JLabel jLabel1 = new JLabel(icon1);
        //图片位置
        jLabel1.setBounds(-10, 0, 1200, 800);
        this.add(jLabel1);
        this.getContentPane().add(jLabel1);
    }

    private void addRedOwner() {
        ImageIcon icon1 = new ImageIcon("resource\\red-owner.png");
        //创建一个JLabel的对象（管理容器）
        JLabel jLabel1 = new JLabel(icon1);
        //图片位置
        jLabel1.setBounds(100, 170, 238, 300);
        this.add(jLabel1);
        this.getContentPane().add(jLabel1);
    }

    public ChessboardComponent getChessboardComponent() {
        return chessboardComponent;
    }

    public void setChessboardComponent(ChessboardComponent chessboardComponent) {
        this.chessboardComponent = chessboardComponent;
    }

    /**
     * 在游戏面板中添加棋盘
     */
    private void addChessboard() {
        chessboardComponent = new ChessboardComponent(ONE_CHESS_SIZE);
        int chessboardWidth = chessboardComponent.getWidth();
        int chessboardHeight = chessboardComponent.getHeight();
        int x = (getWidth() - chessboardWidth) / 2;
        int y = (getHeight() - chessboardHeight) / 2;
        chessboardComponent.setLocation(x, y);
        add(chessboardComponent);
    }

    public void addRound() {
        roundLabel = new JLabel("turn 1：BLUE");
        roundLabel.setLocation(HEIGTH - 800, HEIGTH / 10 - 40);
        roundLabel.setSize(600, 700);
        roundLabel.setFont(roundLabel.getFont().deriveFont(Font.BOLD, 40f)); // 设置字体大小为40
        roundLabel.setForeground(Color.BLUE); // 设置字体颜色为蓝色
        add(roundLabel);
    }

    public void updateLabel(String status, PlayerColor playerColor) {
        roundLabel.setText(status);
        roundLabel.setForeground(playerColor.getColor());
    }


    private void addOfferLoseButton() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("resource\\offerlose-button.tif"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JButton button1 = new JButton(new ImageIcon(image));
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                winFrame winFrame = new winFrame(oppositePlayerColor(gameController.getCurrentPlayer()));
                winFrame.setVisible(true);
            }
        });
        button1.setLocation(HEIGTH + 50, HEIGTH / 10 + 400);
        button1.setSize(200, 60);
        button1.setFont(new Font("Rockwell", Font.BOLD, 20));
        add(button1);
    }

    private void addLoadButton() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("resource\\load-button.tif"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JButton button = new JButton(new ImageIcon(image));
        button.setLocation(HEIGTH + 50, HEIGTH / 10 + 30);
        button.setSize(200, 60);
        button.setFont(new Font("Rockwell", Font.BOLD, 20));
        add(button);

        button.addActionListener(e -> {
            System.out.println("Click load");
            String path = JOptionPane.showInputDialog(this, "Input Path here");
            gameController.loadGameFromFile(path);
        });
    }

    private void addSaveButton() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("resource\\save-button.tif"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JButton button = new JButton(new ImageIcon(image));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Click save");
                try {
                    gameController.saveGameToFile();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        button.setLocation(HEIGTH + 50, HEIGTH / 10 + 150);
        button.setSize(200, 60);
        button.setFont(new Font("Rockwell", Font.BOLD, 20));
        add(button);
    }

    private void addReturnButton() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("resource\\return-button.tif"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JButton button1 = new JButton(new ImageIcon(image));
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose(); // 释放界面资源

                StartFrame startFrame = new StartFrame(490, 750);
                startFrame.setVisible(true);
            }


        });
        button1.setLocation(HEIGTH + 50, HEIGTH / 10 + 520);
        button1.setSize(200, 60);
        button1.setFont(new Font("Rockwell", Font.BOLD, 20));
        add(button1);
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(int remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public static JLabel getRoundLabel() {
        return roundLabel;
    }

    public static void setRoundLabel(JLabel roundLabel) {
        ChessGameFrame.roundLabel = roundLabel;
    }

    public int getWIDTH() {
        return WIDTH;
    }

    public int getHEIGTH() {
        return HEIGTH;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getONE_CHESS_SIZE() {
        return ONE_CHESS_SIZE;
    }

    public JLabel getTimerLabel() {
        return timerLabel;
    }

    public void setTimerLabel(JLabel timerLabel) {
        this.timerLabel = timerLabel;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public GameController getGameController() {
        return gameController;
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    public static boolean isIsMusicPlaying() {
        return isMusicPlaying;
    }

    public static void setIsMusicPlaying(boolean isMusicPlaying) {
        ChessGameFrame.isMusicPlaying = isMusicPlaying;
    }

    public Clip getClip() {
        return clip;
    }

    public void setClip(Clip clip) {
        this.clip = clip;
    }

    public void setgameController(GameController gameController) {
        this.gameController = gameController;
    }
}