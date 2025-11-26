package com.painter.web_painter.model;

import java.util.UUID;

public class Rectangle extends Shape {
    private double width;
    private double height;

    public Rectangle() {
        this.setType("rectangle");
    }

    public Rectangle(double x, double y, double width, double height, String color, String fillColor) {
        super(UUID.randomUUID().toString(), x, y, color, fillColor);
        this.width = width;
        this.height = height;
        this.setType("rectangle");
    }

    private Rectangle(Rectangle target) {
        super(UUID.randomUUID().toString(), target.x, target.y, target.color, target.fillColor);
        this.width = target.width;
        this.height = target.height;
        this.setType("rectangle");
        
    }

    @Override
    public Shape clone() {
        return new Rectangle(this);
    }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
}