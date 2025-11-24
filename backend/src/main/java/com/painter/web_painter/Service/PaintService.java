package java.com.painter.web_painter.service;

import org.springframework.stereotype.Service;
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
}
