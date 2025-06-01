package com.oauth.authorizationserver.security;

import com.oauth.authorizationserver.config.CorsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import java.security.Principal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class OAuth2Config {

    private final PasswordEncoder passwordEncoder;
    private final CorsConfig corsConfig;
    private final JwtAuthConverter jwtAuthConverter;

    @Autowired
    public OAuth2Config(PasswordEncoder passwordEncoder, CorsConfig corsConfig, JwtAuthConverter jwtAuthConverter) {
        this.passwordEncoder = passwordEncoder;
        this.corsConfig = corsConfig;
        this.jwtAuthConverter = jwtAuthConverter;
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            Authentication principal = context.getPrincipal();

            if (context.getTokenType().getValue().equals("access_token") &&
                    principal instanceof UsernamePasswordAuthenticationToken auth) {
                Object principalObj = auth.getPrincipal();
                if (principalObj instanceof UserDetails user) {
                    context.getClaims().claim("name", user.getUsername());
                    context.getClaims().claim("email", user.getUsername() + "@example.com");
                    context.getClaims().claim("preferred_username", user.getUsername());
                    context.getClaims().claim("roles", user.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList()));
                }
            }
        };
    }

    private OidcUserInfo customUserInfoMapper(OidcUserInfoAuthenticationContext context) {
        Principal principal = context.getAuthorization().getAttribute(Principal.class.getName());

        if (!(principal instanceof Authentication authentication) ||
                !(authentication.getPrincipal() instanceof UserDetails user)) {
            throw new IllegalStateException("User principal is not available or not valid");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getUsername());
        claims.put("name", user.getUsername());
        claims.put("email", user.getUsername() + "@example.com");
        claims.put("roles", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        claims.put("scope", context.getAuthorization().getAuthorizedScopes());

        return new OidcUserInfo(claims);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        // Enable OpenID Connect
        authorizationServerConfigurer
                .oidc(oidc ->
                        oidc.userInfoEndpoint(userInfo ->
                                userInfo.userInfoMapper(this::customUserInfoMapper))
                );

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/oauth2/token",
                                "/oauth2/jwks",
                                "/userinfo"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                )
                .formLogin(Customizer.withDefaults())
                .apply(authorizationServerConfigurer);

        return http.build();
    }

    @Bean
    RegisteredClientRepository registeredClientRepository() {
        RegisteredClient client = RegisteredClient
                .withId("pizza-client")
                .clientId("pizza-client")
                .clientSecret(passwordEncoder.encode("secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                // Add multiple grant types
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:5173/callback")
                // Scopes
                .scope("api.read")
                .scope("openid") // For OpenID Connect
                .scope("profile")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .build())
                .build();
        return new InMemoryRegisteredClientRepository(client);
    }
}