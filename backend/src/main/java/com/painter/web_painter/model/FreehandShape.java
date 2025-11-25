package com.painter.web_painter.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FreehandShape extends Shape {
    private List<Map<String, Double>> points = new ArrayList<>();

    public FreehandShape() {
        this.setType("freehand");

    }

    public FreehandShape(List<Map<String, Double>> points, String color, String fillColor) {
        super(UUID.randomUUID().toString(), 0, 0, color, fillColor);
        this.points = points;
        this.setType("freehand");


    }

    // Prototype Copy
    private FreehandShape(FreehandShape target) {
        super(UUID.randomUUID().toString(), target.x + 10, target.y + 10, target.color, target.fillColor);
        this.points = new ArrayList<>(target.points);
        this.setType("freehand");

    }

    @Override
    public Shape clone() {
        return new FreehandShape(this);
    }

    public List<Map<String, Double>> getPoints() { return points; }
    public void setPoints(List<Map<String, Double>> points) { this.points = points; }
}