package com.painter.web_painter.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    // --- BASIC ---
    @GetMapping("/shapes")
    public ResponseEntity<List<Shape>> getAll() { return ResponseEntity.ok(paintService.getShapes()); }

    @PostMapping("/create")
    public ResponseEntity<List<Shape>> create(@RequestBody Map<String, Object> payload) {
        Shape s = factory.createShape((String)payload.get("type"), (Map)payload.get("params"));
        if (s != null) paintService.addShape(s);
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/undo")
    public ResponseEntity<List<Shape>> undo() { paintService.undo(); return ResponseEntity.ok(paintService.getShapes()); }

    @PostMapping("/redo")
    public ResponseEntity<List<Shape>> redo() { paintService.redo(); return ResponseEntity.ok(paintService.getShapes()); }

    // --- MANIPULATION ---
    @PostMapping("/select")
    public ResponseEntity<List<Shape>> select(@RequestBody Map<String, Double> payload) {
        paintService.selectShapeAt(payload.get("x"), payload.get("y"));
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/move/start")
    public ResponseEntity<Void> startMove() {
        paintService.startMove(); // CRITICAL: Snapshots state before drag starts
        return ResponseEntity.ok().build();
    }

    @PostMapping("/move")
    public ResponseEntity<List<Shape>> move(@RequestBody Map<String, Double> payload) {
        paintService.moveSelected(payload.get("dx"), payload.get("dy"));
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/move/end")
    public ResponseEntity<List<Shape>> endMove() {
        paintService.endMove(); 
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/resize")
    public ResponseEntity<List<Shape>> resize(@RequestBody Map<String, Object> payload) {
        paintService.resizeSelected((String)payload.get("anchor"), 
            ((Number)payload.get("dx")).doubleValue(), 
            ((Number)payload.get("dy")).doubleValue());
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/copy")
    public ResponseEntity<List<Shape>> copy() {
        paintService.copySelected();
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/delete")
    public ResponseEntity<List<Shape>> delete() {
        paintService.deleteSelected();
        return ResponseEntity.ok(paintService.getShapes());
    }

    // --- FILES ---
    @GetMapping("/save/json")
    public ResponseEntity<byte[]> saveJson() {
        try {
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"drawing.json\"")
                .body(paintService.SavetoJson().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) { return ResponseEntity.internalServerError().build(); }
    }

    @GetMapping("/save/xml")
    public ResponseEntity<byte[]> saveXml() {
        try {
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"drawing.xml\"")
                .contentType(MediaType.APPLICATION_XML)
                .body(paintService.SavetoXml().getBytes(StandardCharsets.ISO_8859_1));
        } catch (Exception e) { return ResponseEntity.internalServerError().build(); }
    }

    // FIX: Ensure path matches Frontend ('/load' vs '/files/load')
    @PostMapping("/load") 
    public ResponseEntity<String> load(@RequestParam("file") MultipartFile file) {
        try {
            paintService.loadFromFile(file);
            return ResponseEntity.ok("Loaded");
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }
}