package com.simuladorbanco.BancoDigital.config;

import io.jsonwebtoken.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JWTCreator {
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String ROLES_AUTHORITIES = "authorities";

    public static String create(String prefix, String key, JWTObject jwtObject) {
        // Certifique-se de que as roles sejam formatadas corretamente
        List<String> rolesAsString = checkRoles(jwtObject.getRoles());
        // Gera o token com as claims corretas
        String token = Jwts.builder()
                .setSubject(jwtObject.getSubject())
                .setIssuedAt(jwtObject.getIssuedAt())
                .setExpiration(jwtObject.getExpiration())
                .claim(ROLES_AUTHORITIES, rolesAsString)  // Passa as roles já formatadas
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();

        System.out.println("Roles a serem incluídas no token: " + rolesAsString);
        return prefix + " " + token;
    }

    public static JWTObject create(String token,String prefix,String key)
            throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException {
        JWTObject object = new JWTObject();
        token = token.replace(prefix, "");
        Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        object.setSubject(claims.getSubject());
        object.setExpiration(claims.getExpiration());
        object.setIssuedAt(claims.getIssuedAt());
        object.setRoles((List) claims.get(ROLES_AUTHORITIES));
        return object;

    }



    private static List<String> checkRoles(List<String> roles) {
        return roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)  // Adiciona "ROLE_" somente se não existir
                .collect(Collectors.toList());
    }


}