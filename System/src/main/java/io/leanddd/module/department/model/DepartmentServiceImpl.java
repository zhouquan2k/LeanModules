package io.leanddd.module.department.model;

import io.leanddd.component.framework.Repository;
import io.leanddd.component.meta.Service;
import io.leanddd.module.department.api.DepartmentService;
import io.leanddd.module.department.infra.DepartmentMapper;
import lombok.RequiredArgsConstructor;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Named
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final Repository<Department> departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public List<Department> getDepartments(Map<String, Object> params) {
        return departmentMapper.queryByExample(params);
    }

    @Override
    public Department getDepartment(String departmentId) {
        return departmentRepository.get(departmentId).orElseThrow();
    }

    @Override
    public void createDepartment(Department department) {
        departmentRepository.save(department);
    }

    @Override
    public void updateDepartment(String departmentId, Department department) {
        Department existingDepartment = getDepartment(departmentId);
        existingDepartment.update(department);
        departmentRepository.save(existingDepartment);
    }

    @Override
    public void deleteDepartment(String departmentId) {
        departmentRepository.remove(departmentId);
    }
}