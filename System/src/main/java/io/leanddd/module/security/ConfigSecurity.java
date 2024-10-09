package io.leanddd.module.security;

import io.leanddd.component.common.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

@Configuration
public class ConfigSecurity {

    @Value("${app.security.testPassword:none}")
    String testPassword;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new MyPasswordEncoder(testPassword);
    }

    static class MyPasswordEncoder implements PasswordEncoder {
        PasswordEncoder internal = new BCryptPasswordEncoder();
        private String testPassword;

        MyPasswordEncoder(String testPassword) {
            this.testPassword = testPassword;
        }

        @Override
        public String encode(CharSequence rawPassword) {
            return internal.encode(rawPassword);
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return Util.isNotEmpty(testPassword) && Objects.equals(rawPassword, testPassword)
                    || internal.matches(rawPassword, encodedPassword);
        }
    }
}

