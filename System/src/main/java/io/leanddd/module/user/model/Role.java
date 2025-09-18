package io.leanddd.module.user.model;

import io.leanddd.component.common.BizException;
import io.leanddd.component.common.Util;
import io.leanddd.component.data.BaseEntity;
import io.leanddd.component.data.DictionaryItem;
import io.leanddd.component.data.EntityHelper;
import io.leanddd.component.meta.Meta;
import io.leanddd.component.meta.Meta.Category;
import io.leanddd.component.meta.Meta.Type;
import io.leanddd.component.meta.MetaEntity;
import io.leanddd.module.user.api.Role.RoleType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

import static io.leanddd.component.meta.Meta.BooleanEx.False;
import static io.leanddd.component.meta.Meta.BooleanEx.True;

@EqualsAndHashCode(callSuper = false)
@MetaEntity(tableName = "t_role")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseEntity<Role> implements DictionaryItem {

    private static final long serialVersionUID = 1L;
    private static EntityHelper<Role> entityHelper = new EntityHelper<Role>(Role.class).init();

    @Meta(value = Type.ID, hidden = False, listable = True)
    private String roleId;
    @Meta(category = Category.DisplayName, editable = True)
    private String roleName;
    @Meta(value = Type.Enum, editable = True)
    private Boolean enabled;

    // TODO conflict with departmentID?
    @Meta(value = Type.RefID)
    private String orgId;
    @Meta(value = Type.Enum, listable = True, editable = True, searchable = True, nullable = False) //
    private RoleType roleType;
    @Meta(value = Type.Enum, listable = True, editable = False)
    private Boolean workflowGroup;
    @Meta(value = Type.ToMany)
    private List<RolePermission> _permissions;

    public void init() {
        if (this.enabled == null) {
            this.enabled = true;
        }
        Util.check(roleType != null);
    }

    public List<String> getPermissions() {
        return _permissions == null ? null : Util.mapToList(_permissions.stream(), perm -> perm.getPermission());
    }

    @Override
    public String getType() {
        return "Role";
    }

    @Override
    public Object getValue() {
        return roleId;
    }

    @Override
    public String getLabel() {
        return roleName;
    }

    @Override
    public void update(Object obj) {
        entityHelper.update(this, obj);
    }

    public void delete() throws BizException {
        Util.checkBiz(this.workflowGroup == null || !this.workflowGroup, Util.GenericError, "can't remove workflow group, can only be set disabled");
        this.setDelFlag(true);
    }

    public void assignPermissions(List<String> permissions) {
        if (permissions == null) this._permissions = null;
        this._permissions = Util.mapToList(permissions.stream(), perm -> new RolePermission(this.roleId, perm));
    }

    @MetaEntity(tableName = "t_role_permission")
    @Data
    @RequiredArgsConstructor
    public static class RolePermission implements Serializable {
        @Meta(value = Type.RefID, nullable = False)
        private final String roleId;
        @Meta(value = Type.String, nullable = False)
        private final String permission;
        @Meta(Type.ID)
        String rolePermissionId = null;
        @Meta(value = Type.Integer, hidden = True)
        Integer index;
    }
}
