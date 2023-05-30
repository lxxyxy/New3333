package controller;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import listener.GameListener;
import model.Constant;
import model.PlayerColor;
import model.Chessboard;
import model.ChessboardPoint;
import view.*;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import javax.swing.JOptionPane;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.io.InputStreamReader;
import java.util.Objects;


/**
 * Controller is the connection between model and view,
 * when a Controller receive a request from a view, the Controller
 * analyzes and then hands over to the model for processing
 * [in this demo the request methods are onPlayerClickCell() and onPlayerClickChessPiece()]
 */
public class GameController implements GameListener {


    private Chessboard model = new Chessboard();
    private ChessboardComponent view;
    private PlayerColor currentPlayer;
    private ChessboardPoint selectedPoint;
    private int round = 1;

    private ChessGameFrame chessGameFrame;

    public GameController(ChessboardComponent view, Chessboard model, ChessGameFrame chessGameFrame) {
        this.view = view;
        this.model = model;
        this.currentPlayer = PlayerColor.BLUE;
        this.chessGameFrame = chessGameFrame;
        view.registerController(this);
        //initialize();
        view.initiateChessComponent(model);
        view.repaint();
    }

    public GameController() {
    }

 /*   private void initialize() {
        for (int i = 0; i < Constant.CHESSBOARD_ROW_SIZE.getNum(); i++) {
            for (int j = 0; j < Constant.CHESSBOARD_COL_SIZE.getNum(); j++) {

            }
        }
    }*/

    // after a valid move swap the player
    private void swapColor() {
        currentPlayer = currentPlayer == PlayerColor.BLUE ? PlayerColor.RED : PlayerColor.BLUE;
        chessGameFrame.setRemainingSeconds(60);
        chessGameFrame.updateTimerLabel();
        if (currentPlayer == PlayerColor.BLUE) {
            round++;
        }
        chessGameFrame.updateLabel("turn " + round + "：" + currentPlayer, currentPlayer);
        view.repaint();

    }

    private boolean win() {
        // TODO: Check the board if there is a winner

        return false;
    }

    /*   public void solveTrap(ChessboardPoint selectedPoint, ChessboardPoint destPoint) {
           if (getGridAt(destPoint).getType() == GridType.TRAP && getGridAt(destPoint).getOwner() != getChessPieceAt(selectedPoint).getOwner()) {
               getTrapped(selectedPoint);
           } else if (getGridAt(selectedPoint).getType() == GridType.TRAP && getGridAt(selectedPoint).getOwner() != getChessPieceAt(selectedPoint).getOwner()) {
               exitTrap(selectedPoint);
           }
       }
   */
    // click an empty cell
    @Override
    public void onPlayerClickCell(ChessboardPoint point, CellComponent component) {
        if (selectedPoint != null && model.isValidMove(selectedPoint, point)) {

            // TODO: if the chess enter Dens or Traps and so on
            //先写进入巢穴,则对方赢
            if (model.decidedens(point, selectedPoint)) {
                winFrame winFrame = new winFrame(currentPlayer);
                winFrame.setVisible(true);
                view.setChessComponentAtGrid(point, view.removeChessComponentAtGrid(selectedPoint));
                selectedPoint = null;
                view.repaint();
            } else {
                model.decidetraps(point, selectedPoint);
                model.moveChessPiece(selectedPoint, point);
                view.setChessComponentAtGrid(point, view.removeChessComponentAtGrid(selectedPoint));
                selectedPoint = null;
                swapColor();
                playMusic();
                view.repaint();
            }
        }
    }

    // click a cell with a chess
    @Override
    public void onPlayerClickChessPiece(ChessboardPoint point, ChessComponent component) {
        if (selectedPoint == null) {
            if (model.getChessPieceOwner(point).equals(currentPlayer)) {
                selectedPoint = point;
                component.setSelected(true);
                playMusic();
                component.repaint();
            }
        } else if (selectedPoint.equals(point)) {
            selectedPoint = null;
            component.setSelected(false);
            component.repaint();
            playMusic();
        }
        // TODO: Implement capture function
        else {
            model.captureChessPiece(selectedPoint, point);
            view.removeChessComponentAtGrid(point);
            view.setChessComponentAtGrid(point, view.removeChessComponentAtGrid(selectedPoint));
            playMusic();
            selectedPoint = null;
            view.repaint();
            swapColor();


        }

    }

    public Chessboard getModel() {
        return model;
    }

    public void setModel(Chessboard model) {
        this.model = model;
    }

    public ChessboardComponent getView() {
        return view;
    }

    public void setView(ChessboardComponent view) {
        this.view = view;
    }

    public PlayerColor getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(PlayerColor currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public ChessboardPoint getSelectedPoint() {
        return selectedPoint;
    }

    public void setSelectedPoint(ChessboardPoint selectedPoint) {
        this.selectedPoint = selectedPoint;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public ChessGameFrame getChessGameFrame() {
        return chessGameFrame;
    }

    public void setChessGameFrame(ChessGameFrame chessGameFrame) {
        this.chessGameFrame = chessGameFrame;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    public void restart() {
        model.removeAllPieces();
        model.initPieces();
        round = 1;
        chessGameFrame.updateLabel("turn " + round + "：BLUE", PlayerColor.BLUE);
        chessGameFrame.setRemainingSeconds(60);
        chessGameFrame.updateTimerLabel();
        view.removeAllPieces();
        view.initiateChessComponent(model);
        view.repaint();
        currentPlayer = PlayerColor.BLUE;
        selectedPoint = null;
    }

  /*  public void regret(){
        if (stepList.isEmpty()) {
            return;
        }
        Step step = stepList.remove(stepList.size() - 1);
        model.undoStep(step);
        view.undoStep(step);
        view.repaint();
        swapColor();
    }*/


    public void loadGameFromFile(String path) {
        try {
            // 读取文件内容
            List<String> lines = Files.readAllLines(Path.of(path));

            // 校验文件格式
            if (!isValidFileFormat(path)) {
                showErrorDialog("文件格式错误", "101");
                return;
            }

            // 校验棋盘大小
            if (!isValidChessboardSize(lines)) {
                showErrorDialog("棋盘大小错误", "102");
                return;
            }

            // 校验棋子
            if (!isValidChessPieces(lines)) {
                showErrorDialog("棋子错误", "103");
                return;
            }

            // 校验下一步行棋方
            if (!hasNextMove(lines)) {
                showErrorDialog("缺少下一步行棋方", "104");
                return;
            }

            // 清空模型中的棋子
            model.removeAllPieces();

            // 初始化棋子
            model.initPieces(lines);

            // 清空视图中的棋子
            view.removeAllPieces();

            // 初始化棋盘视图
            view.initiateChessComponent(model);

            // 刷新视图
            view.repaint();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showErrorDialog(String message, String errorCode) {
        String errorMessage = message + "\n错误编码：" + errorCode;
        JOptionPane.showMessageDialog(null, errorMessage, "错误", JOptionPane.ERROR_MESSAGE);
    }

    private boolean isValidFileFormat(String path) {
        String fileExtension = getFileExtension(path);
        return fileExtension.equalsIgnoreCase("txt");
    }


    private String getFileExtension(String path) {
        int lastDotIndex = path.lastIndexOf(".");
        if (lastDotIndex != -1 && lastDotIndex < path.length() - 1) {
            return path.substring(lastDotIndex + 1);
        }
        return "";
    }


    private boolean isValidChessboardSize(List<String> lines) {
        // 检查行数是否为7
        if (lines.size() != 7) {
            return false;
        }

        // 检查每行的字符数量是否为9
        for (String line : lines) {
            if (line.length() != 9) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidChessPieces(List<String> lines) {
        String chessPiecePattern = "[a-p]*";  // 匹配字母a到p的任意次数

        for (String line : lines) {
            if (!line.matches(chessPiecePattern)) {
                return false;
            }
        }

        return true;
    }


    private boolean hasNextMove(List<String> lines) {
        String regex = "(?=.*[a-h])(?=.*[i-p])";  // 包含字母a到h和字母i到p

        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line);
        }
        String concatenatedLines = sb.toString();

        return concatenatedLines.matches(regex);
    }


    public void saveGameToFile() throws IOException {
        String fileName = JOptionPane.showInputDialog(null, "请输入文件名：");

        String userId = "user"; // 替换为用户ID或用户名
        String folderPath = "D:\\code\\New2\\resource\\"; // 根据用户ID创建文件夹路径
        String filePath = folderPath + "\\" + fileName + ".txt"; // 文件路径

        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs(); // 创建文件夹
        }

        File file = new File(filePath);
        file.createNewFile();

        FileWriter writer = new FileWriter(file);
        String toStore = new String();
        for (int i = 0; i < Constant.CHESSBOARD_ROW_SIZE.getNum(); i++) {
            for (int j = 0; j < Constant.CHESSBOARD_COL_SIZE.getNum(); j++) {
                ChessboardPoint point = new ChessboardPoint(i, j);
                if (model.getChessPieceAt(point) == null) {
                    toStore += "0";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Rat") && model.getChessPieceAt(point).getOwner() == PlayerColor.BLUE) {
                    toStore += "j";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Cat") && model.getChessPieceAt(point).getOwner() == PlayerColor.BLUE) {
                    toStore += "k";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Dog") && model.getChessPieceAt(point).getOwner() == PlayerColor.BLUE) {
                    toStore += "l";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Wolf") && model.getChessPieceAt(point).getOwner() == PlayerColor.BLUE) {
                    toStore += "m";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Leopard") && model.getChessPieceAt(point).getOwner() == PlayerColor.BLUE) {
                    toStore += "n";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Tiger") && model.getChessPieceAt(point).getOwner() == PlayerColor.BLUE) {
                    toStore += "o";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Lion") && model.getChessPieceAt(point).getOwner() == PlayerColor.BLUE) {
                    toStore += "p";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Elephant") && model.getChessPieceAt(point).getOwner() == PlayerColor.BLUE) {
                    toStore += "q";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Rat") && model.getChessPieceAt(point).getOwner() == PlayerColor.RED) {
                    toStore += "a";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Cat") && model.getChessPieceAt(point).getOwner() == PlayerColor.RED) {
                    toStore += "b";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Dog") && model.getChessPieceAt(point).getOwner() == PlayerColor.RED) {
                    toStore += "c";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Wolf") && model.getChessPieceAt(point).getOwner() == PlayerColor.RED) {
                    toStore += "d";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Leopard") && model.getChessPieceAt(point).getOwner() == PlayerColor.RED) {
                    toStore += "e";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Tiger") && model.getChessPieceAt(point).getOwner() == PlayerColor.RED) {
                    toStore += "f";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Lion") && model.getChessPieceAt(point).getOwner() == PlayerColor.RED) {
                    toStore += "g";
                } else if (Objects.equals(model.getChessPieceAt(point).getName(), "Elephant") && model.getChessPieceAt(point).getOwner() == PlayerColor.RED) {
                    toStore += "h";
                } else {
                    toStore += "0";
                }
            }
            toStore += "\n";
        }
        System.out.println(toStore);
        writer.write(toStore);
        writer.close();
    }

    public void playMusic() {
        if (ChessGameFrame.isMusicPlaying) {
            try {
                // 加载音频文件
                File audioFile = new File("resource\\click.wav");

                // 创建音频输入流
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

                // 获取音频格式
                AudioFormat format = audioStream.getFormat();

                // 创建数据行信息
                DataLine.Info info = new DataLine.Info(Clip.class, format);

                // 打开数据行
                Clip clip = (Clip) AudioSystem.getLine(info);
                clip.open(audioStream);

                // 播放音乐
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    User user1 = new User("user1", "1");
    User user2 = new User("user2", "2");

   // public void regret() {
   //     model.removeChessPiece(model.nowPoint);
   //     model.setChessPiece(model.nowPoint, model.pastChess);
   //     model.setChessPiece(model.pastPoint, model.nowChess);
   //     chessGameFrame.repaint();
   // }
}


