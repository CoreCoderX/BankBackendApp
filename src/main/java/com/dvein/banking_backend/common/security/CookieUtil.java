package com.dvein.banking_backend.common.security;

import com.dvein.banking_backend.common.config.CookieConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final CookieConfig cookieConfig;

    public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(cookieConfig.getPath());
        cookie.setHttpOnly(cookieConfig.getHttpOnly());
        cookie.setSecure(cookieConfig.getSecure());
        cookie.setMaxAge(maxAge);

        if (cookieConfig.getDomain() != null) {
            cookie.setDomain(cookieConfig.getDomain());
        }

        response.addCookie(cookie);
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        addCookie(
                response,
                cookieConfig.getName().getRefreshToken(),
                token,
                cookieConfig.getMaxAge()
        );
    }

    public void addDeviceIdCookie(HttpServletResponse response, String deviceId) {
        addCookie(
                response,
                cookieConfig.getName().getDeviceId(),
                deviceId,
                cookieConfig.getMaxAge()
        );
    }

    public Optional<String> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst();
    }

    public Optional<String> getRefreshToken(HttpServletRequest request) {
        return getCookie(request, cookieConfig.getName().getRefreshToken());
    }

    public Optional<String> getDeviceId(HttpServletRequest request) {
        return getCookie(request, cookieConfig.getName().getDeviceId());
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        addCookie(response, name, "", 0);
    }

    public void deleteRefreshToken(HttpServletResponse response) {
        deleteCookie(response, cookieConfig.getName().getRefreshToken());
    }

    public void deleteDeviceId(HttpServletResponse response) {
        deleteCookie(response, cookieConfig.getName().getDeviceId());
    }
}