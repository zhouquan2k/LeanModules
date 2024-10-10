package io.leanddd.module.user.model;

import io.leanddd.component.common.BizException;
import io.leanddd.component.event.EntityCreatedEvent;
import io.leanddd.component.event.EntityUpdatedEvent;
import io.leanddd.component.framework.Context;
import io.leanddd.component.framework.Repository;
import io.leanddd.component.meta.Command;
import io.leanddd.component.meta.Metadata;
import io.leanddd.component.meta.Service;
import io.leanddd.component.meta.Service.Type;
import io.leanddd.module.user.api.RoleService;
import io.leanddd.module.user.infra.ConvertRole;
import io.leanddd.module.user.infra.RoleMapper;
import lombok.RequiredArgsConstructor;

import javax.inject.Named;
import java.util.List;

class RolePermissions {
    static final String RoleRead = "read";
    static final String RoleWrite = "write";
    static final String RoleAssign = "assign";
    public static List<Metadata.PermissionDef> permissionDefList = List.of( //
            new Metadata.PermissionDef(RoleRead), //
            new Metadata.PermissionDef(RoleWrite), //
            new Metadata.PermissionDef(RoleAssign)
    );
}

@Service(type = Type.Mixed, name = "role", permissions = RolePermissions.class, permissionDomain = "role", order = 102)
@Named
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final Repository<Role> repository;
    private final ConvertRole convert;
    private final RoleMapper roleMapper;

    @Override
    @Command(permission = RolePermissions.RoleAssign)
    public void assignRolePermissions(String roleId, List<String> permissionCodes) {
        var role = this.repository.get(roleId).orElseThrow();
        role.assignPermissions(permissionCodes);
        this.repository.save(role);
    }

    @Override
    public io.leanddd.module.user.api.Role getById(String id) {
        return convert.doToVo(repository.get(id).orElseThrow());
    }

    @Override
    @Command(permission = RolePermissions.RoleWrite)
    public io.leanddd.module.user.api.Role create(io.leanddd.module.user.api.Role role) {
        var roleDo = repository.create(convert.voToDo(role));
        var ret = convert.doToVo(repository.save(roleDo));
        Context.publishEvent(new EntityCreatedEvent(ret));
        return ret;
    }

    @Override
    @Command(permission = RolePermissions.RoleWrite)
    public io.leanddd.module.user.api.Role update(String id, io.leanddd.module.user.api.Role role) {
        var roleDo = repository.get(id).orElseThrow();
        roleDo.update(role);
        var ret = convert.doToVo(repository.save(roleDo));
        Context.publishEvent(new EntityUpdatedEvent(ret));
        return ret;
    }

    @Command
    @Override
    public void delete(String id) throws BizException {
        var roleDo = this.repository.get(id).orElseThrow();
        roleDo.delete();
        this.repository.save(roleDo);
    }

    // Queries

    @Override
    public List<io.leanddd.module.user.api.Role> getRoles(io.leanddd.module.user.api.Role.RoleType roleType) {
        if (roleType != null) {
            return convert.doToVo(roleMapper.queryByType(roleType.name()));
        }
        return convert.doToVo(roleMapper.queryAll());
    }

    @Override
    public List<io.leanddd.module.user.api.Role> queryByOrgId(String orgId) {
        return convert.doToVo(roleMapper.queryByOrgId(orgId));
    }

}

