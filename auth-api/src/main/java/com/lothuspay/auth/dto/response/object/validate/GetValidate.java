package com.lothuspay.auth.dto.response.object.validate;

import com.lothuspay.auth.model.accounts.segment.AccountSegment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetValidate {

    private boolean valid;
    private String userId;
    private String email;
    private List<String> roles;

    private boolean verified;
    private boolean emailVerified;

    private AccountSegment segment;
}
