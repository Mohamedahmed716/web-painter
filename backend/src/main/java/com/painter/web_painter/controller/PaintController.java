package com.painter.web_painter.controller;
import com.painter.web_painter.Service.PaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
@CrossOrigin
public class PaintController {
    @Autowired
    private PaintService paintService;

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
