package io.leanddd.module.user.model;

import io.leanddd.component.common.BizException;
import io.leanddd.component.common.Util;
import io.leanddd.component.data.BaseEntity;
import io.leanddd.component.data.DictionaryItem;
import io.leanddd.component.data.EntityHelper;
import io.leanddd.component.framework.AuthInfo;
import io.leanddd.component.framework.Context;
import io.leanddd.component.meta.Meta;
import io.leanddd.component.meta.Meta.Category;
import io.leanddd.component.meta.Meta.Type;
import io.leanddd.component.meta.MetaEntity;
import io.leanddd.component.meta.Metadata.PermissionDef;
import io.leanddd.module.user.api.User.UserStatus;
import io.leanddd.module.user.api.UserService.UpdatePasswordParams;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import static io.leanddd.component.meta.Meta.BooleanEx.False;
import static io.leanddd.component.meta.Meta.BooleanEx.True;

@Getter
@EqualsAndHashCode(callSuper = false)
@MetaEntity(tableName = "t_user")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity<User> implements UserDetails, AuthInfo, DictionaryItem {

    private static final long serialVersionUID = 1L;

    private static PassEncoder passEncoder = Context.getBean("passEncoder", PassEncoder.class);
    private static EntityHelper<User> entityHelper = new EntityHelper<User>(User.class).init();

    @Meta(value = Type.ID, searchable = True)
    private String userId;

    @Meta(value = Type.String, searchable = True, nullable = False, unique = {
            "login_name"}, editable = True, listable = False)
    private String loginName;

    @Meta(category = Category.PersonName, nullable = False)
    private String username;

    @Meta(value = Type.String, editable = True, searchable = True)
    private String userCode;

    @Meta(value = Type.ToMany, refData = "roleId,roleName", nullable = False, searchable = True, editable = False)
    private Set<UserRole> roles;

    @Meta(value = Type.Enum, editable = True)
    private UserStatus status;

    @Meta(category = Category.Password)
    private String password;

    @Meta(category = Category.Phone, editable = True)
    private String phone;

    @Meta(value = Type.String, editable = True)
    private String remark;

    @Meta(value = Type.ToMany, persistable = False)
    private Set<String> permissions;

    // for UserDetails interface
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.status != UserStatus.Disabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.status != UserStatus.Disabled;
    }

    // for DictionaryItem interface
    @Override
    public String getType() {
        return "User";
    }

    @Override
    public Object getValue() {
        return this.userId;
    }

    @Override
    public String getLabel() {
        return this.username;
    }

    /* constructor
   public User(User state) {
       this.state.setPassword(passEncoder.encode(getDefaultPassword()));
   }
    */
    private String getDefaultPassword() {
        return Context.getProperty("app.user.initialPassword");
    }

    @Override
    public void update(Object obj) {
        entityHelper.update(this, obj);
    }

    public void updateMyProfile(io.leanddd.module.user.api.User user) {
        this.username = user.getUsername();
        this.phone = user.getPhone();
    }

    public void updateMyPassword(UpdatePasswordParams params) throws BizException {
        Util.checkBiz(passEncoder.matches(params.oldPass, this.password), "401", "原密码不正确");
        this.password = passEncoder.encode(params.newPass);
    }

    public void resetPassword() {
        password = passEncoder.encode(getDefaultPassword());
    }

    public void enrichWithRoles(Function<String, Role> func) {
        if (this.roles == null) return;
        this.roles.forEach(userRole -> {
            userRole.setRole(func.apply(userRole.getRoleId()));
        });
    }

    // only update roles (in input param) for specified orgId
    public void assignRoles(String orgId, Set<UserRole> roles) {
        this.roles.removeIf(role -> Util.isEmpty(orgId) ? Util.isEmpty(role.getOrgId()) : Objects.equals(orgId, role.getOrgId()));
        this.roles.addAll(Util.mapToList(roles.stream().filter(role -> Util.isEmpty(orgId) ? (role.getRole().getRoleType() == io.leanddd.module.user.api.Role.RoleType.Global) : Objects.equals(orgId, role.getOrgId())), role -> {
            if (role.getCreateTime() == null)
                role.setCreateTime(new Date());
            return role;
        }));
    }

    public void removeFromOrg(String orgId) {
        this.roles.removeIf(role -> Objects.equals(role.getOrgId(), orgId));
    }

    public void initPermissions(Map<String, PermissionDef> permissionDefMap) {
        Set<String> permissions = new HashSet<String>();
        for (var ur : roles) {
            var role = ur.getRole();
            if (role != null && ur.getRole().get_permissions() != null)
                for (var rp : role.get_permissions()) {
                    var permission = rp.getPermission();
                    var permissionDef = permissionDefMap.get(permission);
                    if (permissionDef != null) {
                        if (permissionDef.getName().endsWith("@") && role.getRoleType() != io.leanddd.module.user.api.Role.RoleType.Global) {
                            permission = permission + ur.getOrgId();
                        }
                    }
                    permissions.add(permission); // for menu permission = function name
                }
        }
        this.permissions = permissions;
    }

    @MetaEntity(tableName = "t_user_role")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserRole implements Serializable {

        @Meta(value = Type.ID)
        private String id;

        @Meta(value = Type.RefID, nullable = False)
        private String userId;

        @Meta(value = Type.RefID, nullable = False, searchable = True)
        private String roleId;

        @Meta(value = Type.RefID, searchable = True)
        private String orgId;

        @Meta(value = Type.Integer, hidden = True)
        private Integer index;

        @Meta(persistable = False, searchable = True)
        private Role role;

        @Meta(value = Type.Date)
        private Date createTime;
    }
}

