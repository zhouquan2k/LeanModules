package io.leanddd.module.security.api;

import io.leanddd.component.framework.AuthInfo;
import io.leanddd.component.meta.Metadata.ServiceDef;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

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

    @PostMapping("/register/send-code")
    void sendRegisterCode(@RequestBody SendRegisterCodeRequest request);

    @PostMapping("/register")
    void register(@RequestBody RegisterRequest request);

    @PostMapping("/register/verify")
    void verifyRegister(@RequestBody VerifyRegisterRequest request);

    @Data
    public static class LoginRequest {
        @NotBlank
        private String username;

        @NotBlank
        private String password;

        private String code;

        private Map<String, Object> options;
    }

    @Data
    class SendRegisterCodeRequest {
        @NotBlank
        @Email
        private String email;
    }

    @Data
    class RegisterRequest {
        @NotBlank
        @javax.validation.constraints.Pattern(regexp = "^[A-Za-z0-9_.-]{3,}$", message = "登录名需由字母、数字或 _.- 组成，长度至少 3")
        private String loginName;

        @NotBlank
        private String username;

        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String password;

        @NotBlank
        private String code;
    }

    @Data
    class VerifyRegisterRequest {
        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String code;
    }
}
