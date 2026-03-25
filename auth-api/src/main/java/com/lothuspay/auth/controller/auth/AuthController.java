package com.lothuspay.auth.controller.auth;

import com.lothuspay.auth.dto.request.Dto2FAAuth;
import com.lothuspay.auth.dto.request.DtoAuthLogin;
import com.lothuspay.auth.dto.request.DtoAuthRegister;
import com.lothuspay.auth.dto.request.DtoAuthValidate;
import com.lothuspay.auth.dto.response.DtoResponse;
import com.lothuspay.auth.service.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/login")
    public ResponseEntity<DtoResponse> login(@RequestBody DtoAuthLogin login) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        DtoResponse response = accountService.login(login);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<DtoResponse> register(@RequestBody DtoAuthRegister register) {
        DtoResponse response = accountService.register(register);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<DtoResponse> validateToken(@RequestBody DtoAuthValidate dto) {
        DtoResponse response = accountService.validateToken(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<DtoResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        DtoResponse response = accountService.profile(userDetails);
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/profile/kyc/submit",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DtoResponse> uploadBatch(
            @RequestPart MultipartFile front,
            @RequestPart MultipartFile back,
            @RequestPart MultipartFile selfie,

            @RequestPart String address,
            @RequestPart String city,
            @RequestPart String state,
            @RequestPart String zipCode,

            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                accountService.submitKyc(
                        userDetails, front, back, selfie,
                        address, city, state, zipCode
                )
        );
    }

    @GetMapping("/profile/2fa/setup")
    public ResponseEntity<DtoResponse> setupTwoFactorAuth(@AuthenticationPrincipal UserDetails userDetails) {
        DtoResponse response = accountService.twoFactorAuthSetup(userDetails);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile/2fa/verify")
    public ResponseEntity<DtoResponse> verifyTwoFactorAuth(@RequestBody Dto2FAAuth dto) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        DtoResponse response = accountService.twoFactorAuthVerify(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/2fa/confirm/{code}/{secret}")
    public ResponseEntity<DtoResponse> verifyTwoFactorAuth(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("code") String code, @PathVariable("secret") String secret) {
            DtoResponse response = accountService.twoFactorAuthConfirm(userDetails.getUsername(), code);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/2fa/disable/{backupCode}")
    public ResponseEntity<DtoResponse> disableTwoFactorAuth(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("backupCode") String backupCode) {
        DtoResponse response = accountService.twoFactorAuthDisable(userDetails, backupCode);
        return ResponseEntity.ok(response);
    }
}
