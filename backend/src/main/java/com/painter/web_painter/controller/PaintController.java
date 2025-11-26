package com.painter.web_painter.controller;

import java.nio.charset.Charset;
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
    public ResponseEntity<List<Shape>> getAll() { 
        return ResponseEntity.ok(paintService.getShapes()); 
    }

    @PostMapping("/create")
    public ResponseEntity<List<Shape>> create(@RequestBody Map<String, Object> payload) {
        try {
            String type = (String) payload.get("type");
            // The params map comes directly from the request body. 
            // Jackson handles the initial deserialization to Map<String, Object>.
            // We cast it to Map<String, Object> to pass to the factory.
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) payload.get("params");
            
            // If params is null, create an empty map to avoid NullPointerException in factory
            if (params == null) {
                 params = Map.of();
            }

            Shape s = factory.createShape(type, params);
            if (s != null) {
                paintService.addShape(s);
            }
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

    // --- MANIPULATION ---
    @PostMapping("/select")
    public ResponseEntity<List<Shape>> select(@RequestBody Map<String, Object> p) {
        // Safely cast numbers to avoid Integer vs Double issues
        double x = ((Number) p.get("x")).doubleValue();
        double y = ((Number) p.get("y")).doubleValue();
        paintService.selectShapeAt(x, y);
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/move/start")
    public ResponseEntity<Void> startMove() { 
        paintService.startMove(); 
        return ResponseEntity.ok().build(); 
    }

    @PostMapping("/move")
    public ResponseEntity<List<Shape>> move(@RequestBody Map<String, Object> p) {
        double dx = ((Number) p.get("dx")).doubleValue();
        double dy = ((Number) p.get("dy")).doubleValue();
        paintService.moveSelected(dx, dy);
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
        double x = ((Number) p.get("x")).doubleValue();
        double y = ((Number) p.get("y")).doubleValue();
        paintService.pasteSelected(x, y); 
        return ResponseEntity.ok(paintService.getShapes()); 
    }

    @PostMapping("/delete")
    public ResponseEntity<List<Shape>> delete() { 
        paintService.deleteSelected(); 
        return ResponseEntity.ok(paintService.getShapes()); 
    }
    
    @PostMapping("/clear")
    public ResponseEntity<List<Shape>> clear() { 
        paintService.clearBoard(); 
        return ResponseEntity.ok(paintService.getShapes()); 
    }

    @PostMapping("/resize")
    public ResponseEntity<List<Shape>> resize(@RequestBody Map<String, Object> p) {
        String anchor = (String) p.get("anchor");
        double dx = ((Number) p.get("dx")).doubleValue();
        double dy = ((Number) p.get("dy")).doubleValue();
        
        paintService.resizeSelected(anchor, dx, dy);
        return ResponseEntity.ok(paintService.getShapes());
    }

    // STROKE COLOR
    @PostMapping("/color")
    public ResponseEntity<List<Shape>> color(@RequestBody Map<String, String> p) {
        paintService.updateColor(p.get("color"));
        return ResponseEntity.ok(paintService.getShapes());
    }

    @PostMapping("/fill")
    public ResponseEntity<List<Shape>> fill(@RequestBody Map<String, String> p) {
        // Matches frontend { fillColor: ... } payload
        paintService.updateFillColor(p.get("fillColor"));
        return ResponseEntity.ok(paintService.getShapes());
    }

    // --- FILES ---
    
    // FIX: Changed path to match Frontend API ('/files/save/json' -> '/save/json')
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

    @PostMapping("/load") 
    public ResponseEntity<String> load(@RequestParam("file") MultipartFile file) {
        try { 
            paintService.loadFromFile(file); 
            return ResponseEntity.ok("Loaded"); 
        } catch (Exception e) { 
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage()); 
        }
    }
}