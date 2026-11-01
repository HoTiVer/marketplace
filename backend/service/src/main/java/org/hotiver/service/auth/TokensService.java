package org.hotiver.service.auth;

import org.hotiver.common.Utils.RedisKeyUtils;
import org.hotiver.common.Utils.TimeUtils;
import org.hotiver.domain.Entity.User;
import org.hotiver.domain.security.SecurityUser;
import org.hotiver.dto.jwt.JwtTokensDto;
import org.hotiver.service.redis.RedisService;
import org.springframework.stereotype.Service;

@Service
public class TokensService {

    private final Long millisecondsToSaveJwtRefresh;
    private final RedisService redisService;
    private final JwtService jwtService;

    public TokensService(RedisService redisService, JwtService jwtService) {
        millisecondsToSaveJwtRefresh = jwtService.getJwtRefreshExpirationMilliseconds();
        this.redisService = redisService;
        this.jwtService = jwtService;
    }

    public JwtTokensDto generateJwtTokens(User user) {
        SecurityUser securityUser = new SecurityUser(user);
        String refreshToken = jwtService.generateRefreshToken(securityUser);
        String accessToken = jwtService.generateAccessToken(securityUser);
        return new JwtTokensDto(refreshToken, accessToken);
    }

    public void saveJwtRefreshTokenInRedis(JwtTokensDto jwtTokensDto, User user) {
        String key = RedisKeyUtils.generateRedisRefreshTokenKey(user.getId());
        redisService.saveValue(key, jwtTokensDto.getRefreshToken(),
                TimeUtils.toMinutes(millisecondsToSaveJwtRefresh));
    }

}
