package com.painter.web_painter.model;

import java.util.UUID;

public class LineSegment extends Shape {
    private double x2;
    private double y2;

    public LineSegment() {
        this.setType("line");
    }

    public LineSegment(double x, double y, double x2, double y2, String color) {
        super(UUID.randomUUID().toString(), x, y, color, "transparent");
        this.x2 = x2;
        this.y2 = y2;
        this.setType("line");
    }

    private LineSegment(LineSegment target) {
        super(UUID.randomUUID().toString(), target.x, target.y, target.color, "transparent");
        this.x2 = target.x2;
        this.y2 = target.y2;
        this.setType("line");
    }

    @Override
    public Shape clone() {
        return new LineSegment(this);
    }

    public double getX2() { return x2; }
    public void setX2(double x2) { this.x2 = x2; }
    public double getY2() { return y2; }
    public void setY2(double y2) { this.y2 = y2; }
}