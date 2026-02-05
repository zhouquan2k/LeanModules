package io.leanddd.module.department.model;

import io.leanddd.component.data.BaseEntity;
import io.leanddd.component.data.DictionaryItem;
import io.leanddd.component.data.EntityHelper;
import io.leanddd.component.meta.Meta;
import io.leanddd.component.meta.Meta.Type;
import io.leanddd.component.meta.MetaEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static io.leanddd.component.meta.Meta.BooleanEx.False;
import static io.leanddd.component.meta.Meta.BooleanEx.True;

@MetaEntity(tableName = "t_department", defaultUpdatable = true)
@Getter
@EqualsAndHashCode(callSuper = false)
public class Department extends BaseEntity<Department> implements DictionaryItem {

    private static final long serialVersionUID = 1L;
    private static EntityHelper<Department> entityHelper = new EntityHelper<Department>(Department.class).init();

    @Meta(Type.ID)
    private String departmentId;

    @Meta(value = Type.String, label = "部门名称", nullable = False, searchable = True)
    private String departmentName;

    @Meta(value = Type.String, label = "部门编码", searchable = True, listable = True)
    private String departmentCode;

    @Meta(value = Type.Dictionary, label = "部门类型", refData = "DepartmentType")
    private String departmentType;
    
    @Meta(value = Type.String, label = "描述", listable = False)
    private String description;

    // @Meta(value = Type.String, label = "联系电话", listable = False)
    // private String contactPhone;

    // @Meta(value = Type.Enum, label = "是否启用")
    // private Boolean enabled;

    public void update(Department obj) {
        entityHelper.update(this, obj);
    }


    // DictionaryItem implementation
    @Override
    public String getType() {
        return this.departmentType;
    }

    @Override
    public Object getValue() {
        return departmentCode;
    }

    @Override
    public String getLabel() {
        return departmentName;
    }
}