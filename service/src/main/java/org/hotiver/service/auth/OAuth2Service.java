package org.hotiver.service.auth;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hotiver.common.Enum.RoleType;
import org.hotiver.common.Exception.base.EntityAlreadyExistsException;
import org.hotiver.common.Utils.PasswordUtils;
import org.hotiver.common.Utils.RedisKeyUtils;
import org.hotiver.common.Utils.TimeUtils;
import org.hotiver.domain.Entity.Role;
import org.hotiver.domain.Entity.User;
import org.hotiver.dto.auth.AuthResponse;
import org.hotiver.dto.auth.RegisterRequest;
import org.hotiver.dto.jwt.JwtTokensDto;
import org.hotiver.repo.RoleRepo;
import org.hotiver.repo.UserRepo;
import org.hotiver.service.factory.UserFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class OAuth2Service {

    private final Long millisecondsToSaveJwtRefresh;
    private final Long millisecondsToSaveJwtAccess;
    private final JwtService jwtService;
    private final UserRepo userRepo;
    private final TokensService tokensService;
    private final UserFactory userFactory;
    private final RoleRepo roleRepo;

    public OAuth2Service(JwtService jwtService, UserRepo userRepo,
                         TokensService tokensService, UserFactory userFactory,
                         RoleRepo roleRepo) {
        millisecondsToSaveJwtRefresh = jwtService.getJwtRefreshExpirationMilliseconds();
        millisecondsToSaveJwtAccess =  jwtService.getJwtAccessExpirationMilliseconds();
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.tokensService = tokensService;
        this.userFactory = userFactory;
        this.roleRepo = roleRepo;
    }


    @Transactional
    public AuthResponse authAsOAuth2User(String email) {
        User user = userRepo.findByEmail(email).orElse(null);

        if (user == null){
            JwtTokensDto jwtTokensDto = registerOAuth2(email);
            log.info("OAuth user created: {}", email);
            return buildAuthResponse(jwtTokensDto);
        }

        JwtTokensDto jwtTokensDto = tokensService.generateJwtTokens(user);
        tokensService.saveJwtRefreshTokenInRedis(jwtTokensDto, user);

        log.info("OAuth login: {}", email);
        return buildAuthResponse(jwtTokensDto);
    }

    private JwtTokensDto registerOAuth2(String email) {
        if (userRepo.findByEmail(email).isPresent()) {
            throw new EntityAlreadyExistsException("User already exists");
        }

        Role userRole = roleRepo.findByName(RoleType.USER)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        String displayName = email.substring(0, email.indexOf('@'));

        User user = userFactory.createNewDefaultUser(
                email,
                null,
                displayName,
                userRole
        );

        userRepo.save(user);

        JwtTokensDto jwtTokensDto = tokensService.generateJwtTokens(user);
        tokensService.saveJwtRefreshTokenInRedis(jwtTokensDto, user);
        return jwtTokensDto;
    }

    private AuthResponse buildAuthResponse(JwtTokensDto jwtTokensDto) {
        return new AuthResponse(
                jwtTokensDto.getAccessToken(),
                jwtTokensDto.getRefreshToken(),
                TimeUtils.toSeconds(millisecondsToSaveJwtAccess),
                TimeUtils.toSeconds(millisecondsToSaveJwtRefresh)
        );
    }
}
