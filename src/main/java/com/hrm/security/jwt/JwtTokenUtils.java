package com.hrm.security.jwt;

import com.hrm.cache.LoggedOutJwtTokenCache;
import com.hrm.event.OnUserLogoutSuccessEvent;
import com.hrm.exception.InvalidTokenRequestException;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtils.class);

    @Value("${hrm.jwtSecret}")
    private String jwtSecret;

    @Value("${hrm.jwtExpirationMs}")
    private int jwtExpirationMs;

    private LoggedOutJwtTokenCache log;

    public void setLoggedOutJwtTokenCache(LoggedOutJwtTokenCache loggedOutJwtTokenCache) {
        this.log = loggedOutJwtTokenCache;
    }

    public String generateJwtToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        return Jwts.builder().setClaims(claims).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret).compact();
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public Date getExtAt(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getExpiration();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            validateTokenIsNotForALoggedOutDevice(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public Date getTokenExpiryFromJWT(String token) {
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();

        return claims.getExpiration();
    }

    private void validateTokenIsNotForALoggedOutDevice(String authToken) {
        OnUserLogoutSuccessEvent previouslyLoggedOutEvent = log.getLogoutEventForToken(authToken);
        if (previouslyLoggedOutEvent != null) {
            String loginId = previouslyLoggedOutEvent.getEmail();
            Date logoutEventDate = previouslyLoggedOutEvent.getEventTime();
            String errorMessage = String.format(
                    "Token corresponds to an already logged out user [%s] at [%s]. Please login again", loginId,
                    logoutEventDate);
            throw new InvalidTokenRequestException("JWT", authToken, errorMessage);
        }
    }
}
