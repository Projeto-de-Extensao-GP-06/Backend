package br.com.sgpj.dto.response;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken
) {
}
