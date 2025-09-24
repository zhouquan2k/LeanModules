package io.leanddd.module.department.infra;

import io.leanddd.component.data.BaseMapper;
import io.leanddd.module.department.model.Department;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    static final String selectByExample = "select * from t_department a\n"
            + "${where}\n" //
            + " order by a.department_name asc";

    default List<Department> queryByExample(Map<String, Object> example) {
        return this.queryByExample(Department.class, example, selectByExample, Map.of());
    }

    @Override
    List<Department> queryAll();
}