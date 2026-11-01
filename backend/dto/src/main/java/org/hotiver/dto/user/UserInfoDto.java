package org.hotiver.dto.user;

import java.util.List;

public record UserInfoDto(
        String displayName,
        List<String> roles
) {}