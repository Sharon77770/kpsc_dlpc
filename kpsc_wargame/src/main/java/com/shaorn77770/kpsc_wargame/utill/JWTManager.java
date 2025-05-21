package com.shaorn77770.kpsc_wargame.utill;

import java.util.Date;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JWTManager {
    private String jwtkey;

    private long tokenValidTime = 1000L * 60 * 60 * 24; //60분
	
	public String createToken(String userId, String pw) {
		Claims claims = Jwts.claims().setId(userId);
		Date now = new Date();
		return Jwts.builder().setClaims(claims).setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + tokenValidTime))
				.setIssuer("kpsc.dlpc")
				.signWith(SignatureAlgorithm.HS256, jwtkey).compact();
	}
	
	public Jws<Claims> getClaims(String jwt) {
		try {
			return Jwts.parser().setSigningKey(jwtkey).parseClaimsJws(jwt);
		} catch (Exception e) {
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
