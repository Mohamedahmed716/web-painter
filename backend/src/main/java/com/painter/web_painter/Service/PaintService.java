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

import com.painter.web_painter.model.Shape;

@Service
public class PaintService {
    private List<Shape> shapes = new ArrayList<>();

    Stack<List<Shape>> undoStack = new Stack<>();
    Stack<List<Shape>> redoStack = new Stack<>();

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

    public void copyShape(int index) {
        if (index < 0 || index >= shapes.size()) return;

        deepCopy(undoStack);

        Shape original = shapes.get(index);
        Shape copy = original.clone();

        copy.setX(original.getX() + 20);
        copy.setY(original.getY() + 20);

        shapes.add(copy);
        redoStack.clear();
    }

    public void clearBoard() {
        deepCopy(undoStack);
        shapes.clear();
        redoStack.clear();
    }
    public String SavetoJson() throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(shapes);
    }
    public String SavetoXml() throws IOException{
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
        this.shapes.addAll(loadedShapes);
    }
}
