package com.painter.web_painter.Service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.painter.web_painter.model.Circle;
import com.painter.web_painter.model.Ellipse;
import com.painter.web_painter.model.FreehandShape;
import com.painter.web_painter.model.LineSegment;
import com.painter.web_painter.model.Rectangle;
import com.painter.web_painter.model.Shape;
import com.painter.web_painter.model.Square;
import com.painter.web_painter.model.Triangle;

@Component
public class ShapeFactory {

    public Shape createShape(String type, Map<String, Object> params) {
        // Coordinates from Frontend
        double x1 = Double.parseDouble(params.get("x1").toString());
        double y1 = Double.parseDouble(params.get("y1").toString());
        double x2 = Double.parseDouble(params.get("x2").toString());
        double y2 = Double.parseDouble(params.get("y2").toString());
        
        String color = (String) params.get("color");
        String fillColor = "transparent";

        switch (type.toLowerCase()) {
            case "circle":
                double radius = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                return new Circle(x1, y1, radius, color, fillColor);

            case "rectangle":
                double width = Math.abs(x2 - x1);
                double height = Math.abs(y2 - y1);
                double rectX = Math.min(x1, x2);
                double rectY = Math.min(y1, y2);
                return new Rectangle(rectX, rectY, width, height, color, fillColor);

            case "square":
                double w = Math.abs(x2 - x1);
                double h = Math.abs(y2 - y1);
                double side = Math.max(w, h);
                double sqX = Math.min(x1, x2);
                double sqY = Math.min(y1, y2);
                return new Square(sqX, sqY, side, color, fillColor);
            
            case "ellipse":
                double radX = Math.abs(x2 - x1) / 2.0;
                double radY = Math.abs(y2 - y1) / 2.0;
                double cenX = Math.min(x1, x2) + radX;
                double cenY = Math.min(y1, y2) + radY;
                return new Ellipse(cenX, cenY, radX, radY, color, fillColor);

            case "triangle":
                double tW = Math.abs(x2 - x1);
                double tH = Math.abs(y2 - y1);
                double topX = Math.min(x1, x2) + (tW / 2.0);
                double topY = Math.min(y1, y2);
                
                double botLeftX = Math.min(x1, x2);
                double botLeftY = Math.min(y1, y2) + tH;
                
                double botRightX = Math.min(x1, x2) + tW;
                double botRightY = Math.min(y1, y2) + tH;
                
                return new Triangle(topX, topY, botLeftX, botLeftY, botRightX, botRightY, color, fillColor);

            case "line":
                return new LineSegment(x1, y1, x2, y2, color);

            case "freehand":
            List<Map<String, Double>> points = (List<Map<String, Double>>) params.get("points");
            return new FreehandShape(points, color, fillColor);    
            
            default:
                throw new IllegalArgumentException("Unknown shape type: " + type);
        }
    }
}
