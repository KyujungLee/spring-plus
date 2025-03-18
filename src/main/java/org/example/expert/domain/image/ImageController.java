package org.example.expert.domain.image;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<ImageResponse> uploadFile(MultipartFile imgFile) {
        try {
            return ResponseEntity.ok(imageService.saveFile(imgFile));
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }

}
