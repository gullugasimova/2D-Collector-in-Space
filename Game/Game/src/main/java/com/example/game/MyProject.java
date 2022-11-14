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

    private Pane root;
    private Dialog gameOver;
    private Player player;
    private double t = 0;

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

    private Parent createContent(Stage stage){
        root = new Pane();
        player = new Player(200, 500, 40, 40, "player", Color.BLUE);
        ImagePattern pattern = setImages("player.jpg");
        player.setFill(pattern);
        root.setPrefSize(600,600);
        root.getChildren().add(player);
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                update(stage);
            }
        };

        timer.start();
        addEnemies();
        addTreasures();
        player.getPoints();
        return root;
    }
    private void addEnemies() {
        for (int i = 0; i < 5; i++) {
            Player enemy = new Player(90+ i*100, 150, 50, 50, "enemy", Color.RED);
            ImagePattern pattern = setImages("enemy.jpg");
            enemy.setFill(pattern);
            root.getChildren().add(enemy);
        }
    }

    private void addTreasures() {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Player treasure = new Player(50 + random.nextInt(10) * 50, random.nextInt(1000), 30, 30, "treasure", Color.GREEN);
            ImagePattern pattern = setImages("treasure.jpeg");
            treasure.setFill(pattern);
            root.getChildren().add(treasure);
        }
    }

    private List<Player> sprites(){
        return root.getChildren().stream().map(n -> (Player)n).collect(Collectors.toList());
    }
    private void update(Stage stage){
        t += 0.016;
        sprites().forEach(bullet ->{
            switch (bullet.type){
                case "enemybullet":
                    bullet.moveDown();
                    if(bullet.getBoundsInParent().intersects(player.getBoundsInParent())){
                        player.dead = true;
                        bullet.dead = true;
                        lastScene(stage);
                    }
                    break;
                case "playerbullet":
                    bullet.moveUp();
                    sprites().stream().filter(e -> e.type.equals("enemy")).forEach(enemy ->{
                        if(bullet.getBoundsInParent().intersects(enemy.getBoundsInParent())){
                            enemy.dead = true;
                            bullet.dead = true;
                        }
                    });
                    break;
                case "treasure":
                    sprites().stream().filter(e -> e.type.equals("treasure")).forEach(treasure ->{
                        if(player.getBoundsInParent().intersects(treasure.getBoundsInParent())){
                            treasure.dead = true;
                            player.incrementPoints();
                        }
                    });
                    break;
                case "enemy":
                    if(t > 2){
                        if(Math.random() < 0.3){
                            shoot(bullet);
                        }
                    }
                    break;
            }
        });
        root.getChildren().removeIf(n ->{
            Player p = (Player) n;
            return p.dead;
        });

        if(t > 2){
            t = 0;
        }
    }
    private void shoot(Player who){
        Player bullet = new Player((int)who.getTranslateX() + 20, (int)who.getTranslateY(), 5, 20, who.type + "bullet", Color.BLUE);
        root.getChildren().add(bullet);
    }

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

        player.resetPoints();
        stage.setTitle("Run!");
        ImagePattern pattern = setImages("Hubble_ultra_deep_field.jpg");
        scene.setFill(pattern);
        stage.setScene(scene);
        stage.show();
    }
    private void lastScene(Stage stage){
        gameOver = new Dialog();
        Label text = new Label();
        text.setText("" + player.getPoints());
        gameOver.setContentText("Would you like to restart the game?\n" +
                "Your points: " + text.getText());
        gameOver.setTitle("Game Over");
        ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("No. I'm done", ButtonBar.ButtonData.OK_DONE);
        gameOver.getDialogPane().getButtonTypes().addAll(noButton, okButton);
        stopGame();
        gameOver.show();
        gameOver.setOnCloseRequest(e -> {
            try {
                start(stage);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

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