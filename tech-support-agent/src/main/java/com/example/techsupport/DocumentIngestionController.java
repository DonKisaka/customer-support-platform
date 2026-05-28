package com.example.techsupport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/docs")
@RequiredArgsConstructor
public class DocumentIngestionController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        documentService.ingestDocument(file.getResource());
        return ResponseEntity.ok("Document ingested successfully: " + file.getOriginalFilename());
    }
}
