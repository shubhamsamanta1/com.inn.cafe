package com.inn.cafe.JWT;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {

  private String secret = "SHubham@@SamanTA@@1999";

  public String extractUsername(String token){
      return extractClaims(token, Claims::getSubject);
  }

  public Date extractExpiration(String token){
      return extractClaims(token, Claims::getExpiration);
  }
  public <T> T extractClaims(String token, Function<Claims,T> claimsResolver){
       final Claims claims = extractAllClaims(token);
       return claimsResolver.apply(claims);
  }

  public Claims extractAllClaims(String token){

      return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
  }

  private  Boolean isTokenExpired(String Token){
      return extractExpiration(Token).before(new Date());
  }

  public String generatrToken(String username , String role){
      Map<String, Object> claims = new HashMap<>();
      claims.put("role", role);
      return createToken(claims,username);
  }

  private String createToken(Map<String, Object> Claims, String subject){
      return Jwts.builder().setClaims(Claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)).
              signWith(SignatureAlgorithm.HS256,secret).compact();
  }

  public Boolean toValidateToken(String Token, UserDetails userDetails){

      final String username = extractUsername(Token);
      return (username.equals(userDetails.getUsername()) && !isTokenExpired(Token));
  }

}
