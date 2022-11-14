package com.example.game;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Player extends Rectangle {
    boolean dead = false;
    final String type;
    private int points;
    Player(int x, int y, int w, int h, String type, Color color) {
        super(w, h, color);
        this.type = type;
        setTranslateX(x);
        setTranslateY(y);
    }
    void moveLeft() {
        setTranslateX(getTranslateX() - 5);
    }
    void moveRight(){
        setTranslateX(getTranslateX() + 5);
    }
    void moveUp(){
        setTranslateY(getTranslateY() - 5);
    }
    void moveDown() {
        setTranslateY(getTranslateY() + 5);
    }
    int getPoints() {
        return this.points;
    }

    void incrementPoints() {
        this.points = this.points + 10;
    }

    void resetPoints() {
        this.points = 0;
    }
}