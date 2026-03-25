package com.lothuspay.auth.dto.request;

import lombok.*;

import java.util.List;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DtoAuthApiKeyAllowlist {

    private List<String> allowlist;

}
