package com.lothuspay.auth.service.kyc.records;

public record UploadDocumentResponse(
        String verificationId,
        String r2Key,
        String status
) {}
