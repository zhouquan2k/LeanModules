package io.leanddd.module.department.model;

import io.leanddd.component.meta.Query;
import io.leanddd.component.meta.Service;
import io.leanddd.module.department.api.DepartmentQueryService;
import io.leanddd.module.department.infra.DepartmentMapper;
import lombok.RequiredArgsConstructor;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Named
@Service
@RequiredArgsConstructor
public class DepartmentQueryServiceImpl implements DepartmentQueryService {
    private final DepartmentMapper departmentMapper;

    @Override
    @Query()
    public List<Department> getDepartments(Map<String, Object> params) {
        return departmentMapper.queryByExample(params);
    }


}
