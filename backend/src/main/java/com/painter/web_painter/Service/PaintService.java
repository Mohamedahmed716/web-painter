package com.painter.web_painter.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

import com.painter.web_painter.model.Shape;

@Service
public class PaintService {
    private List<Shape> shapes = new ArrayList<>();
    private Stack<List<Shape>> undoStack = new Stack<>();
    private Stack<List<Shape>> redoStack = new Stack<>();
    // PaintService.java
    private String selectedShapeId = null;
    private String selectedId = null;

    private boolean moving = false;              // drag in progress?
    private List<Shape> moveSnapshot = null;     // undo snapshot for whole drag


    public List<Shape> getShapes() {
        return shapes;
    }

    public void addShape(Shape shape) {
        deepCopy(undoStack);
        shapes.add(shape);
        redoStack.clear();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        deepCopy(redoStack);
        shapes = undoStack.pop();
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        deepCopy(undoStack);
        shapes = redoStack.pop();
    }

    private void deepCopy(Stack<List<Shape>> stack) {
        List<Shape> snapshot = new ArrayList<>();
        for (Shape s : shapes) {
            snapshot.add(s.clone());
        }
        stack.push(snapshot);
    }

    public void clearBoard() {
        deepCopy(undoStack);
        shapes.clear();
        redoStack.clear();
    }

    public String SavetoJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(shapes);
    }

    public String SavetoXml() throws IOException {
        XmlMapper mapper = new XmlMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(shapes);
    }

    public void loadFromFile(MultipartFile file) throws IOException {
        String content = new String(file.getBytes());
        String fileName = file.getOriginalFilename();
        List<Shape> loadedShapes;

        if (fileName != null && fileName.endsWith(".xml")) {
            XmlMapper xmlMapper = new XmlMapper();
            loadedShapes = xmlMapper.readValue(content, new TypeReference<List<Shape>>(){});
        } else {
            ObjectMapper jsonMapper = new ObjectMapper();
            loadedShapes = jsonMapper.readValue(content, new TypeReference<List<Shape>>(){});
        }

        this.clearBoard();
        if (loadedShapes != null) {
            this.shapes.addAll(loadedShapes);
        }
    }
    // PaintService.java

    private Shape getShapeById(String id) {
        if (id == null) return null;
        for (Shape s : shapes) {
            if (id.equals(s.getId())) {
                return s;
            }
        }
        return null;
    }

    private Shape findShapeAt(double px, double py) {
        // iterate from topmost (last drawn) to bottom
        for (int i = shapes.size() - 1; i >= 0; i--) {
            Shape s = shapes.get(i);
            if (hitTest(s, px, py)) {
                return s;
            }
        }
        return null;
    }

    private boolean hitTest(Shape shape, double px, double py) {
        if (shape instanceof com.painter.web_painter.model.Rectangle) {
            com.painter.web_painter.model.Rectangle r = (com.painter.web_painter.model.Rectangle) shape;
            double x = r.getX();
            double y = r.getY();
            double w = r.getWidth();
            double h = r.getHeight();
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }

        if (shape instanceof com.painter.web_painter.model.Square) {
            com.painter.web_painter.model.Square s = (com.painter.web_painter.model.Square) shape;
            double x = s.getX();
            double y = s.getY();
            double side = s.getSideLength();
            return px >= x && px <= x + side && py >= y && py <= y + side;
        }

        if (shape instanceof com.painter.web_painter.model.Circle) {
            com.painter.web_painter.model.Circle c = (com.painter.web_painter.model.Circle) shape;
            double dx = px - c.getX();
            double dy = py - c.getY();
            double r = c.getRadius();
            return dx * dx + dy * dy <= r * r;
        }

        if (shape instanceof com.painter.web_painter.model.Ellipse) {
            com.painter.web_painter.model.Ellipse e = (com.painter.web_painter.model.Ellipse) shape;
            double dx = (px - e.getX()) / e.getRadiusX();
            double dy = (py - e.getY()) / e.getRadiusY();
            return dx * dx + dy * dy <= 1.0;
        }

        if (shape instanceof com.painter.web_painter.model.LineSegment) {
            com.painter.web_painter.model.LineSegment l = (com.painter.web_painter.model.LineSegment) shape;
            double x1 = l.getX();
            double y1 = l.getY();
            double x2 = l.getX2();
            double y2 = l.getY2();
            double dx = x2 - x1;
            double dy = y2 - y1;
            double lengthSq = dx * dx + dy * dy;
            if (lengthSq == 0) return false;
            double t = ((px - x1) * dx + (py - y1) * dy) / lengthSq;
            t = Math.max(0, Math.min(1, t));
            double projX = x1 + t * dx;
            double projY = y1 + t * dy;
            double distSq = (px - projX) * (px - projX) + (py - projY) * (py - projY);
            double tolerance = 5.0;
            return distSq <= tolerance * tolerance;
        }

        if (shape instanceof com.painter.web_painter.model.Triangle) {
            com.painter.web_painter.model.Triangle t = (com.painter.web_painter.model.Triangle) shape;
            double x1 = t.getX();
            double y1 = t.getY();
            double x2 = t.getX2();
            double y2 = t.getY2();
            double x3 = t.getX3();
            double y3 = t.getY3();

            double denom = ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));
            if (denom == 0) return false;
            double a = ((y2 - y3) * (px - x3) + (x3 - x2) * (py - y3)) / denom;
            double b = ((y3 - y1) * (px - x3) + (x1 - x3) * (py - y3)) / denom;
            double cBary = 1 - a - b;
            return a >= 0 && b >= 0 && cBary >= 0;
        }

        if (shape instanceof com.painter.web_painter.model.FreehandShape) {
            com.painter.web_painter.model.FreehandShape f = (com.painter.web_painter.model.FreehandShape) shape;
            List<Map<String, Double>> pts = f.getPoints();
            if (pts == null || pts.size() < 2) return false;
            double tolerance = 5.0;
            for (int i = 0; i < pts.size() - 1; i++) {
                double x1 = pts.get(i).getOrDefault("x", 0.0);
                double y1 = pts.get(i).getOrDefault("y", 0.0);
                double x2 = pts.get(i + 1).getOrDefault("x", 0.0);
                double y2 = pts.get(i + 1).getOrDefault("y", 0.0);
                double dx = x2 - x1;
                double dy = y2 - y1;
                double lengthSq = dx * dx + dy * dy;
                if (lengthSq == 0) continue;
                double t = ((px - x1) * dx + (py - y1) * dy) / lengthSq;
                t = Math.max(0, Math.min(1, t));
                double projX = x1 + t * dx;
                double projY = y1 + t * dy;
                double distSq = (px - projX) * (px - projX) + (py - projY) * (py - projY);
                if (distSq <= tolerance * tolerance) return true;
            }
            return false;
        }

        return false;
    }
    // PaintService.java
    public void selectShapeAt(double x, double y) {
        Shape s = findShapeAt(x, y);
        if (s != null) {
            selectedShapeId = s.getId();
        } else {
            selectedShapeId = null;
        }
    }
    // PaintService.java
    public void moveSelected(double dx, double dy) {
        if (selectedShapeId == null) return;

        Shape s = getShapeById(selectedShapeId);
        if (s == null) return;

        deepCopy(undoStack);
        redoStack.clear();

        if (s instanceof com.painter.web_painter.model.Rectangle) {
            com.painter.web_painter.model.Rectangle r = (com.painter.web_painter.model.Rectangle) s;
            r.setX(r.getX() + dx);
            r.setY(r.getY() + dy);
        } else if (s instanceof com.painter.web_painter.model.Square) {
            com.painter.web_painter.model.Square sq = (com.painter.web_painter.model.Square) s;
            sq.setX(sq.getX() + dx);
            sq.setY(sq.getY() + dy);
        } else if (s instanceof com.painter.web_painter.model.Circle) {
            com.painter.web_painter.model.Circle c = (com.painter.web_painter.model.Circle) s;
            c.setX(c.getX() + dx);
            c.setY(c.getY() + dy);
        } else if (s instanceof com.painter.web_painter.model.Ellipse) {
            com.painter.web_painter.model.Ellipse e = (com.painter.web_painter.model.Ellipse) s;
            e.setX(e.getX() + dx);
            e.setY(e.getY() + dy);
        } else if (s instanceof com.painter.web_painter.model.LineSegment) {
            com.painter.web_painter.model.LineSegment l = (com.painter.web_painter.model.LineSegment) s;
            l.setX(l.getX() + dx);
            l.setY(l.getY() + dy);
            l.setX2(l.getX2() + dx);
            l.setY2(l.getY2() + dy);
        } else if (s instanceof com.painter.web_painter.model.Triangle) {
            com.painter.web_painter.model.Triangle t = (com.painter.web_painter.model.Triangle) s;
            t.setX(t.getX() + dx);
            t.setY(t.getY() + dy);
            t.setX2(t.getX2() + dx);
            t.setY2(t.getY2() + dy);
            t.setX3(t.getX3() + dx);
            t.setY3(t.getY3() + dy);
        } else if (s instanceof com.painter.web_painter.model.FreehandShape) {
            com.painter.web_painter.model.FreehandShape f = (com.painter.web_painter.model.FreehandShape) s;
            List<Map<String, Double>> pts = f.getPoints();
            if (pts != null) {
                for (Map<String, Double> p : pts) {
                    p.put("x", p.get("x") + dx);
                    p.put("y", p.get("y") + dy);
                }
            }
        }
    }
    // PaintService.java
    public void resizeSelected(String anchor, double dx, double dy) {
        if (selectedShapeId == null) return;

        Shape s = getShapeById(selectedShapeId);
        if (s == null) return;

        deepCopy(undoStack);
        redoStack.clear();

        if (s instanceof com.painter.web_painter.model.Rectangle) {
            com.painter.web_painter.model.Rectangle r = (com.painter.web_painter.model.Rectangle) s;
            double newW = r.getWidth();
            double newH = r.getHeight();
            if ("right".equals(anchor) || "both".equals(anchor)) {
                newW += dx;
            }
            if ("bottom".equals(anchor) || "both".equals(anchor)) {
                newH += dy;
            }
            r.setWidth(Math.max(1, newW));
            r.setHeight(Math.max(1, newH));
        } else if (s instanceof com.painter.web_painter.model.Square) {
            com.painter.web_painter.model.Square sq = (com.painter.web_painter.model.Square) s;
            double side = sq.getSideLength() + Math.max(dx, dy);
            sq.setSideLength(Math.max(1, side));
        } else if (s instanceof com.painter.web_painter.model.Circle) {
            com.painter.web_painter.model.Circle c = (com.painter.web_painter.model.Circle) s;
            double r = c.getRadius() + dx; // use dx as radial change
            c.setRadius(Math.max(1, r));
        } else if (s instanceof com.painter.web_painter.model.Ellipse) {
            com.painter.web_painter.model.Ellipse e = (com.painter.web_painter.model.Ellipse) s;
            e.setRadiusX(Math.max(1, e.getRadiusX() + dx));
            e.setRadiusY(Math.max(1, e.getRadiusY() + dy));
        }
        // You can extend this to lines/triangles/freehand if you want more advanced resizing
    }
    // PaintService.java
    public void copySelected() {
        if (selectedShapeId == null) return;
        Shape s = getShapeById(selectedShapeId);
        if (s == null) return;

        deepCopy(undoStack);
        redoStack.clear();

        Shape copy = s.clone();
        // small offset so it’s visible
        copy.setX(copy.getX() + 10);
        copy.setY(copy.getY() + 10);
        shapes.add(copy);

        selectedShapeId = copy.getId();
    }

    public void deleteSelected() {
        if (selectedShapeId == null) return;
        Shape s = getShapeById(selectedShapeId);
        if (s == null) return;

        deepCopy(undoStack);
        redoStack.clear();

        shapes.remove(s);
        selectedShapeId = null;
    }
    public void startMove() {
        if (!moving) {
            moving = true;

            // Take ONE snapshot at start of dragging
            moveSnapshot = new ArrayList<>();
            for (Shape s : shapes) {
                moveSnapshot.add(s.clone());
            }
        }
    }
    public void endMove() {
        if (moving) {
            moving = false;

            // Push snapshot ONCE to undo stack
            undoStack.push(moveSnapshot);
            redoStack.clear();

            moveSnapshot = null;
        }
    }







}