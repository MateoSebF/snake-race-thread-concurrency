package snakepackage;

import java.awt.*;

import javax.swing.JFrame;

import enums.GameState;
import enums.GridSize;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;
import javax.swing.JPanel;

/**
 * @author jd-
 *
 */
public class SnakeApp {

    private static SnakeApp app;
    public static final int MAX_THREADS = 4;
    Snake[] snakes = new Snake[MAX_THREADS];
    private static final Cell[] spawn = {
            new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell(GridSize.GRID_WIDTH - 2,
                    3 * (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, 1),
            new Cell((GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2),
            new Cell(1, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell(GridSize.GRID_WIDTH - 2, (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell((GridSize.GRID_WIDTH / 2) / 2, 1),
            new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2,
                    GridSize.GRID_HEIGHT - 2) };
    private JFrame frame;
    private static Board board;
    int nr_selected = 0;
    Thread[] thread = new Thread[MAX_THREADS];
    Button action;
    public static GameState gameState = GameState.STARTED;
    public static Object lock = new Object();
    private CountDownLatch countDownLatch = new CountDownLatch(MAX_THREADS);

    public SnakeApp() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame = new JFrame("The Snake Race");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setSize(618, 640);
        frame.setSize(GridSize.GRID_WIDTH * GridSize.WIDTH_BOX + 17,
                GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX + 40);
        frame.setLocation(dimension.width / 2 - frame.getWidth() / 2,
                dimension.height / 2 - frame.getHeight() / 2);
        board = new Board();

        frame.add(board, BorderLayout.CENTER);

        JPanel actionsBPabel = new JPanel();
        actionsBPabel.setLayout(new FlowLayout());
        action = new Button("Start the game");
        action.setBackground(new Color(89, 198, 193));
        action.setPreferredSize(new Dimension(150, 50));
        actionsBPabel.add(action);
        frame.add(actionsBPabel, BorderLayout.SOUTH);
        action.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (gameState) {
                    case STARTED:
                        app.runGame();
                        action.setLabel("Pause the game");
                        break;
                    case RUNNING:
                        app.pauseGame();
                        action.setLabel("Resume the game");
                        break;
                    case PAUSED:
                        app.resumeGame();
                        action.setLabel("Pause the game");
                        break;
                    case ENDED:
                        break;
                }
            }
        });

    }

    public void pauseGame() {
        long lowestTime = 1000000000;
        int lowestThread = -1;
        int longestSize = 0;
        int longestThread = -1;
        for (int i = 0; i != MAX_THREADS; i++) {
            long timeAlive = snakes[i].getDuration();
            if (snakes[i].isSnakeEnd() && lowestTime > timeAlive) {
                lowestTime = timeAlive;
                lowestThread = i;
            }
            if (!snakes[i].isSnakeEnd() && snakes[i].getBody().size() > longestSize) {
                longestSize = snakes[i].getBody().size();
                longestThread = i;
            }
        }
        System.out.println("The longest snake is: " + longestThread);
        System.out.println("The worst snake is: " + lowestThread);
        if (longestThread != -1)
            snakes[longestThread].setIsTheBest();

        if (lowestThread != -1)
            snakes[lowestThread].setIsTheWorst();
        synchronized (lock) {
            gameState = GameState.PAUSED;
        }
        board.repaint();
    }

    public void resumeGame() {
        for (int i = 0; i != MAX_THREADS; i++) {
            snakes[i].setNormal();
        }
        synchronized (lock) {
            gameState = GameState.RUNNING;
            lock.notifyAll();
        }
        board.repaint();
    }

    public void runGame() {
        for (int i = 0; i != MAX_THREADS; i++) {
            thread[i].start();
        }
        gameState = GameState.RUNNING;
    }

    public static void main(String[] args) {
        app = new SnakeApp();
        app.init();
    }

    @SuppressWarnings("deprecation")
    private void init() {
        for (int i = 0; i != MAX_THREADS; i++) {
            snakes[i] = new Snake(i + 1, spawn[i], i + 1, countDownLatch);
            snakes[i].addObserver(board);
            thread[i] = new Thread(snakes[i]);
        }

        frame.setVisible(true);

        try {
            countDownLatch.await();
            gameState = GameState.ENDED;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (gameState == GameState.ENDED) {
            System.out.println("Game Over");
            System.out.println("Thread (snake) status:");
            for (int i = 0; i != MAX_THREADS; i++) {
                System.out.println("[" + i + "] :" + thread[i].getState());
            }
        }
        System.exit(0);
    }

    public static SnakeApp getApp() {
        return app;
    }

}
