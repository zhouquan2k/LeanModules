package io.leanddd.module.security.model;

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
import io.leanddd.module.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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

    protected final MetadataProvider metadataProvider;
    private final AuthenticationManager authenticationManager;
    private final ITokenUtil tokenUtil;


    @Command(logParam = false)
    @Override
    public AuthInfo login(@Validated @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                loginRequest.getPassword());

        var authentication = authenticationManager.authenticate(authenticationToken);
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        var authInfo = (AuthInfo) userDetails;
        SecurityContextHolder.getContext().setAuthentication(authentication);
        this.processUserPermissions(userDetails);
        return tokenUtil.generateAuthResult(authInfo, request);
    }

    private void processUserPermissions(UserDetails user) {
        var userDo = (User) user;
        var metadata = this.metadataProvider.getMetadata(Locale.getDefault(), null);
        var permissionMap = new HashMap<String, PermissionDef>();
        metadata.getServices().forEach(func -> {
            func.getPermissions().forEach(perm -> {
                permissionMap.put(func.getName() + "." + perm.getName(), perm);
            });
        });
        // TODO call domain outside package
        userDo.initPermissions(permissionMap);
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
}
