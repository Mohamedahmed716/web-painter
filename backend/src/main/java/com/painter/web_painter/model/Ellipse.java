package com.painter.web_painter.model;

import java.util.UUID;

public class Ellipse extends Shape {
    private double radiusX;
    private double radiusY;

    public Ellipse() {}

    public Ellipse(double centerX, double centerY, double radiusX, double radiusY, String color, String fillColor) {
        super(UUID.randomUUID().toString(), centerX, centerY, color, fillColor);
        this.radiusX = radiusX;
        this.radiusY = radiusY;
    }

    // Prototype Pattern Constructor
    private Ellipse(Ellipse target) {
        super(UUID.randomUUID().toString(), target.x + 10, target.y + 10, target.color, target.fillColor);
        this.radiusX = target.radiusX;
        this.radiusY = target.radiusY;
    }

    @Override
    public Shape clone() {
        return new Ellipse(this);
    }

    public double getRadiusX() { return radiusX; }
    public void setRadiusX(double radiusX) { this.radiusX = radiusX; }
    public double getRadiusY() { return radiusY; }
    public void setRadiusY(double radiusY) { this.radiusY = radiusY; }
}