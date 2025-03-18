package org.example.expert.domain.image;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Uploader s3Uploader;

    public ImageResponse saveFile(MultipartFile imgFile) throws IOException {
        if (!imgFile.isEmpty()) {
            String storedFileName = s3Uploader.upload(imgFile, "images"); // s3 버킷에 images 디렉토리에 업로드
            return new ImageResponse(storedFileName);
        } else {
            throw new IOException("이미지가 없습니다.");
        }
    }

}
