package com.aiswift.MultiTenancy;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;


@Component
public class JwtUtil {

	private static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build(); 

	private SecretKey getSigningKey1() {
//		System.out.println("Using key: " + Base64.getEncoder().encodeToString(SECRET_KEY.getEncoded()));
		return SECRET_KEY;
	}
	
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}
	
	public List<SimpleGrantedAuthority> extractAuthorities(String token) {
		  List<String> authorities = extractClaim(token, claims -> 
	        claims.get("authorities", List.class));
	    
	    if (authorities != null) {
	        return authorities.stream()
	            .map(SimpleGrantedAuthority::new)
	            .collect(Collectors.toList());
	    }
	    
	    return Collections.emptyList();
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		System.out.println("I a in extractclaim");
		final Claims claims = extractAllClaims(token);
		System.out.println("done extract all claims");
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		System.out.println("I am in extract all claims");
		try {
			Claims claims = Jwts.parser().verifyWith(getSigningKey1()).build().parseSignedClaims(token).getPayload();
			System.out.println("printing the result: " + claims);
			return claims;
		} catch (Exception e) {
			System.out.println("Error parsing JWT: " + e.getMessage());
			e.printStackTrace();
			throw e; // Re-throw the exception or handle it as needed
		}
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	// generate access token
	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		
	    // Extract authorities from UserDetails and add to claims
		
	    List<String> authorities = userDetails.getAuthorities().stream()
	        .map(authority -> authority.getAuthority())
	        .collect(Collectors.toList()); // this returns a list of authority string, not GrantedAuthority type
	    
	    claims.put("authorities", authorities);
	    
		return createToken(claims, userDetails.getUsername());
	}
	
	//generate refresh token
	public String generateRefreshToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>(); 
		return createToken(claims, userDetails.getUsername());
	}
	
	//access token-valid for 1 day
	private String createToken(Map<String, Object> claims, String subject) {

		return Jwts.builder().claims(claims).subject(subject).issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)).signWith(getSigningKey1()) 
				.compact();

	}
	//refresh token-valid for 7 days
	private String createRefreshToken(Map<String, Object> claims, String subject) {

		return Jwts.builder().claims(claims).subject(subject).issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24*7)).signWith(getSigningKey1()) 
				.compact();

	}

	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);

		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
}
