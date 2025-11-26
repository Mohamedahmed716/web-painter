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
import java.util.UUID;
import com.painter.web_painter.model.*;

@Service
public class PaintService {
    // The LIVE state
    private List<Shape> shapes = new ArrayList<>();
    
    // History Stacks
    private Stack<List<Shape>> undoStack = new Stack<>();
    private Stack<List<Shape>> redoStack = new Stack<>();

    // Selection State
    private String selectedShapeId = null;

    public List<Shape> getShapes() {
        // Mark selected shape for frontend
        for (Shape s : shapes) {
            // You might need a 'selected' boolean in your Shape model or handle it purely in frontend.
            // For now, we assume frontend handles selection visual based on ID.
        }
        return shapes;
    }

    // --- CORE ACTIONS ---

    public void addShape(Shape shape) {
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

    // --- SELECTION & MANIPULATION ---

    public void selectShapeAt(double x, double y) {
        Shape found = null;
        // Iterate backwards to select top-most shape
        for (int i = shapes.size() - 1; i >= 0; i--) {
            if (hitTest(shapes.get(i), x, y)) {
                found = shapes.get(i);
                break;
            }
        }
        selectedShapeId = (found != null) ? found.getId() : null;
        
        // Mark shapes as selected (optional if your Shape class has this field)
        // for (Shape s : shapes) s.setSelected(s.getId().equals(selectedShapeId));
    }

    public void moveSelected(double dx, double dy) {
        if (selectedShapeId == null) return;
        
        // Find the shape
        Shape s = getShapeById(selectedShapeId);
        if (s == null) return;

        // We do NOT save to undo stack on every pixel move (too many states).
        // The frontend handles "startMove" and "endMove" for that.
        
        // Apply move logic
        if (s instanceof Rectangle) {
            Rectangle r = (Rectangle) s; r.setX(r.getX() + dx); r.setY(r.getY() + dy);
        } else if (s instanceof Circle) {
            Circle c = (Circle) s; c.setX(c.getX() + dx); c.setY(c.getY() + dy);
        } else if (s instanceof Square) {
            Square sq = (Square) s; sq.setX(sq.getX() + dx); sq.setY(sq.getY() + dy);
        } else if (s instanceof Ellipse) {
            Ellipse e = (Ellipse) s; e.setX(e.getX() + dx); e.setY(e.getY() + dy);
        } else if (s instanceof LineSegment) {
            LineSegment l = (LineSegment) s;
            l.setX(l.getX() + dx); l.setY(l.getY() + dy);
            l.setX2(l.getX2() + dx); l.setY2(l.getY2() + dy);
        } else if (s instanceof Triangle) {
            Triangle t = (Triangle) s;
            t.setX(t.getX() + dx); t.setY(t.getY() + dy);
            t.setX2(t.getX2() + dx); t.setY2(t.getY2() + dy);
            t.setX3(t.getX3() + dx); t.setY3(t.getY3() + dy);
        } else if (s instanceof FreehandShape) {
            FreehandShape f = (FreehandShape) s;
            if (f.getPoints() != null) {
                for (Map<String, Double> p : f.getPoints()) {
                    p.put("x", p.get("x") + dx);
                    p.put("y", p.get("y") + dy);
                }
            }
        }
    }

    public void resizeSelected(String anchor, double dx, double dy) {
        if (selectedShapeId == null) return;
        Shape s = getShapeById(selectedShapeId);
        if (s == null) return;

        // Logic matches yours but ensures robust null checks
        if (s instanceof Rectangle) {
            Rectangle r = (Rectangle) s;
            r.setWidth(Math.max(5, r.getWidth() + dx)); // Min size check
            r.setHeight(Math.max(5, r.getHeight() + dy));
        } else if (s instanceof Circle) {
            Circle c = (Circle) s;
            c.setRadius(Math.max(5, c.getRadius() + dx));
        } else if (s instanceof Square) {
            Square sq = (Square) s;
            sq.setSideLength(Math.max(5, sq.getSideLength() + dx));
        }
        // Add other shapes as needed
    }

    public void copySelected() {
        if (selectedShapeId == null) return;
        Shape original = getShapeById(selectedShapeId);
        if (original == null) return;

        saveStateToUndo();

        Shape copy = original.clone();
        copy.setId(UUID.randomUUID().toString()); // CRITICAL: New ID
        
        // Offset so user sees it
        if (copy instanceof FreehandShape) {
             // Offset points for freehand
             FreehandShape f = (FreehandShape) copy;
             for(Map<String, Double> p : f.getPoints()) {
                 p.put("x", p.get("x") + 20);
                 p.put("y", p.get("y") + 20);
             }
        } else {
            // Offset standard shapes
            copy.setX(copy.getX() + 20);
            copy.setY(copy.getY() + 20);
            if(copy instanceof LineSegment) {
                LineSegment l = (LineSegment) copy;
                l.setX2(l.getX2() + 20);
                l.setY2(l.getY2() + 20);
            } else if (copy instanceof Triangle) {
                Triangle t = (Triangle) copy;
                t.setX2(t.getX2() + 20); t.setY2(t.getY2() + 20);
                t.setX3(t.getX3() + 20); t.setY3(t.getY3() + 20);
            }
        }

        shapes.add(copy);
        selectedShapeId = copy.getId(); // Select the copy
        redoStack.clear();
    }

    public void deleteSelected() {
        if (selectedShapeId == null) return;
        saveStateToUndo();
        shapes.removeIf(s -> s.getId().equals(selectedShapeId));
        selectedShapeId = null;
        redoStack.clear();
    }

    // --- UNDO SNAPSHOT HELPERS ---

    public void startMove() {
        // Call this BEFORE dragging starts
        saveStateToUndo(); 
    }

    public void endMove() {
        // Optional: If you want to consolidate moves. 
        // Currently startMove handles the snapshot.
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

    // --- UTILS ---

    private Shape getShapeById(String id) {
        for (Shape s : shapes) if (s.getId().equals(id)) return s;
        return null;
    }

    private boolean hitTest(Shape s, double px, double py) {
        // Your existing hitTest logic goes here (it was correct).
        // Ensure Freehand hit test checks points distance properly.
        return true; // Placeholder - verify your existing hitTest is pasted here
    }

    // --- FILES ---

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
        List<Shape> loaded;
        if (file.getOriginalFilename().endsWith(".xml")) {
            loaded = new XmlMapper().readValue(content, new TypeReference<List<Shape>>(){});
        } else {
            loaded = new ObjectMapper().readValue(content, new TypeReference<List<Shape>>(){});
        }
        
        saveStateToUndo(); // Save before overwriting
        shapes = loaded;
        selectedShapeId = null;
    }
}