package io.leanddd.module.user.infra;

import io.leanddd.component.data.BaseMapper;
import io.leanddd.module.user.model.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    @Select("select * from t_role where role_id=#{roleId}")
    Role getById(String roleId);

    Role getRoleWithPermissions(String roleId);

    @Select("select * from t_role where org_id = #{orgId} or role_type = 'GroupPublic'")
    List<Role> queryByOrgId(@Param("orgId") String orgId);

    @Override
    @Select("select * from t_role")
    @Results(id = "Example")
    List<Role> queryAll();

    @Select("select * from t_role where role_type=#{roleType}")
    List<Role> queryByType(String roleType);
}
