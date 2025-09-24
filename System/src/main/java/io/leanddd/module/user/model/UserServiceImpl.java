package io.leanddd.module.user.model;

import io.leanddd.component.common.BizException;
import io.leanddd.component.common.Util;
import io.leanddd.component.data.impl.DictionaryProvider;
import io.leanddd.component.event.EntityCreatedEvent;
import io.leanddd.component.event.EntityUpdatedEvent;
import io.leanddd.component.framework.AuthInfo;
import io.leanddd.component.framework.Context;
import io.leanddd.component.framework.MetadataProvider;
import io.leanddd.component.framework.Repository;
import io.leanddd.component.meta.Command;
import io.leanddd.component.meta.Metadata;
import io.leanddd.component.meta.Service;
import io.leanddd.component.meta.Service.Type;
import io.leanddd.module.user.api.UserService;
import io.leanddd.module.user.infra.ConvertUser;
import io.leanddd.module.user.infra.RoleMapper;
import lombok.RequiredArgsConstructor;

import javax.inject.Named;
import java.util.*;

class UserPermissions {
    static final String UserRead = "read";
    static final String UserWrite = "write";
    static final String UserPasswordReset = "pass-reset";
    public static List<Metadata.PermissionDef> permissionDefList = List.of( //
            new Metadata.PermissionDef(UserWrite), //
            new Metadata.PermissionDef(UserRead), //
            new Metadata.PermissionDef(UserPasswordReset)
    );
}

@Service(type = Type.Command, name = "user",
        permissionDomain = "user", permissions = UserPermissions.class, order = 101)
@Named
@RequiredArgsConstructor
class UserServiceImpl implements UserService {

    private final ConvertUser convert;
    private final RoleMapper roleMapper;
    private final DictionaryProvider<User> userDictionaryProvider;
    private final Repository<User> repository;
    private final MetadataProvider metadataProvider;

    @Override
    public io.leanddd.module.user.api.User getById(String id) {
        return convert.doToVo(this.repository.get(id).orElseThrow());
    }

    @Override
    @Command(permission = UserPermissions.UserPasswordReset)
    public void resetPassword(String userId) {
        var user = repository.get(userId).orElseThrow();
        user.resetPassword();
        repository.save(user);
    }

    @Command(permission = UserPermissions.UserWrite)
    public io.leanddd.module.user.api.User create(io.leanddd.module.user.api.User user) {
        User userDo = repository.create(convert.voToDo(user));
        userDo.enrichWithRoles(this.roleMapper::getById);
        var ret = convert.doToVo(repository.save(userDo));
        this.userDictionaryProvider.refresh();
        Context.publishEvent(new EntityCreatedEvent(ret));
        return ret;
    }

    @Command(permission = UserPermissions.UserWrite)
    @Override
    public io.leanddd.module.user.api.User update(String id, io.leanddd.module.user.api.User user) {
        User userDo = repository.get(id).orElseThrow();
        userDo.update(user);
        var ret = convert.doToVo(repository.save(userDo));
        this.userDictionaryProvider.refresh();
        Context.publishEvent(new EntityUpdatedEvent(ret));
        return ret;
    }

    @Override
    @Command(permission = UserPermissions.UserWrite)
    public void delete(String id) throws BizException {
        repository.remove(id);
    }

    @Override
    @Command(permission = UserPermissions.UserWrite)
    public void assignRoles(String userId, String orgId, Set<io.leanddd.module.user.api.User.UserRole> roles) {
        var user = repository.get(userId).orElseThrow();
        user.enrichWithRoles(this.roleMapper::getById);
        var enrichedRoles = convert.voToDo(roles);
        enrichedRoles.forEach(userRole -> userRole.setRole(this.roleMapper.getById(userRole.getRoleId())));
        user.assignRoles(orgId, enrichedRoles);
        var ret = convert.doToVo(repository.save(user));
        Context.publishEvent(new EntityUpdatedEvent(ret));
    }

    @Override
    @Command(permission = UserPermissions.UserWrite)
    public void removeFromOrg(String userId, String orgId) {
        var user = repository.get(userId).orElseThrow();
        user.removeFromOrg(orgId);
        user.enrichWithRoles(this.roleMapper::getById);
        var ret = convert.doToVo(repository.save(user));
        Context.publishEvent(new EntityUpdatedEvent(ret));
    }

    @Override
    public void updateMyProfile(io.leanddd.module.user.api.User user) {
        Util.check(Objects.equals(Context.getUserId(), user.getUserId()));
        var userDo = repository.get(Context.getUserId()).orElseThrow();
        userDo.updateMyProfile(user);
        repository.save(userDo);
    }

    @Override
    public void updateMyOptions(Map<String, Object> options) {
        var userDo = repository.get(Context.getUserId()).orElseThrow();
        userDo.updateMyOptions(options);
        repository.save(userDo);
    }

    @Override
    @Command(logParam = false)
    public void updateMyPassword(UpdatePasswordParams params) throws BizException {
        var userDo = repository.get(Context.getUserId()).orElseThrow();
        userDo.updateMyPassword(params);
        repository.save(userDo);
    }

    @Override
    public AuthInfo login(String userId, Map<String, Object> options) {
        var userDo = repository.get(userId).orElseThrow();
        userDo.enrichWithRoles(this.roleMapper::getRoleWithPermissions);
        var metadata = this.metadataProvider.getMetadata(Locale.getDefault(), null);
        var permissionMap = new HashMap<String, Metadata.PermissionDef>();
        metadata.getServices().forEach(func -> {
            func.getPermissions().forEach(perm -> {
                permissionMap.put(func.getName() + "." + perm.getName(), perm);
            });
        });
        userDo.login(options, permissionMap);
        return repository.save(userDo);
    }
}

