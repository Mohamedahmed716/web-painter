package com.painter.web_painter.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, 
    include = JsonTypeInfo.As.PROPERTY, 
    property = "type",
        visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Circle.class, name = "circle"),
    @JsonSubTypes.Type(value = Rectangle.class, name = "rectangle"),
    @JsonSubTypes.Type(value = Square.class, name = "square"),
    @JsonSubTypes.Type(value = LineSegment.class, name = "line"),
    @JsonSubTypes.Type(value = Ellipse.class, name = "ellipse"),
    @JsonSubTypes.Type(value = Triangle.class, name = "triangle"),
    @JsonSubTypes.Type(value = FreehandShape.class, name = "freehand")

})
public abstract class Shape implements Cloneable {
    protected String type;
    protected String id;
    protected double x;
    protected double y;
    protected String color;
    protected String fillColor;

    public Shape() {}

    public Shape(String id, double x, double y, String color, String fillColor) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.color = color;
        this.fillColor = fillColor;
    }

    public abstract Shape clone();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getFillColor() { return fillColor; }
    public void setFillColor(String fillColor) { this.fillColor = fillColor; }
}