package org.academiadecodigo.argicultores;
import org.academiadecodigo.argicultores.Objects.*;
import org.academiadecodigo.simplegraphics.graphics.Color;
import org.academiadecodigo.simplegraphics.graphics.Rectangle;
import org.academiadecodigo.simplegraphics.graphics.Text;
import org.academiadecodigo.simplegraphics.keyboard.KeyboardEvent;
import org.academiadecodigo.simplegraphics.keyboard.KeyboardHandler;
import org.academiadecodigo.simplegraphics.pictures.Picture;
import java.util.ArrayList;


public class Game implements KeyboardHandler {
    //Sound stuff
    Sounds bgMusic = new Sounds("resources/bgmusic.wav");
    Sounds shootFX = new Sounds("resources/piu.wav");
    Sounds explosionFX = new Sounds("resources/explosion.wav");
    Sounds gameOverFX = new Sounds("resources/som.wav");
    Sounds shotFX = new Sounds("resources/getHit.wav");
    Sounds menuSound = new Sounds("resources/menuSound.wav");
    //STATES
    private final int menuState = -1;
    private final int playsState = 0;
    private final int overState = 1;
    private final int pauseState = 2;
    private final int instrctState = 3;
    private int gameState;
    //SHAPES
    private final Picture background;
    private final Text gamePaused;
    private final Picture gameMenu;
    private final Picture instrctMenu;
    private final Text gameOverP1;
    private final Text gameOverP2;
    private final Text gameOver2;
    private Picture arrow;
    private final Rectangle blackScreen;
    private final Playership player1ship;
    private final Playership player2ship;
    public static int PADDING = 10;
    public static int BACKGROUNDW;
    public static int BACKGROUNDX;
    ///players
    private final Players[] players = {new Players("Player 1"), new Players("Player 2")};
    private final Hearts[] heartsP1 = new Hearts[5];
    private final Hearts[] heartsP2 = new Hearts[5];
    private int hearts1Start = 0;
    private int hearts2End = 4;
    //Object Arrays
    private final ArrayList<Asteroids> asteroids = new ArrayList<>();
    private final ArrayList<Bullets> bullets1 = new ArrayList<>();
    private final ArrayList<Bullets> bullets2 = new ArrayList<>();


    public Game() {
        //Keyboard
        new Players.Pkeyboard(this);
        //// bg
        background = new Picture(PADDING, PADDING, "resources/background.png");background.draw();
        gameState = menuState;
        BACKGROUNDW = background.getWidth();
        arrow = new Picture(350,290, "resources/arrow.png");
        ///playerships
        player1ship = new Playership(BACKGROUNDW / 2, PADDING + 11, "resources/player2ship.png");player1ship.draw();
        player2ship = new Playership(BACKGROUNDW / 2, (background.getHeight() + 1) - 50, "resources/player1ship.png");player2ship.draw();
        //shapes
        gamePaused = new Text((BACKGROUNDW / 2) - PADDING, background.getHeight() / 2, "GAME PAUSED");gamePaused.setColor(Color.WHITE);gamePaused.grow(50, 20);
        gameMenu = new Picture(PADDING, PADDING, "resources/startmenu.png");
        gameOverP1 = new Text((BACKGROUNDW / 2) + 5, background.getHeight() / 2, "CYAN PLAYER WON!");gameOverP1.setColor(Color.CYAN);gameOverP1.grow(50, 20);
        gameOverP2 = new Text((BACKGROUNDW / 2) + 8, background.getHeight() / 2, "RED PLAYER WON!");gameOverP2.setColor(Color.RED);gameOverP2.grow(50, 20);
        gameOver2 = new Text((BACKGROUNDW / 2) - 6, gameOverP1.getY() + 50, "Press ESC to restart");gameOver2.setColor(Color.WHITE);gameOver2.grow(40, 10);
        blackScreen = new Rectangle(PADDING, PADDING, BACKGROUNDW, background.getHeight());
        instrctMenu = new Picture(PADDING,PADDING, "resources/instructions.png");
    }

    public void init() {
        createAsteroids();
        createHearts();
    }

    private void createAsteroids() {
        for (int i = 0; i < 10; i++) {
            Direction direction;
            double randomNum = Math.random();
            int x;
            int y = (int) (Math.random() * ((background.getY() + 90) - (background.getHeight() - 90))) + (background.getHeight() - 90);

            if (randomNum < 0.5) {
                direction = Direction.LEFT;
            } else {
                direction = Direction.RIGHT;
            }
            if (direction == Direction.LEFT) {
                x = BACKGROUNDW - 20;
            } else {
                x = background.getX();
            }
            double rand = Math.random();
            if(rand < 0.03){
            asteroids.add(new Asteroids(x, y, "resources/alien.png", direction, "Ship"));asteroids.get(i).draw();
            }else
                asteroids.add(new Asteroids(x, y, "resources/asteroids.png", direction, "Normal"));asteroids.get(i).draw();
        }
    }

    private void createHearts() {
        int x = 20;
        for (int i = 0; i < 5; i++) {
            heartsP2[i] = new Hearts(background.getX() + x, background.getHeight() - 20 , "resources/heart.png");
            heartsP2[i].draw();
            x +=22;
        }
        for (int i = 0; i < 5; i++) {
                heartsP1[i] = new Hearts(BACKGROUNDW - x, background.getY() + PADDING, "resources/heart.png");
                heartsP1[i].draw();
                x -=22;
        }
    }


    public void start() throws InterruptedException {
        boolean gameOverSound = false;
        boolean backgroundSound = false;
        while (true) {

            // Pause for a while
            Thread.sleep(20);
            if(players[0].isDead()){
                if(!gameOverSound){
                    gameOverFX.play();
                    gameOverSound = true;
                }
                gameState = overState;
                blackScreen.fill();
                gameOverP1.draw();
                gameOver2.draw();
            }else if(players[1].isDead()){
                if(!gameOverSound){
                    gameOverFX.play();
                    gameOverSound = true;
                }
                gameState = overState;
                blackScreen.fill();
                gameOverP2.draw();
                gameOver2.draw();
            }

            if(gameState == menuState){
                gameOverSound = false;
                if(backgroundSound) {
                    backgroundSound = false;
                    bgMusic.stop();
                }
                gameMenu.draw();
                arrow.draw();
            }

            if(gameState == instrctState){
                instrctMenu.draw();
            }

            if (gameState == playsState) {
                if(!backgroundSound) {
                    backgroundSound = true;
                    bgMusic.play();
                }
                gameOverSound = false;
                // Move asteroids && bullets
                moveObjects();

                //check asteroid && bullet limits
                checkLimit();

                //check bullet collisions
                checkCollisions();
            }

        }

    }


    public void checkLimit() {
        Bullets toremoveB1= null;
        Bullets toremoveB2 = null;
        for (Asteroids asteroid : asteroids) {
            if (asteroid.getX() >= BACKGROUNDW - 50) {
                asteroid.setDirection(Direction.LEFT);
            }
            if (asteroid.getX() <= background.getX() + 20) {
                asteroid.setDirection(Direction.RIGHT);
            }
        }

        for (Bullets bullet : bullets1) {
            if ((bullet.getY() >= background.getHeight() - 20)) {
                bullet.delete();
                toremoveB1 = bullet;
            }
            if (bullet.getY() <= background.getY() + 10) {
                bullet.delete();
                toremoveB1 = bullet;
            }
        }

        for (Bullets bullet : bullets2) {
            if ((bullet.getY() >= background.getHeight() - 20)) {
                bullet.delete();
                toremoveB2 = bullet;
            }
            if (bullet.getY() <= background.getY() + 10) {
                bullet.delete();
                toremoveB2 = bullet;
            }
        }
        bullets1.remove(toremoveB1);
        bullets2.remove(toremoveB2);
    }

    public void checkCollisions() throws InterruptedException {
        Asteroids toRemoveA = null;
        Bullets toRemoveB1 = null;
        Bullets toRemoveB2 = null;

        for (Bullets bullet : bullets1) {
            for (Asteroids asteroid : asteroids) {
                if (bullet.intersects(asteroid)) {
                    if(asteroid.getType() == "Ship"){
                        players[0].setPowerShot(true);
                    }
                    toRemoveB1 = bullet;
                    bullet.delete();
                    toRemoveA = asteroid;
                    asteroid.delete();
                    explosionFX.play();
                }
            }
        }

        for (Bullets bullet : bullets2) {
            for (Asteroids asteroid : asteroids) {
                if (bullet.intersects(asteroid)) {
                    if(asteroid.getType() == "Ship"){
                        players[1].setPowerShot(true);
                    }
                    toRemoveB2 = bullet;
                    bullet.delete();
                    toRemoveA = asteroid;
                    asteroid.delete();
                    explosionFX.play();
                }
            }
        }

        for (int i = 0; i < bullets1.size(); i++) {
            if (bullets1.get(i).intersects(player2ship)) {
                toRemoveB1 = bullets1.get(i);
                players[0].hit();
                System.out.println("player1 bullet hit p2");
                heartsP2[hearts2End].delete();
                heartsP2[hearts2End] = null;
                hearts2End--;
                Thread.sleep(20);
                bullets1.get(i).delete();
                shotFX.play();
            }
        }

        for (int i = 0; i < bullets2.size(); i++) {
            if (bullets2.get(i).intersects(player1ship)) {
                toRemoveB2 = bullets2.get(i);
                players[1].hit();
                System.out.println("player2 bullet hit p1");
                heartsP1[hearts1Start].delete();
                heartsP1[hearts1Start] = null;
                hearts1Start++;
                Thread.sleep(20);
                bullets2.get(i).delete();
                shotFX.play();
            }
        }

        bullets1.remove(toRemoveB1);
        bullets2.remove(toRemoveB2);
        asteroids.remove(toRemoveA);

        if (asteroids.size() == 6) {
            createAsteroids();
        }
    }

    public void resetGame(){
        for (int i = 0; i < bullets2.size(); i++) {
            bullets2.get(i).delete();
            bullets2.remove(bullets2.get(i));
        }
        for (int i = 0; i < bullets1.size(); i++) {
            bullets1.get(i).delete();
            bullets1.remove(bullets1.get(i));
        }
        for (int i = 0; i < heartsP1.length; i++) {
            if(heartsP1[i] != null) {
                heartsP1[i].delete();
                heartsP1[i] = null;
            }
        }
        for (int i = 0; i < heartsP2.length; i++) {
            if(heartsP2[i] != null) {
                heartsP2[i].delete();
                heartsP2[i] = null;
            }
        }

        hearts1Start = 0;
        hearts2End = 4;
        createHearts();
        players[0].setDead(false);
        players[0].resetHp();
        players[1].setDead(false);
        players[1].resetHp();
        blackScreen.delete();
        gameOver2.delete();
        gameOverP1.delete();
        gameOverP2.delete();
        gameState = playsState;
    }

    private void moveObjects() {
        for (Bullets bullet : bullets1) {
            bullet.move();
        }
        for (Bullets bullet : bullets2) {
            bullet.move();
        }
        for (Asteroids asteroid : asteroids) {
            asteroid.move();
        }
        player1ship.move();
        player2ship.move();
    }


    @Override
    public void keyPressed(KeyboardEvent keyboardEvent) {
        switch (keyboardEvent.getKey()) {
            case KeyboardEvent.KEY_DOWN:
                if(!(arrow.getY() == 530)) {
                    arrow.translate(0, 120);
                    menuSound.play();
                }
                break;
            case KeyboardEvent.KEY_UP:
                if(!(arrow.getY() == 290)) {
                    arrow.translate(0, -120);
                    menuSound.play();
                }
                break;
            case KeyboardEvent.KEY_RIGHT:
                if(gameState == playsState) {
                    player1ship.setDirection(Direction.RIGHT);
                }
                break;
            case KeyboardEvent.KEY_LEFT:
                if(gameState == playsState) {
                    player1ship.setDirection(Direction.LEFT);
                    break;
                }
            case KeyboardEvent.KEY_A:
                if(gameState == playsState) {
                    player2ship.setDirection(Direction.LEFT);
                }
                break;
            case KeyboardEvent.KEY_D:
                if(gameState == playsState) {
                    player2ship.setDirection(Direction.RIGHT);
                }
                break;
            case KeyboardEvent.KEY_W:
                if(bullets2.size() == 0 && gameState == playsState) {
                        shootFX.play();
                        bullets2.add(new Bullets(player2ship.getX() + (player2ship.getWidth() / 2) - 5, player2ship.getY(), "resources/player1bala.png", Direction.DOWN));
                        bullets2.get(0).draw();
                }
                break;
            case KeyboardEvent.KEY_SHIFT:
                if(bullets1.size() == 0 && gameState == playsState) {
                    shootFX.play();
                    bullets1.add(new Bullets(player1ship.getX() + (player1ship.getWidth() / 2) - 5, player1ship.getY() + 20, "resources/player2bala.png", Direction.UP));
                    bullets1.get(0).draw();
                }
                break;
            case KeyboardEvent.KEY_ENTER:
                if (arrow.getY() == 290 && gameState == menuState){
                    menuSound.play();
                    arrow.delete();
                    gameMenu.delete();
                    blackScreen.delete();
                    gameState = playsState;
                }else  if (arrow.getY() == 530 && gameState == menuState){
                    System.exit(1);
                }else  if (arrow.getY() == 410 && gameState == menuState){
                    menuSound.play();
                    arrow.delete();
                    gameMenu.delete();
                    gameState = instrctState;
                }
                break;
            case KeyboardEvent.KEY_ESC:
                if (gameState == playsState) {
                    gameState = pauseState;
                    gamePaused.draw();
                } else if (gameState == pauseState) {
                    gamePaused.delete();
                    gameState = playsState;
                }else if(gameState == overState) {
                   resetGame();
                }else if(gameState == instrctState) {
                    menuSound.play();
                    instrctMenu.delete();
                    gameState = menuState;
                }
                break;
            case KeyboardEvent.KEY_M:
                if (gameState == pauseState || gameState == playsState) {
                    gamePaused.delete();
                    resetGame();
                    gameState = menuState;
                    gameMenu.draw();
                    arrow.draw();
                    menuSound.play();
                }else if(gameState == instrctState) {
                    menuSound.play();
                    instrctMenu.delete();
                    gameState = menuState;
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyboardEvent keyboardEvent) {
    }

}
