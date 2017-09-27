import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.io.FileWriter;
import java.io.File;

public class SnakeCanvas extends Canvas implements Runnable, KeyListener {

    private final int BOX_HEIGHT = 10;
    private final int BOX_WIDTH = 10;
    private final int GRID_WIDTH = 50;
    private final int GRID_HEIGHT = 40;


    private LinkedList<Point> snake;
    private Point fruit;
    private int direction = Direction.NO_DIRECTION;
    private int score;
    private String highScore = "";

    private Thread runThread;



    @Override
    public void paint(Graphics g) {
        this.addKeyListener(this);
        this.setPreferredSize(new Dimension(640, 480));

        if (snake == null) {
            snake = new LinkedList<>();
            generateDefaultSnake();
            placeFruit();

        }

        if (runThread == null) {

            runThread = new Thread(this);
            runThread.start();
        }

        if (highScore == "") {
            highScore = this.getHighScore();
        }

        drawFruit(g);
        drawGrid(g);
        drawSnake(g);
        drawScore(g);


    }

    public void update(Graphics g) {

        Graphics offScreenGraphics;
        BufferedImage offScreen = null;
        Dimension d = this.getSize();
        offScreen = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        offScreenGraphics = offScreen.getGraphics();
        offScreenGraphics.setColor(this.getBackground());
        offScreenGraphics.fillRect(0, 0, d.width, d.height);
        offScreenGraphics.setColor(this.getForeground());
        paint(offScreenGraphics);

        g.drawImage(offScreen, 0,0, this);
    }

    public void generateDefaultSnake() {

        score = 0;
        snake.clear();
        snake.add(new Point((GRID_WIDTH / 2) - 3, GRID_HEIGHT / 2));
        snake.add(new Point((GRID_WIDTH / 2) - 4, GRID_HEIGHT / 2));
        snake.add(new Point((GRID_WIDTH / 2) - 5, GRID_HEIGHT / 2));
        direction = Direction.NO_DIRECTION;
    }

    public void move() {

        Point head = snake.peekFirst();
        Point newPoint = head;
        switch (direction) {
            case Direction.NORTH:
                newPoint = new Point(head.x, head.y - 1);
                break;
            case Direction.SOUTH:
                newPoint = new Point(head.x, head.y + 1);
                break;
            case Direction.WEST:
                newPoint = new Point(head.x - 1, head.y);
                break;
            case Direction.EAST:
                newPoint = new Point(head.x + 1, head.y);
                break;
        }

        snake.remove(snake.peekLast());

        if (newPoint.equals(fruit)) {

            score += 10;
            Point addPoint = (Point) newPoint.clone();
            switch (direction) {
                case Direction.NORTH:
                    newPoint = new Point(head.x, head.y - 1);
                    break;
                case Direction.SOUTH:
                    newPoint = new Point(head.x, head.y + 1);
                    break;
                case Direction.WEST:
                    newPoint = new Point(head.x - 1, head.y);
                    break;
                case Direction.EAST:
                    newPoint = new Point(head.x + 1, head.y);
                    break;
            }
            snake.push(addPoint);
            placeFruit();

        } else if (newPoint.x < 0) {
            checkScore();
            generateDefaultSnake();
            return;

        } else if (newPoint.x > GRID_WIDTH - 1) {
            checkScore();
            generateDefaultSnake();
            return;

        } else if (newPoint.y < 0) {
            checkScore();
            generateDefaultSnake();
            return;

        } else if (newPoint.y > GRID_HEIGHT - 1) {
            checkScore();
            generateDefaultSnake();
            return;

        } else if (snake.contains(newPoint)) {

            if (!newPoint.equals(head)) {
                checkScore();
                generateDefaultSnake();
                return;
            }
        }

        snake.push(newPoint);



    }

    public void drawScore(Graphics g) {

        g.drawString("Score:" + score, 0, BOX_HEIGHT * GRID_HEIGHT + 15);
        g.drawString("Highscore: " + highScore, 0, BOX_HEIGHT * GRID_HEIGHT + 30);
    }

    public void checkScore() {

        if (score > Integer.parseInt((getHighScore().split(": ")[1]))) {
            String name = JOptionPane.showInputDialog("New Highscore! What is your name?");
            highScore = name + ": " + score;

            File highScoreFile = new File("highScore.dat");
            if (!highScoreFile.exists()) {
                try {
                    highScoreFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileWriter fileWriter = null;
            BufferedWriter bufferWriter = null;
            try {
                fileWriter = new FileWriter(highScoreFile);
                bufferWriter = new BufferedWriter(fileWriter);
                bufferWriter.write(this.highScore);
            } catch (Exception e) {

            } finally {

                try {
                    if (bufferWriter != null) {
                        bufferWriter.close();
                    }
                } catch (Exception e) {

                }

            }
        }


    }

    public void drawGrid(Graphics g) {

        g.setColor(Color.BLACK);
        g.drawRect(0,0,  GRID_WIDTH * BOX_WIDTH, GRID_HEIGHT * BOX_HEIGHT);
        g.drawLine(0, 0, 0, BOX_HEIGHT * GRID_HEIGHT);
        g.drawLine(GRID_WIDTH * BOX_WIDTH, 0, GRID_WIDTH * BOX_WIDTH, BOX_HEIGHT * GRID_HEIGHT);
        g.drawLine(0, 0, GRID_WIDTH * BOX_WIDTH, 0);
        g.drawLine(0, BOX_HEIGHT * GRID_HEIGHT, GRID_WIDTH * BOX_WIDTH, BOX_HEIGHT * GRID_HEIGHT);


    }

    public void drawSnake(Graphics g) {

        g.setColor(Color.GREEN);

        for (Point p: snake) {
            g.fillOval(p.x * BOX_HEIGHT, p.y * BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);
        }
        g.setColor(Color.BLACK);


    }

    public void drawFruit(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(fruit.x * BOX_WIDTH, fruit.y * BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);
    }

    public void placeFruit() {

        Random rand = new Random();
        int randomX = rand.nextInt(GRID_WIDTH);
        int randomY = rand.nextInt(GRID_HEIGHT);
        Point randomPoint = new Point(randomX, randomY);
        while (snake.contains(randomPoint)) {
            randomX = rand.nextInt(GRID_WIDTH);
            randomY = rand.nextInt(GRID_HEIGHT);
            randomPoint = new Point(randomX, randomY);

        }
        fruit = randomPoint;
    }


    @Override
    public void run() {

        while (true) {

            move();
            repaint();

            try {
                Thread.currentThread();
                Thread.sleep(100);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (direction != Direction.SOUTH) {
                    direction = Direction.NORTH;
                    break;
                }
            case KeyEvent.VK_DOWN:
                if (direction != Direction.NORTH) {
                    direction = Direction.SOUTH;
                    break;
                }
            case KeyEvent.VK_LEFT:
                if (direction != Direction.EAST) {
                    direction = Direction.WEST;
                    break;
                }
            case KeyEvent.VK_RIGHT:
                if (direction != Direction.WEST) {
                    direction = Direction.EAST;
                    break;
                }
        }



    }



    public String getHighScore() {

        FileReader reader = null;
        BufferedReader bufferReader = null;
        try {
            reader = new FileReader("highScore.dat");
            bufferReader = new BufferedReader(reader);
            return  bufferReader.readLine();

        } catch (Exception e){

            return "Nobody: 0";

        } finally {
            try {
                if (bufferReader != null) {
                    bufferReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

}
