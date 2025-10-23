package io.leanddd.module.security.model;

import io.leanddd.component.common.BizException;
import io.leanddd.component.common.Util;
import io.leanddd.component.framework.AuthInfo;
import io.leanddd.component.framework.MetadataProvider;
import io.leanddd.component.meta.Command;
import io.leanddd.component.meta.Metadata.PermissionDef;
import io.leanddd.component.meta.Metadata.ServiceDef;
import io.leanddd.component.meta.Service;
import io.leanddd.component.security.AuthResult;
import io.leanddd.component.security.ITokenUtil;
import io.leanddd.module.security.api.SecurityService;
import io.leanddd.module.user.api.User;
import io.leanddd.module.user.api.UserService;
import io.leanddd.module.user.infra.UserMapper;
import io.leanddd.module.user.model.PassEncoder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

class SecurityPermissions {
    static final String AdminPermissions = "***";
    static final String TestPermissions = "test*";
    public static List<PermissionDef> permissionDefList = List.of( //
            new PermissionDef(AdminPermissions), //
            new PermissionDef(TestPermissions));
}

@Service(type = Service.Type.Mixed, name = "security", permissions = SecurityPermissions.class, order = 103)
@RequiredArgsConstructor
@Named
public class SecurityServiceImpl implements SecurityService {

    private static final Logger log = LoggerFactory.getLogger(SecurityServiceImpl.class);
    private static final String REGISTER_CODE_KEY_PREFIX = "security:register:code:";
    private static final String REGISTER_PENDING_KEY_PREFIX = "security:register:pending:";
    private static final String REGISTER_LIMIT_KEY_PREFIX = "security:register:limit:";
    private static final String REGISTER_ACTIVE_KEY_PREFIX = "security:register:active:";
    private static final long REGISTER_CODE_TTL_MINUTES = 10L;
    private static final long REGISTER_LIMIT_SECONDS = 60L;
    private static final SecureRandom RANDOM = new SecureRandom();

    protected final MetadataProvider metadataProvider;
    private final AuthenticationManager authenticationManager;
    private final ITokenUtil tokenUtil;
    private final UserService userService;
    private final UserMapper userMapper;
    private final PassEncoder passEncoder;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.username:}")
    String mailFrom;


    @Command(logParam = false)
    @Override
    public AuthInfo login(@Validated @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                loginRequest.getPassword());
        var authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var options = loginRequest.getOptions();
        final AuthInfo auth = (AuthInfo) authentication.getPrincipal();
        var authInfo = (AuthInfo)userService.login(auth.getUserId(), options);
        return tokenUtil.generateAuthResult(authInfo, request);
    }

    @Override
    public AuthResult getUserInfo(HttpServletRequest request) {
        return tokenUtil.getAuthInfoFromToken(request);
    }

    @Override
    @Command(logParam = false)
    public void logout(HttpServletRequest request) {
        tokenUtil.invalidateToken(request);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Override
    public List<ServiceDef> getAllFunctionDefs() {
        var metadata = this.metadataProvider.getMetadata(Locale.getDefault(), null);
        return Util.toList(metadata.getServices().stream().filter(x -> x.getOrder() > 0)
                .sorted((x, y) -> x.getOrder() - y.getOrder()));
    }

    @Override
    public void sendRegisterCode(@Validated @RequestBody SendRegisterCodeRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (isEmailActive(email)) {
            throw new BizException(400, "该邮箱已注册，请直接登录");
        }

        throttle(email);
        String code = generateVerificationCode();
        stringRedisTemplate.opsForValue().set(buildRegisterCodeKey(email), code, Duration.ofMinutes(REGISTER_CODE_TTL_MINUTES));
        sendVerificationMail(email, code);
    }

    @Override
    @Command
    public void register(@Validated @RequestBody RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        // TODO 验证码，暂未实现
        // ensureValidCode(email, request.getCode());

        String loginName = request.getLoginName() != null ? request.getLoginName().trim() : null;
        if (!StringUtils.hasText(loginName)) {
            throw new BizException(400, "登录名不能为空");
        }

        String username = request.getUsername() != null ? request.getUsername().trim() : null;
        if (!StringUtils.hasText(username)) {
            throw new BizException(400, "昵称不能为空");
        }

        var existingUser = userMapper.getUserByLoginName(loginName);
        if (existingUser != null) {
            if (existingUser.getStatus() == io.leanddd.module.user.api.User.UserStatus.Active) {
                throw new BizException(400, "登录名已存在，请更换");
            }
            throw new BizException(400, "该登录名对应的账号已创建但尚未验证，请使用原登录名完成验证");
        }

        if (isEmailActive(email)) {
            throw new BizException(400, "该邮箱已注册，请直接登录");
        }

        var pendingUserId = stringRedisTemplate.opsForValue().get(buildRegisterPendingKey(email));
        if (StringUtils.hasText(pendingUserId)) {
            throw new BizException(400, "该邮箱已提交注册，请查收邮件完成验证");
        }

        var user = User.builder()
                .loginName(loginName)
                .username(username)
                .email(email)
                .status(User.UserStatus.RegisterPending)
                .password(request.getPassword())
                .build();
        var newUser = userService.create(user);
        stringRedisTemplate.opsForValue().set(buildRegisterPendingKey(email), newUser.getUserId(), Duration.ofMinutes(REGISTER_CODE_TTL_MINUTES));
    }

    @Override
    public void verifyRegister(@Validated @RequestBody VerifyRegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (isEmailActive(email)) {
            cleanupVerificationCache(email);
            return;
        }
        ensureValidCode(email, request.getCode());

        var pendingUserId = stringRedisTemplate.opsForValue().get(buildRegisterPendingKey(email));
        if (!StringUtils.hasText(pendingUserId)) {
            throw new BizException(400, "该邮箱未提交注册或验证码已过期，请重新注册");
        }
        userService.activate(pendingUserId);
        stringRedisTemplate.opsForValue().set(buildRegisterActiveKey(email), pendingUserId);
        cleanupVerificationCache(email);
    }

    private void cleanupVerificationCache(String email) {
        stringRedisTemplate.delete(List.of(buildRegisterCodeKey(email), buildRegisterPendingKey(email), buildRegisterLimitKey(email)));
    }

    private void throttle(String email) {
        var limitKey = buildRegisterLimitKey(email);
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(limitKey, "1", Duration.ofSeconds(REGISTER_LIMIT_SECONDS));
        if (Boolean.FALSE.equals(success)) {
            throw new BizException(400, "验证码发送过于频繁，请稍后再试");
        }
    }

    private void ensureValidCode(String email, String code) {
        String cachedCode = stringRedisTemplate.opsForValue().get(buildRegisterCodeKey(email));
        if (!StringUtils.hasText(cachedCode)) {
            throw new BizException(400, "验证码已过期，请重新发送");
        }
        if (!Objects.equals(cachedCode, code)) {
            throw new BizException(400, "验证码错误，请重新输入");
        }
    }

    private void sendVerificationMail(String email, String code) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("MailSender 未配置，无法发送验证码到 {}", email);
            throw new BizException(500, "邮件服务未配置，暂时无法发送验证码");
        }
        var message = new SimpleMailMessage();
        message.setTo(email);
        if (StringUtils.hasText(mailFrom)) {
            message.setFrom(mailFrom);
        }
        message.setSubject("RetroTimes 邮箱验证");
        message.setText(String.format("您的注册验证码为 %s ，有效期 %d 分钟。", code, REGISTER_CODE_TTL_MINUTES));
        mailSender.send(message);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateVerificationCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String generateUserId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String buildRegisterCodeKey(String email) {
        return REGISTER_CODE_KEY_PREFIX + email;
    }

    private String buildRegisterPendingKey(String email) {
        return REGISTER_PENDING_KEY_PREFIX + email;
    }

    private String buildRegisterLimitKey(String email) {
        return REGISTER_LIMIT_KEY_PREFIX + email;
    }

    private String buildRegisterActiveKey(String email) {
        return REGISTER_ACTIVE_KEY_PREFIX + email;
    }

    private boolean isEmailActive(String email) {
        return StringUtils.hasText(stringRedisTemplate.opsForValue().get(buildRegisterActiveKey(email)));
    }
}
