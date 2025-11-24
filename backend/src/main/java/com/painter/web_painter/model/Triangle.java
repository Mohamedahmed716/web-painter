package com.painter.web_painter.model;

import java.util.UUID;

public class Triangle extends Shape {
    // We store the 3 points of the triangle relative to the bounding box
    private double x2, y2;
    private double x3, y3;

    public Triangle() {}

    public Triangle(double x1, double y1, double x2, double y2, double x3, double y3, String color, String fillColor) {
        super(UUID.randomUUID().toString(), x1, y1, color, fillColor);
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
    }

    private Triangle(Triangle target) {
        super(UUID.randomUUID().toString(), target.x + 10, target.y + 10, target.color, target.fillColor);
        this.x2 = target.x2 + 10;
        this.y2 = target.y2 + 10;
        this.x3 = target.x3 + 10;
        this.y3 = target.y3 + 10;
    }

    @Override
    public Shape clone() {
        return new Triangle(this);
    }

    public double getX2() { return x2; }
    public void setX2(double x2) { this.x2 = x2; }
    public double getY2() { return y2; }
    public void setY2(double y2) { this.y2 = y2; }
    public double getX3() { return x3; }
    public void setX3(double x3) { this.x3 = x3; }
    public double getY3() { return y3; }
    public void setY3(double y3) { this.y3 = y3; }
}