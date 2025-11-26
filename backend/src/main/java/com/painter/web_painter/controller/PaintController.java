package com.painter.web_painter.controller;
import java.nio.charset.Charset;
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
    public ResponseEntity<List<Shape>> getAllShapes() {
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/create")
    public ResponseEntity<List<Shape>> createShape(@RequestBody Map<String, Object> payload) {
        String type = (String) payload.get("type");
        Map<String, Object> params = (Map<String, Object>) payload.get("params");
        Shape s = factory.createShape(type, params);
        if (s != null) {
            paintService.addShape(s);
        }
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/undo")
    public ResponseEntity<List<Shape>> undoShape() {
        paintService.undo();
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/redo")
    public ResponseEntity<List<Shape>> redoShape() {
        paintService.redo();
        return ResponseEntity.ok(paintService.getShapes());
    }


    @GetMapping("files/save/json")
    public ResponseEntity<byte[]> saveJson() {
        try {
            String json = paintService.SavetoJson();
            byte[] content = json.getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"drawing.json\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/files/save/xml")
    public ResponseEntity<byte[]> saveXml() {
        try {
            String xml = paintService.SavetoXml();
            byte[] content = xml.getBytes(Charset.forName("ISO-8859-1"));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"drawing.xml\"")
                    .contentType(MediaType.APPLICATION_XML)
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Load File
    @PostMapping("/files/load")
    public ResponseEntity<String> loadFile(@RequestParam("file") MultipartFile file) {
        try {
            paintService.loadFromFile(file);
            return ResponseEntity.ok("File loaded successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error loading file: " + e.getMessage());
        }
    }
    // PaintController.java

    @PostMapping("/select")
    public ResponseEntity<List<Shape>> selectShape(@RequestBody Map<String, Object> payload) {
        double x = ((Number) payload.get("x")).doubleValue();
        double y = ((Number) payload.get("y")).doubleValue();
        paintService.selectShapeAt(x, y);
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/move")
    public ResponseEntity<List<Shape>> moveSelected(@RequestBody Map<String, Object> payload) {
        double dx = ((Number) payload.get("dx")).doubleValue();
        double dy = ((Number) payload.get("dy")).doubleValue();
        paintService.moveSelected(dx, dy);
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/resize")
    public ResponseEntity<List<Shape>> resizeSelected(@RequestBody Map<String, Object> payload) {
        String anchor = (String) payload.get("anchor");
        double dx = ((Number) payload.get("dx")).doubleValue();
        double dy = ((Number) payload.get("dy")).doubleValue();
        paintService.resizeSelected(anchor, dx, dy);
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/copy")
    public ResponseEntity<List<Shape>> copySelected() {
        paintService.copySelected();
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/delete")
    public ResponseEntity<List<Shape>> deleteSelected() {
        paintService.deleteSelected();
        return ResponseEntity.ok(paintService.getShapes());
    }

}