package com.painter.web_painter.model;

import java.util.UUID;


public class Circle extends Shape {
    private double radius;

    public Circle() {} // Empty constructor for JSON

    public Circle(double x, double y, double radius, String color, String fillColor) {
        super(UUID.randomUUID().toString(), x, y, color, fillColor);
        this.radius = radius;
    }
    private Circle(Circle target) {
        super(UUID.randomUUID().toString(), target.x, target.y, target.color, target.fillColor);
        this.radius = target.radius;
    }

    @Override
    public Shape clone() {
        return new Circle(this);
    }

    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }
}