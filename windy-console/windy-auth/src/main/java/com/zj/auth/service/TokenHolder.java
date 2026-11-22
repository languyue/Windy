package com.zj.auth.service;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.zj.auth.entity.Constants;
import com.zj.auth.entity.LoginResult;
import com.zj.auth.entity.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class TokenHolder {

    /**
     * hs256签名要求的密钥长度，对应256bit
     */
    private static final int SECRET_BYTE_LENGTH = 32;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String SECRET_FILE_NAME = "token.secret";

    @Value("${windy.token.expire}")
    private Integer tokenExpire;

    /**
     * 优先通过环境变量或者启动参数-DTOKEN_SECRET配置
     */
    @Value("${TOKEN_SECRET:}")
    private String envSecret;

    /**
     * 历史配置方式，仅做兼容使用，不建议再把密钥明文保存在配置文件中
     */
    @Value("${windy.token.secret:}")
    private String legacySecret;

    /**
     * 未配置密钥时的持久化文件，默认在用户目录的.windy下
     */
    @Value("${TOKEN_SECRET_FILE:}")
    private String secretFilePath;

    private String tokenSecret;

    private final ISessionCache sessionCache;

    public TokenHolder(ISessionCache sessionCache) {
        this.sessionCache = sessionCache;
    }

    @PostConstruct
    public void initTokenSecret() {
        tokenSecret = resolveSecret();
        if (tokenSecret.length() < SECRET_BYTE_LENGTH) {
            log.warn("token secret is too short, hs256 recommend at least {} characters", SECRET_BYTE_LENGTH);
        }
    }

    private String resolveSecret() {
        if (StringUtils.isNotBlank(envSecret)) {
            log.info("token secret is loaded from environment variable TOKEN_SECRET");
            return envSecret.trim();
        }
        if (StringUtils.isNotBlank(legacySecret)) {
            log.warn("token secret is loaded from deprecated config windy.token.secret, "
                + "please use environment variable TOKEN_SECRET instead");
            return legacySecret.trim();
        }
        return loadOrCreateFileSecret();
    }

    private String loadOrCreateFileSecret() {
        Path secretPath = getSecretPath();
        if (Files.exists(secretPath)) {
            try {
                String existSecret = new String(Files.readAllBytes(secretPath), StandardCharsets.UTF_8).trim();
                if (StringUtils.isNotBlank(existSecret)) {
                    log.info("token secret is loaded from file {}", secretPath);
                    return existSecret;
                }
                log.warn("token secret file is empty, regenerate one, path={}", secretPath);
            } catch (IOException e) {
                log.error("read token secret file error, regenerate one, path={}", secretPath, e);
            }
        } else {
            log.info("token secret is not configured, generate a random one, path={}", secretPath);
        }
        return generateAndSaveSecret(secretPath);
    }

    private String generateAndSaveSecret(Path secretPath) {
        String secret = generateSecret();
        try {
            Files.createDirectories(secretPath.getParent());
            writeSecretFile(secretPath, secret);
            log.info("token secret is generated and saved to {}, keep it stable to avoid invalidating all tokens",
                secretPath);
        } catch (Exception e) {
            //写文件失败时不阻塞启动，仅使用内存密钥，重启后所有token失效
            log.error("save token secret file error, only the in-memory secret is used, path={}", secretPath, e);
            log.error("all tokens will be invalid after restart, please configure TOKEN_SECRET manually");
        }
        return secret;
    }

    private void writeSecretFile(Path secretPath, String secret) throws IOException {
        Files.write(secretPath, secret.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        try {
            Files.setPosixFilePermissions(secretPath, PosixFilePermissions.fromString("rw-------"));
        } catch (Exception e) {
            log.info("can not set token secret file permission, maybe not a posix file system, path={}", secretPath);
        }
    }

    private Path getSecretPath() {
        if (StringUtils.isNotBlank(secretFilePath)) {
            return Paths.get(secretFilePath.trim());
        }
        return Paths.get(System.getProperty("user.home"), ".windy", SECRET_FILE_NAME);
    }

    private static String generateSecret() {
        byte[] secretBytes = new byte[SECRET_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(secretBytes);
        StringBuilder secretBuilder = new StringBuilder(secretBytes.length * 2);
        for (byte secretByte : secretBytes) {
            secretBuilder.append(Character.forDigit((secretByte >> 4) & 0xF, 16));
            secretBuilder.append(Character.forDigit(secretByte & 0xF, 16));
        }
        return secretBuilder.toString();
    }

    public LoginResult createToken(UserSession userSession) {
        String userId = userSession.getUserId();
        String token = generateJWTToken(userId);
        refreshToken(token, userSession);

        saveCache(userSession);
        return new LoginResult(token, tokenExpire);
    }

    private void saveCache(UserSession userSession) {
        sessionCache.setCacheValue(userSession.getUserId(), userSession);
    }

    private void refreshToken(String token, UserSession userSession) {
        userSession.setToken(token);
        userSession.setExpireTime(System.currentTimeMillis() + tokenExpire * 1000);
    }

    private static Map<String, Object> getPayload(String userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(Constants.TOKEN_VERSION_KEY, Constants.TOKEN_VERSION);
        payload.put(Constants.TOKEN_IAT_KEY, System.currentTimeMillis());
        payload.put(Constants.TOKEN_ISS_KEY, Constants.TOKEN_ISS);
        payload.put(Constants.TOKEN_SUB_KEY, userId);
        return payload;
    }

    public boolean verifyToken(String token, String secret) {
        if (StringUtils.isBlank(token)) {
            log.info("token is empty verify error");
            return false;
        }
        JWTSigner jwtSigner = JWTSignerUtil.hs256(secret.getBytes(StandardCharsets.UTF_8));
        try {
            return JWTUtil.verify(token, jwtSigner);
        } catch (Exception e) {
            log.info("verify token error");
        }
        return false;
    }

    public String generateJWTToken(String userId) {
        JWTSigner jwtSigner = JWTSignerUtil.hs256(tokenSecret.getBytes(StandardCharsets.UTF_8));
        return JWTUtil.createToken(getPayload(userId), jwtSigner);
    }

    public UserSession getUserSessionByToken(HttpServletRequest request) {
        String authTokenValue = request.getHeader(Constants.AUTHORIZATION_KEY);
        if (StringUtils.isBlank(authTokenValue) || !authTokenValue.startsWith(Constants.TOKEN_PREFIX)){
            log.info("token value is invalid, not start with: {}", Constants.TOKEN_PREFIX);
            return null;
        }

        authTokenValue =  authTokenValue.replace(Constants.TOKEN_PREFIX,"");
        boolean verifyResult = verifyToken(authTokenValue, tokenSecret);
        if (!verifyResult) {
            log.info("verify token fail");
            return null;
        }

        JWT jwt = JWTUtil.parseToken(authTokenValue);
        return sessionCache.getCacheValue((String) jwt.getPayload(Constants.TOKEN_SUB_KEY));
    }

    public void deleteToken(UserSession session) {
        sessionCache.removeCache(session.getUserId());
    }
}
