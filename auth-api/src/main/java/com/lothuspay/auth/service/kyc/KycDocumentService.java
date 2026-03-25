package com.lothuspay.auth.service.kyc;

import com.lothuspay.auth.service.kyc.records.UploadDocumentResponse;
import com.lothuspay.auth.service.r2.R2StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class KycDocumentService {

    private final R2StorageService storageService;

    public KycDocumentService(R2StorageService storageService) {
        this.storageService = storageService;
    }

    public UploadDocumentResponse uploadDocument(
            String userId,
            String verificationId,
            MultipartFile file,
            String label
    ) {

        String key = buildKey(userId, verificationId, label + "_" + file.getOriginalFilename());

        storageService.upload(file, key);

        return new UploadDocumentResponse(
                verificationId,
                key,
                "UPLOADED"
        );
    }

    public boolean validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return false;
        }

        List<String> allowedTypes = List.of(
                "image/jpeg",
                "image/png"
        );

        if (!allowedTypes.contains(file.getContentType())) {
            return false;
        }
        return true;
    }
    public String generatePresignedUrl(String key) {
        return storageService.generatePresignedUrl(key);
    }

    private String generateVerificationId() {
        return "kyc_" + UUID.randomUUID();
    }

    private String buildKey(String userId, String verificationId, String filename) {
        return String.format(
                "kyc/%s/%s/%s",
                userId,
                verificationId,
                filename
        );
    }
}
