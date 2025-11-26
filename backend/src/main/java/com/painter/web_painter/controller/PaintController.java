package com.painter.web_painter.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.painter.web_painter.Service.PaintService;
import com.painter.web_painter.Service.ShapeFactory;
import com.painter.web_painter.model.Shape;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class PaintController {

    private final PaintService paintService;
    private final ShapeFactory factory;

    @Autowired
    public PaintController(ShapeFactory factory, PaintService paintService) {
        this.factory = factory;
        this.paintService = paintService;
    }

    @GetMapping("/shapes")
    public ResponseEntity<List<Shape>> getAll() {
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/create")
    public ResponseEntity<List<Shape>> create(@RequestBody Map<String, Object> payload) {
        try {
            String type = (String) payload.get("type");
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) payload.get("params");
            if (params == null)
                params = Map.of();
            Shape s = factory.createShape(type, params);
            if (s != null)
                paintService.addShape(s);
            return ResponseEntity.ok(paintService.getShapes());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/undo")
    public ResponseEntity<List<Shape>> undo() {
        paintService.undo();
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/redo")
    public ResponseEntity<List<Shape>> redo() {
        paintService.redo();
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/select")
    public ResponseEntity<List<Shape>> select(@RequestBody Map<String, Object> p) {
        paintService.selectShapeAt(((Number) p.get("x")).doubleValue(), ((Number) p.get("y")).doubleValue());
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/move/start")
    public ResponseEntity<Void> startMove() {
        paintService.startMove();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/move")
    public ResponseEntity<List<Shape>> move(@RequestBody Map<String, Object> p) {
        paintService.moveSelected(((Number) p.get("dx")).doubleValue(), ((Number) p.get("dy")).doubleValue());
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/move/end")
    public ResponseEntity<List<Shape>> endMove() {
        paintService.endMove();
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/copy")
    public ResponseEntity<List<Shape>> copy() {
        paintService.copySelected();
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/paste")
    public ResponseEntity<List<Shape>> paste(@RequestBody Map<String, Object> p) {
        paintService.pasteSelected(((Number) p.get("x")).doubleValue(), ((Number) p.get("y")).doubleValue());
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/delete")
    public ResponseEntity<List<Shape>> delete() {
        paintService.deleteSelected();
        return ResponseEntity.ok(paintService.getShapes());
    }

    // NEW: CLEAR ENDPOINT
    @PostMapping("/clear")
    public ResponseEntity<List<Shape>> clear() {
        paintService.clearBoard();
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/resize")
    public ResponseEntity<List<Shape>> resize(@RequestBody Map<String, Object> p) {
        paintService.resizeSelected((String) p.get("anchor"), ((Number) p.get("dx")).doubleValue(),
                ((Number) p.get("dy")).doubleValue());
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/color")
    public ResponseEntity<List<Shape>> color(@RequestBody Map<String, String> p) {
        paintService.updateColor(p.get("color"));
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/fill")
    public ResponseEntity<List<Shape>> fill(@RequestBody Map<String, String> p) {
        paintService.updateFillColor(p.get("fillColor"));
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/width")
    public ResponseEntity<List<Shape>> width(@RequestBody Map<String, Object> p) {
        paintService.updateStrokeWidth(((Number) p.get("width")).doubleValue());
        return ResponseEntity.ok(paintService.getShapes());
    }

    @GetMapping("/save/json")
    public ResponseEntity<byte[]> saveJson() {
        try {
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"drawing.json\"")
                    .body(paintService.SavetoJson().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/save/xml")
    public ResponseEntity<byte[]> saveXml() {
        try {
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"drawing.xml\"")
                    .contentType(MediaType.APPLICATION_XML)
                    .body(paintService.SavetoXml().getBytes(StandardCharsets.ISO_8859_1));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/load")
    public ResponseEntity<String> load(@RequestParam("file") MultipartFile file) {
        try {
            paintService.loadFromFile(file);
            return ResponseEntity.ok("Loaded");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}