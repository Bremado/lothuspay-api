package com.lothuspay.auth.controller.admin;

import com.lothuspay.auth.dto.request.DtoAuthUserUpdate;
import com.lothuspay.auth.dto.request.admin.PostBlockUser;
import com.lothuspay.auth.dto.request.admin.PostUpdateUserRole;
import com.lothuspay.auth.dto.response.DtoResponse;
import com.lothuspay.auth.service.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/admin")
public class AdminController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/users")
    public ResponseEntity<DtoResponse> users(
            @AuthenticationPrincipal UserDetails details,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "active", required = false) Boolean active) {
        return ResponseEntity.ok(accountService.getUsersWithFilters(details, page, limit, search, role, active));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<DtoResponse> getUserById(@AuthenticationPrincipal UserDetails details, @PathVariable("userId") String userId) {
        return ResponseEntity.ok(accountService.getUserById(userId));
    }

    @GetMapping("/users/stats")
    public ResponseEntity<DtoResponse> getUserStats(@AuthenticationPrincipal UserDetails details) {
        return ResponseEntity.ok(accountService.getUserStats(details));
    }

    @PutMapping("/users/{userId}/block")
    public ResponseEntity<DtoResponse> blockUser(
            @AuthenticationPrincipal UserDetails details,
            @PathVariable("userId") String userId,
            @RequestBody PostBlockUser dto) {
        return ResponseEntity.ok(accountService.blockUser(details, userId, dto));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<DtoResponse> updateUserRole(
            @AuthenticationPrincipal UserDetails details,
            @PathVariable("userId") String userId,
            @RequestBody PostUpdateUserRole dto) {
        return ResponseEntity.ok(accountService.updateUserRole(details, userId, dto));
    }

    @PostMapping("/users/update/{id}")
    public ResponseEntity<DtoResponse> updateUser(@AuthenticationPrincipal UserDetails details, @PathVariable("id") String id, @RequestBody DtoAuthUserUpdate dto) {
        return ResponseEntity.ok(accountService.updateUser(details, id, dto));
    }
}
