package com.painter.web_painter.model;

import java.util.UUID;

public class Square extends Shape {
    private double sideLength;

    public Square() {}

    public Square(double x, double y, double sideLength, String color, String fillColor) {
        super(UUID.randomUUID().toString(), x, y, color, fillColor);
        this.sideLength = sideLength;
    }

    private Square(Square target) {
        super(UUID.randomUUID().toString(), target.x + 10, target.y + 10, target.color, target.fillColor);
        this.sideLength = target.sideLength;
    }

    @Override
    public Shape clone() {
        return new Square(this);
    }

    public double getSideLength() { return sideLength; }
    public void setSideLength(double sideLength) { this.sideLength = sideLength; }
}