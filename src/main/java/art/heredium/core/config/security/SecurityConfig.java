package art.heredium.core.config.security;

import art.heredium.core.config.properties.CorsProperties;
import art.heredium.core.jwt.AuthenticationFilter;
import art.heredium.core.util.HeaderUtil;
import art.heredium.domain.account.type.AuthType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
//@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationFilter authenticationFilter;
    private final CorsProperties corsProperties;

    public static String[] permitAll = { "/api/test/**",
            "/api/admin/auth/**", "/api/user/auth/**",
            "/api/oauth2/**",
            "/api/user/common/**",
            "/api/user/docents/**",
            "/api/user/policies/**",
            "/api/user/payments/**",
            "/api/user/tickets/info/*",
            "/api/user/tickets/group",
            "/api/user/tickets/non-user/**",
            "/api/user/tickets/hana-bank/**",
            "/api/user/non-user/**",
            "/api/user/hana-bank/**",
            "/api/user/exhibitions/**",
            "/api/user/programs/**",
            "/api/user/coffees/**",
            "/api/user/events/**", "/api/user/notices/**", "/api/user/common/**",
            "/api/health-check", "/api/file/**", "/api/nice/**"};

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors() // cors request 이슈
                .configurationSource(corsConfigurationSource())
                .and()
                .httpBasic().disable() // 기본설정 해제
                .headers()
                .cacheControl().disable()
                .addHeaderWriter(new StaticHeadersWriter("Cache-Control", " no-cache,max-age=0, must-revalidate"))
                .addHeaderWriter(new StaticHeadersWriter("Expires", "0"))
                .and()
                .csrf().disable() // csrf 토큰 안씀
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 제거(토큰기반이라)
                .and()
                .authorizeRequests()
                .antMatchers(permitAll).permitAll()
                .antMatchers("/api/**").hasAnyRole(AuthType.getAPIRole())
                .anyRequest().permitAll()
                .and()
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setAllowCredentials(true);
        configuration.addAllowedHeader(HeaderUtil.HEADER_AUTHORIZATION);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
