package com.painter.web_painter.controller;
import com.painter.web_painter.Service.PaintService;
import com.painter.web_painter.Service.ShapeFactory;
import com.painter.web_painter.model.Shape;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class PaintController {
    @Autowired
    private PaintService paintService;
    private ShapeFactory factory;
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
        paintService.addShape(s);
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


    @GetMapping("/save/json")
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

    @GetMapping("/save/xml")
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
    @PostMapping("/load")
    public ResponseEntity<String> loadFile(@RequestParam("file") MultipartFile file) {
        try {
            paintService.loadFromFile(file);
            return ResponseEntity.ok("File loaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error loading file: " + e.getMessage());
        }
    }





}
