package com.painter.web_painter.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.UUID;
import java.util.Map;
import com.painter.web_painter.model.*;

@Service
public class PaintService {
    private List<Shape> shapes = new ArrayList<>();
    private Stack<List<Shape>> undoStack = new Stack<>();
    private Stack<List<Shape>> redoStack = new Stack<>();
    
    private String selectedShapeId = null;
    private String clipboardShapeId = null; 
    private List<Shape> moveSnapshot = null;

    public List<Shape> getShapes() {
        return shapes;
    }

    public void addShape(Shape shape) {
        if (shape == null) return;
        saveStateToUndo();
        shapes.add(shape);
        redoStack.clear();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        saveStateToRedo();
        shapes = undoStack.pop();
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        saveStateToUndo();
        shapes = redoStack.pop();
    }

    public void selectShapeAt(double x, double y) {
        Shape found = null;
        for (int i = shapes.size() - 1; i >= 0; i--) {
            if (hitTest(shapes.get(i), x, y)) {
                found = shapes.get(i);
                break;
            }
        }
        selectedShapeId = (found != null) ? found.getId() : null;
    }

    public void startMove() {
        moveSnapshot = new ArrayList<>();
        for (Shape s : shapes) moveSnapshot.add(s.clone());
    }

    public void moveSelected(double dx, double dy) {
        if (selectedShapeId == null) return;
        Shape s = getShapeById(selectedShapeId);
        if (s != null) moveShape(s, dx, dy);
    }

    public void endMove() {
        if (moveSnapshot != null) {
            undoStack.push(moveSnapshot);
            redoStack.clear();
            moveSnapshot = null;
        }
    }

    public void copySelected() {
        if (selectedShapeId != null) {
            clipboardShapeId = selectedShapeId;
        }
    }

    public void pasteSelected(double x, double y) {
        if (clipboardShapeId == null) return;
        Shape original = getShapeById(clipboardShapeId);
        if (original == null) return;

        saveStateToUndo();

        Shape copy = original.clone();
        copy.setId(UUID.randomUUID().toString());
        
        if (x != 0 || y != 0) {
             double dx = x - copy.getX();
             double dy = y - copy.getY();
             moveShape(copy, dx, dy);
        } else {
             moveShape(copy, 20, 20);
        }

        shapes.add(copy);
        selectedShapeId = copy.getId(); 
        redoStack.clear();
    }

    public void resizeSelected(String anchor, double dx, double dy) {
        if (selectedShapeId == null) return;
        Shape s = getShapeById(selectedShapeId);
        if (s == null) return;

        if (s instanceof Rectangle) {
            Rectangle r = (Rectangle) s;
            r.setWidth(Math.max(5, r.getWidth() + dx));
            r.setHeight(Math.max(5, r.getHeight() + dy));
        } else if (s instanceof Square) {
            Square sq = (Square) s;
            sq.setSideLength(Math.max(5, sq.getSideLength() + dx));
        } else if (s instanceof Circle) {
            Circle c = (Circle) s;
            c.setRadius(Math.max(5, c.getRadius() + dx));
        } else if (s instanceof Ellipse) {
            Ellipse e = (Ellipse) s;
            e.setRadiusX(Math.max(5, e.getRadiusX() + dx));
            e.setRadiusY(Math.max(5, e.getRadiusY() + dy));
        } else if (s instanceof LineSegment) {
            LineSegment l = (LineSegment) s;
            l.setX2(l.getX2() + dx);
            l.setY2(l.getY2() + dy);
        } else if (s instanceof Triangle) {
            Triangle t = (Triangle) s;

            t.setX3(t.getX3() + dx);
            t.setY2(t.getY2() + dy);
            t.setY3(t.getY3() + dy);

        }
    }

    public void deleteSelected() {
        if (selectedShapeId == null) return;
        saveStateToUndo();
        shapes.removeIf(s -> s.getId().equals(selectedShapeId));
        selectedShapeId = null;
        redoStack.clear();
    }

    public void updateColor(String color) {
        if (selectedShapeId == null) return;
        Shape s = getShapeById(selectedShapeId);
        if (s != null) {
            saveStateToUndo();
            s.setColor(color);
            redoStack.clear();
        }
    }

    private void moveShape(Shape s, double dx, double dy) {
        if (s instanceof Rectangle) { ((Rectangle)s).setX(((Rectangle)s).getX() + dx); ((Rectangle)s).setY(((Rectangle)s).getY() + dy); }
        else if (s instanceof Circle) { ((Circle)s).setX(((Circle)s).getX() + dx); ((Circle)s).setY(((Circle)s).getY() + dy); }
        else if (s instanceof Square) { ((Square)s).setX(((Square)s).getX() + dx); ((Square)s).setY(((Square)s).getY() + dy); }
        else if (s instanceof Ellipse) { ((Ellipse)s).setX(((Ellipse)s).getX() + dx); ((Ellipse)s).setY(((Ellipse)s).getY() + dy); }
        else if (s instanceof LineSegment) { 
            LineSegment l = (LineSegment) s;
            l.setX(l.getX() + dx); l.setY(l.getY() + dy);
            l.setX2(l.getX2() + dx); l.setY2(l.getY2() + dy);
        } else if (s instanceof Triangle) {
            Triangle t = (Triangle) s;
            t.setX(t.getX() + dx); t.setY(t.getY() + dy);
            t.setX2(t.getX2() + dx); t.setY2(t.getY2() + dy);
            t.setX3(t.getX3() + dx); t.setY3(t.getY3() + dy);
        } else if (s instanceof FreehandShape) {
             for (var p : ((FreehandShape)s).getPoints()) {
                 p.put("x", p.get("x") + dx);
                 p.put("y", p.get("y") + dy);
             }
        }
    }

    private void saveStateToUndo() {
        List<Shape> snapshot = new ArrayList<>();
        for (Shape s : shapes) snapshot.add(s.clone());
        undoStack.push(snapshot);
    }

    private void saveStateToRedo() {
        List<Shape> snapshot = new ArrayList<>();
        for (Shape s : shapes) snapshot.add(s.clone());
        redoStack.push(snapshot);
    }

    private Shape getShapeById(String id) {
        for (Shape s : shapes) if (s.getId().equals(id)) return s;
        return null;
    }

    private boolean hitTest(Shape s, double px, double py) {
        if (s instanceof Rectangle) {
            Rectangle r = (Rectangle) s;
            return px >= r.getX() && px <= r.getX() + r.getWidth() && py >= r.getY() && py <= r.getY() + r.getHeight();
        }
        if (s instanceof Square) {
            Square sq = (Square) s;
            return px >= sq.getX() && px <= sq.getX() + sq.getSideLength() && py >= sq.getY() && py <= sq.getY() + sq.getSideLength();
        }
        if (s instanceof Circle) {
            Circle c = (Circle) s;
            double dx = px - c.getX(); double dy = py - c.getY();
            return dx*dx + dy*dy <= c.getRadius()*c.getRadius();
        }
        if (s instanceof Ellipse) {
            Ellipse e = (Ellipse) s;
            double val = Math.pow(px - e.getX(), 2) / Math.pow(e.getRadiusX(), 2) + Math.pow(py - e.getY(), 2) / Math.pow(e.getRadiusY(), 2);
            return val <= 1.0;
        }

        if (s instanceof Triangle) {
             Triangle t = (Triangle) s;
             double minX = Math.min(t.getX(), Math.min(t.getX2(), t.getX3()));
             double maxX = Math.max(t.getX(), Math.max(t.getX2(), t.getX3()));
             double minY = Math.min(t.getY(), Math.min(t.getY2(), t.getY3()));
             double maxY = Math.max(t.getY(), Math.max(t.getY2(), t.getY3()));
             return px >= minX && px <= maxX && py >= minY && py <= maxY;
        }
        return true; 
    }

    public void clearBoard() {
        saveStateToUndo();
        shapes.clear();
        redoStack.clear();
    }

    public String SavetoJson() throws IOException {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(shapes);
    }

    public String SavetoXml() throws IOException {
        return new XmlMapper().writerWithDefaultPrettyPrinter().writeValueAsString(shapes);
    }

    public void loadFromFile(MultipartFile file) throws IOException {
        String content = new String(file.getBytes());
        List<Shape> loaded;
        if (file.getOriginalFilename().endsWith(".xml")) {
            loaded = new XmlMapper().readValue(content, new TypeReference<List<Shape>>(){});
        } else {
            loaded = new ObjectMapper().readValue(content, new TypeReference<List<Shape>>(){});
        }
        saveStateToUndo();
        shapes = loaded;
        selectedShapeId = null;
    }

    public void updateFillColor(String color) {
        if (selectedShapeId == null) return;
        Shape s = getShapeById(selectedShapeId);
        if (s != null) {
            saveStateToUndo();
            s.setFillColor(color);
            redoStack.clear();
        }
    }
}