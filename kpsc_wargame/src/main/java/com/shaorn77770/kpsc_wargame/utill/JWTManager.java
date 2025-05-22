package com.shaorn77770.kpsc_wargame.utill;

import java.util.Date;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JWTManager {
    private static final Logger logger = LogManager.getLogger(JWTManager.class);
    private String jwtkey;

    private long tokenValidTime = 1000L * 60 * 60 * 24; //60분
	
	public String createToken(String userId, String pw) {
		try {
			Claims claims = Jwts.claims().setId(userId);
			Date now = new Date();
			return Jwts.builder().setClaims(claims).setIssuedAt(now)
					.setExpiration(new Date(now.getTime() + tokenValidTime))
					.setIssuer("kpsc.dlpc")
					.signWith(SignatureAlgorithm.HS256, jwtkey).compact();
		} catch (Exception e) {
			logger.error("JWT 토큰 생성 실패: {}", userId, e);
			throw e;
		}
	}
	
	public Jws<Claims> getClaims(String jwt) {
		try {
			return Jwts.parser().setSigningKey(jwtkey).parseClaimsJws(jwt);
		} catch (Exception e) {
			logger.warn("JWT 파싱 실패: {}", jwt, e);
			return null;
		}
	}
	
	public boolean isEnd(Jws<Claims> claims) {
		return claims.getBody().getExpiration().before(new Date());
	}

	public String getId(Jws<Claims> claims) {
		return claims.getBody().getId();
	}

	public void setJwtkey(String jwtkey) {
		this.jwtkey = jwtkey;
	}

	public String getJwtkey() {
		return jwtkey;
	}
}
