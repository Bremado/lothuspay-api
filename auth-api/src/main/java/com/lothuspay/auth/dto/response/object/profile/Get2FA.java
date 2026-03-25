package com.lothuspay.auth.dto.response.object.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Get2FA {

    private List<String> backupCodes;
    private boolean enabled;

}