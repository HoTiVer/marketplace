package org.hotiver.dto.common;

public record RedirectResponse(
        String redirectUrl,
        String method
) { }
