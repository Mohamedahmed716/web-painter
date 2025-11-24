package com.painter.web_painter.DTO;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CircleDto.class, name = "circle"),
        @JsonSubTypes.Type(value = RectangleDto.class, name = "rectangle"),
        @JsonSubTypes.Type(value = TriangleDto.class, name = "triangle"),
        @JsonSubTypes.Type(value = LineSegmentDto.class, name = "line"),
        @JsonSubTypes.Type(value = SquareDto.class, name = "square"),
        @JsonSubTypes.Type(value = EllipseDto.class, name = "ellipse")

})

public abstract class ShapeDto {

    private String id;
    private String type;
    private String color;
    private String fillcolor;
    private double x;
    private double y;

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public String getFillcolor() {
        return fillcolor;
    }

    public void setFillcolor(String fillcolor) {
        this.fillcolor = fillcolor;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

