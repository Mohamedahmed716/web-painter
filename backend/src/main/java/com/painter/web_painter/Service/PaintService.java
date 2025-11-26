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
import com.painter.web_painter.model.*;

@Service
public class PaintService {
    private List<Shape> shapes = new ArrayList<>();
    private Stack<List<Shape>> undoStack = new Stack<>();
    private Stack<List<Shape>> redoStack = new Stack<>();
    private String selectedShapeId = null;
    private List<Shape> moveSnapshot = null;

    public List<Shape> getShapes() {
        // Mark selected shape logic if needed
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

    // --- MANIPULATION ---
    public void selectShapeAt(double x, double y) {
        Shape found = null;
        for (int i = shapes.size() - 1; i >= 0; i--) {
            if (hitTest(shapes.get(i), x, y)) { found = shapes.get(i); break; }
        }
        selectedShapeId = (found != null) ? found.getId() : null;
        // Set 'selected' flag on shapes for frontend highlighting
        for(Shape s : shapes) {
             // Assuming you have a setSelected method or field
             // s.setSelected(s.getId().equals(selectedShapeId));
        }
    }

    public void startMove() {
        moveSnapshot = new ArrayList<>();
        for (Shape s : shapes) moveSnapshot.add(s.clone());
    }

    public void moveSelected(double dx, double dy) {
        if (selectedShapeId == null) return;
        Shape s = getShapeById(selectedShapeId);
        if (s == null) return;
        
        // Update coordinates (add your shape specific setters here)
        if (s instanceof Rectangle) { ((Rectangle)s).setX(((Rectangle)s).getX() + dx); ((Rectangle)s).setY(((Rectangle)s).getY() + dy); }
        else if (s instanceof Circle) { ((Circle)s).setX(((Circle)s).getX() + dx); ((Circle)s).setY(((Circle)s).getY() + dy); }
        else if (s instanceof FreehandShape) {
             for (var p : ((FreehandShape)s).getPoints()) {
                 p.put("x", p.get("x") + dx);
                 p.put("y", p.get("y") + dy);
             }
        }
        // ... add other shapes ...
    }

    public void endMove() {
        if (moveSnapshot != null) {
            undoStack.push(moveSnapshot);
            redoStack.clear();
            moveSnapshot = null;
        }
    }

    public void copySelected() {
        if (selectedShapeId == null) return;
        Shape s = getShapeById(selectedShapeId);
        if (s == null) return;
        saveStateToUndo();
        Shape copy = s.clone();
        copy.setId(UUID.randomUUID().toString());
        // Offset logic
        if(copy instanceof Rectangle) { ((Rectangle)copy).setX(((Rectangle)copy).getX()+20); ((Rectangle)copy).setY(((Rectangle)copy).getY()+20); }
        shapes.add(copy);
        selectedShapeId = copy.getId();
        redoStack.clear();
    }

    public void deleteSelected() {
        if (selectedShapeId == null) return;
        saveStateToUndo();
        shapes.removeIf(s -> s.getId().equals(selectedShapeId));
        selectedShapeId = null;
        redoStack.clear();
    }

    public void resizeSelected(String anchor, double dx, double dy) {
        if (selectedShapeId == null) return;
        Shape s = getShapeById(selectedShapeId);
        if (s == null) return;
        // Add resize logic (e.g. s.setWidth(s.getWidth() + dx))
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

    private Shape getShapeById(String id) {
        for (Shape s : shapes) if (s.getId().equals(id)) return s;
        return null;
    }

    private boolean hitTest(Shape s, double x, double y) {
        // Paste your hitTest logic here
        return true; 
    }

    // --- FILES ---
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
}