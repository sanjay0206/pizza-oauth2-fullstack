package com.oauth.pizzaservice.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthConverter.class);
    private static final String SCOPE_PREFIX = "SCOPE_";

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> extractResourceRoles = extractResourceRoles(jwt);
        Collection<GrantedAuthority> extractRoles = extractRoles(jwt);

        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.addAll(extractResourceRoles);
        authorities.addAll(extractRoles);

        logger.info("authorities: {}", authorities);

        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("sub"));
    }

    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
        if (jwt.getClaim("scope") == null) {
            return Set.of();
        }

        List<String> scopes = jwt.getClaimAsStringList("scope");
        return scopes.stream()
                .map(scope -> new SimpleGrantedAuthority(SCOPE_PREFIX + scope))
                .collect(Collectors.toSet());
    }

    private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
        if (jwt.getClaim("roles") == null) {
            return Set.of();
        }

        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
