package com.lothuspay.auth.controller.admin;

import com.lothuspay.auth.dto.request.admin.PostRejectDocument;
import com.lothuspay.auth.dto.response.DtoResponse;
import com.lothuspay.auth.service.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/admin/documents")
public class DocumentAdminController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/pending")
    public ResponseEntity<DtoResponse> getPendingDocuments(
            @AuthenticationPrincipal UserDetails details,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        return ResponseEntity.ok(accountService.getPendingDocuments(details, page, limit));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<DtoResponse> getUserDocument(
            @AuthenticationPrincipal UserDetails details,
            @PathVariable("userId") String userId) {
        return ResponseEntity.ok(accountService.getUserDocument(details, userId));
    }

    @PostMapping("/{userId}/accept")
    public ResponseEntity<DtoResponse> acceptDocument(
            @AuthenticationPrincipal UserDetails details,
            @PathVariable("userId") String userId) {
        return ResponseEntity.ok(accountService.acceptDocument(details, userId));
    }


    @PostMapping("/{userId}/reject")
    public ResponseEntity<DtoResponse> rejectDocument(
            @AuthenticationPrincipal UserDetails details,
            @PathVariable("userId") String userId,
            @RequestBody PostRejectDocument dto) {
        return ResponseEntity.ok(accountService.rejectDocument(details, userId, dto));
    }

    @GetMapping("/stats")
    public ResponseEntity<DtoResponse> getDocumentStats(@AuthenticationPrincipal UserDetails details) {
        return ResponseEntity.ok(accountService.getDocumentStats(details));
    }

}

