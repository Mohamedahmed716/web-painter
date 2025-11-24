package com.painter.web_painter.controller;
import com.painter.web_painter.DTO.*;
import com.painter.web_painter.model.*;
import org.springframework.stereotype.Component;

import javax.sound.sampled.Line;


@Component


public class ShapeFactory {

    public Shape createShape(ShapeDto dto){
        if (dto instanceof CircleDto){
            CircleDto Cdto =(CircleDto) dto;
            Circle circle =new Circle();
            setCommonProperties(circle,Cdto);
            circle.setRadius(Cdto.getRadius());
            return circle;
        }
        else if (dto instanceof EllipseDto){
            EllipseDto Edto =(EllipseDto) dto;
            Ellipse ellipse =new Ellipse();
            setCommonProperties(ellipse,Edto);
            ellipse.setRadiusX(Edto.getRadiusX());
            ellipse.setRadiusY(Edto.getRadiusY());
            return ellipse;
        }
        else if (dto instanceof LineSegmentDto){
            LineSegmentDto linedto =(LineSegmentDto) dto;
            LineSegment linesegment =new LineSegment();
            setCommonProperties(linesegment,linedto);
            linesegment.setX2(linedto.getX2());
            linesegment.setY2(linedto.getY2());
            return linesegment;
        }
        if (dto instanceof RectangleDto){
            RectangleDto Rdto =(RectangleDto) dto;
            Rectangle rectangle =new Rectangle();
            setCommonProperties(rectangle,Rdto);
            rectangle.setWidth(Rdto.getWidth());
            rectangle.setHeight(Rdto.getHeight());
            return rectangle;
        }
        if (dto instanceof SquareDto) {
            SquareDto sdto = (SquareDto) dto;
            Square square = new Square();
            setCommonProperties(square, sdto);
            square.setSideLength(sdto.getSidelength());
            return square;
        }

        if (dto instanceof TriangleDto) {
            TriangleDto tdto = (TriangleDto) dto;
            Triangle triangle = new Triangle();
            setCommonProperties(triangle, tdto);
            triangle.setX2(tdto.getX2());
            triangle.setX3(tdto.getX3());
            return triangle;
        }
        return null;

    }

    private void setCommonProperties(Shape shape, ShapeDto dto) {
        shape.setId(dto.getId());
        shape.setColor(dto.getColor());
        shape.setFillColor(dto.getFillcolor());
        shape.setX(dto.getX());
        shape.setY(dto.getY());
    }
}
