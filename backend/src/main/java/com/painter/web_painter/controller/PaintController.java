package com.painter.web_painter.controller;

import org.springframework.web.bind.annotation.*;
import java.com.painter.web_painter.service.PaintService;
import com.painter.web_painter.model.Shape;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200") // Allow Angular
public class PaintController {

    private final PaintService paintService;

    public PaintController(PaintService paintService) {
        this.paintService = paintService;
    }

    @PostMapping("/create")
    // When teammate is ready, change @RequestBody Shape shape -> @RequestBody ShapeDTO dto
    public Shape createShape(@RequestBody Shape shape) {
        // If using DTO, you would do: Shape s = dto.toModel();
        paintService.addShape(shape);
        return shape;
    }

    @GetMapping("/shapes")
    public List<Shape> getShapes() {
        return paintService.getShapes();
    }

    @PostMapping("/undo")
    public List<Shape> undo() {
        paintService.undo();
        return paintService.getShapes();
    }

    @PostMapping("/redo")
    public List<Shape> redo() {
        paintService.redo();
        return paintService.getShapes();
    }

    @PostMapping("/copy/{index}")
    public List<Shape> copy(@PathVariable int index) {
        paintService.copyShape(index);
        return paintService.getShapes();
    }

    @PostMapping("/clear")
    public void clear() {
        paintService.clearBoard();
    }
}
