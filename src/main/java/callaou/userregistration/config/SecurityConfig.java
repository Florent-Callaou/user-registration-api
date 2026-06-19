package callaou.userregistration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration class for setting up security filters.
 */
@Configuration
public class SecurityConfig {

        /**
         * Configures the security filter chain for the application.
         *
         * @param http the HttpSecurity object to configure
         * @return the configured SecurityFilterChain
         * @throws Exception if an error occurs during configuration
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) {
                http
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/h2-console/**").permitAll()
                                                .requestMatchers("/api/**").permitAll()
                                                .anyRequest().authenticated())
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/h2-console/**", "/api/**"))
                                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

                return http.build();
        }

}
