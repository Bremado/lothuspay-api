package com.lothuspay.auth.dto.response.object.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetUserStats {

    private Long totalUsers;
    private Long activeUsers;
    private Long blockedUsers;
    private Long newUsersToday;
    private Map<String, Long> usersByRole;

}

