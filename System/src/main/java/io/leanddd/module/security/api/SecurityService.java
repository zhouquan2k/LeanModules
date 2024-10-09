package io.leanddd.module.security.api;

import io.leanddd.component.framework.AuthInfo;
import io.leanddd.component.meta.Metadata.ServiceDef;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RequestMapping("/api/security")
public interface SecurityService {

    @PostMapping("/login")
    AuthInfo login(@RequestBody LoginRequest loginRequest, HttpServletRequest request);

    @GetMapping("/info")
    AuthInfo getUserInfo(HttpServletRequest request);

    @PostMapping("/logout")
    void logout(HttpServletRequest request);

    @GetMapping("/functions")
    List<ServiceDef> getAllFunctionDefs();

    @Data
    public static class LoginRequest {
        @NotBlank
        private String username;

        @NotBlank
        private String password;

        private String code;
    }
}
