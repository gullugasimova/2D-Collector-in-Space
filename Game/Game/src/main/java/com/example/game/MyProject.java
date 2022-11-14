package com.example.game;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MyProject extends Application {

    //the main page
    private Pane root;
    //the dialog that shows up when the player is dead
    private Dialog gameOver;
    private Player player;
    //time
    private double t = 0;
    //timer
    private AnimationTimer timer;

    //a method to pass generate ImagePattern which requires image name (source)
    //it helps to avoid repetition
    private ImagePattern setImages(String source){
        Image image = null;
        try {
            image = new Image(getClass().getResource(source).toURI().toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        ImagePattern pattern = new ImagePattern(image);
        return pattern;
    }

    //creates the root pane
    private Parent createContent(Stage stage){
        root = new Pane();
        player = new Player(200, 500, 40, 40, "player", Color.BLUE);
        ImagePattern pattern = setImages("player.jpg");
        player.setFill(pattern);
        //sets size of the root scene
        root.setPrefSize(600,600);
        //add player to the scene
        root.getChildren().add(player);
        timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                update(stage);
            }
        };

        timer.start();
        //adds enemies to the root scene
        addEnemies();
        //adds treasures to the root scene
        addTreasures();
        return root;
    }
    //generates enemies that are added to the root
    private void addEnemies() {
        //in this case only 5 enemies are created
        for (int i = 0; i < 5; i++) {
            Player enemy = new Player(90+ i*100, 150, 50, 50, "enemy", Color.RED);
            ImagePattern pattern = setImages("enemy.jpg");
            enemy.setFill(pattern);
            root.getChildren().add(enemy);
        }
    }

    //generates treasures and adds them to the root
    private void addTreasures() {
        //the positions of the treasures are random
        Random random = new Random();
        //only 10 treasures are created
        for (int i = 0; i < 10; i++) {
            Player treasure = new Player(50 + random.nextInt(10) * 50, random.nextInt(1000), 30, 30, "treasure", Color.GREEN);
            ImagePattern pattern = setImages("treasure.jpeg");
            treasure.setFill(pattern);
            root.getChildren().add(treasure);
        }
    }


    //creates bullets
    private List<Player> bullets(){
        return root.getChildren().stream().map(n -> (Player)n).collect(Collectors.toList());
    }

    //updates the scene based on timer
    private void update(Stage stage){
        t += 0.016;
        bullets().forEach(bullet ->{
            switch (bullet.type){
                //if an enemy bullet, which moves downward, hits the player,
                // they are dead and bullet also goes out of scene and game over
                case "enemybullet":
                    bullet.moveDown();
                    if(bullet.getBoundsInParent().intersects(player.getBoundsInParent())){
                        player.dead = true;
                        bullet.dead = true;
                        //opens the game over dialog because the player is dead
                        lastScene(stage);
                    }
                    break;
                //if the player's bullet, which moves upward, hits an enemy,
                // they are dead and bullet also goes out of scene
                case "playerbullet":
                    bullet.moveUp();
                    bullets().stream().filter(e -> e.type.equals("enemy")).forEach(enemy ->{
                        if(bullet.getBoundsInParent().intersects(enemy.getBoundsInParent())){
                            enemy.dead = true;
                            bullet.dead = true;
                        }
                    });
                    break;
                //if it's a treasure then treasure disappers as soon as player intersects with it
                // this increases the player's points
                case "treasure":
                    bullets().stream().filter(e -> e.type.equals("treasure")).forEach(treasure ->{
                        if(player.getBoundsInParent().intersects(treasure.getBoundsInParent())){
                            treasure.dead = true;
                            player.incrementPoints();
                        }
                    });
                    break;
                //to control the timer of enemies shooting bullets
                case "enemy":
                    if(t > 2){
                        if(Math.random() < 0.3){
                            shoot(bullet);
                        }
                    }
                    break;
            }
        });
        //this is to remove the dead "player" (instance of player class) from the scene
        root.getChildren().removeIf(n ->{
            Player p = (Player) n;
            return p.dead;
        });

        if(t > 2){
            t = 0;
        }
    }
    //creates a bullet and adds to the root
    private void shoot(Player who){
        Player bullet = new Player((int)who.getTranslateX() + 20, (int)who.getTranslateY(), 5, 20, who.type + "bullet", Color.BLUE);
        root.getChildren().add(bullet);
    }

    //starts the game and determines for the player how to move around
    public void startGame(Stage stage) {
        Scene scene = new Scene(createContent(stage));
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                shoot(player);
            } else if (e.getCode() == KeyCode.RIGHT) {
                player.moveRight();
            } else if (e.getCode() == KeyCode.LEFT) {
                player.moveLeft();
            } else if (e.getCode() == KeyCode.UP) {
                player.moveUp();
            } else if (e.getCode() == KeyCode.DOWN) {
                player.moveDown();
            }
        });

        //player has 0 points when the game begins
        player.resetPoints();
        stage.setTitle("Run!");
        //background image
        ImagePattern pattern = setImages("Hubble_ultra_deep_field.jpg");
        scene.setFill(pattern);
        stage.setScene(scene);
        stage.show();
    }

    //opens the dialog if the player is dead
    private void lastScene(Stage stage){
        gameOver = new Dialog();
        Label text = new Label();
        text.setText("" + player.getPoints());
        //displays the points
        gameOver.setContentText("Would you like to restart the game?\n" +
                "Your points: " + text.getText());
        gameOver.setTitle("Game Over");
        //two buttons for the user to decide whether to play again or not
        ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("No. I'm done", ButtonBar.ButtonData.OK_DONE);
        gameOver.getDialogPane().getButtonTypes().addAll(noButton, okButton);
        stopGame();
        gameOver.show();
        //if the dialog is closed the restart the game
        gameOver.setOnCloseRequest(e -> {
            try {
                start(stage);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    //clears the scene when it's game over
    public void stopGame(){
        root.getChildren().clear();
    }

    @Override
    public void start(Stage stage) throws IOException {
        startGame(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}