package com.painter.web_painter.model;

import java.util.UUID;

public class Square extends Shape {
    private double sideLength;

    public Square() {
        this.setType("square");

    }

    public Square(double x, double y, double sideLength, String color, String fillColor) {
        super(UUID.randomUUID().toString(), x, y, color, fillColor);
        this.sideLength = sideLength;
        this.setType("square");

    }

    private Square(Square target) {
        super(UUID.randomUUID().toString(), target.x, target.y, target.color, target.fillColor);
        this.sideLength = target.sideLength;
        this.setType("square");

    }

    @Override
    public Shape clone() {
        return new Square(this);
    }

    public double getSideLength() { return sideLength; }
    public void setSideLength(double sideLength) { this.sideLength = sideLength; }
}