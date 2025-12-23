package io.leanddd.module.user.infra;

import io.leanddd.component.data.BaseMapper;
import io.leanddd.module.user.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    static final String selectByExample = "with user as (\n" //
            + "select distinct a.user_id\n" //
            + "from t_user a\n" //
            + "left join t_user_role roles on a.user_id = roles.user_id  \n"
            + "left join t_role role on role.role_id = roles.role_id \n" //
            + "${where}\n" //
            + ")\n" //
            + "select *\n" //
            + "from t_user a\n" + "inner join user u on a.user_id = u.user_id \n"
            + "left join t_user_role roles on a.user_id = roles.user_id  \n"
            + "left join t_role role on role.role_id = roles.role_id\n" //
            + "${where}\n" //
            + "order by a.username";


    @ResultMap("Example")
    default List<User> queryByExample(@Param("example") Map<String, Object> example) {
        return queryByExample(User.class, example, selectByExample, Map.of());
    }

    @Override
    List<User> queryAll();

    User getUserByLoginName(String username);

    User getUser(String userId);

    List<User> queryUsersByOrg(String orgId);

    List<User> queryUsersByDepartment(String departmentId);


}
