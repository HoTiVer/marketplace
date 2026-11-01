package org.hotiver.common.Utils;

public class RedisKeyUtils {

    public static String generateRedisRefreshTokenKey(Long userId) {
        return "refresh:" + HashUtils.hashKeySha256(userId.toString());
    }

    public static String generateRedisTwoFactorKey(String email) {
        return "2fa:" + HashUtils.hashKeySha256(email);
    }

}
