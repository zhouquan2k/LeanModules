package io.leanddd.module.department.api;

import io.leanddd.module.department.model.Department;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/departments")
public interface DepartmentService {
    @GetMapping("/{departmentId}")
    Department getDepartment(@PathVariable("departmentId") String departmentId);

    @PostMapping
    void createDepartment(@RequestBody Department department);

    @PutMapping("/{departmentId}")
    void updateDepartment(@PathVariable("departmentId") String departmentId, @RequestBody Department department);

    @DeleteMapping("/{departmentId}")
    void deleteDepartment(@PathVariable("departmentId") String departmentId);
}