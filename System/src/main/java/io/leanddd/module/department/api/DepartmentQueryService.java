package io.leanddd.module.department.api;

import io.leanddd.module.department.model.Department;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


@RequestMapping("/api/public/departments")
public interface DepartmentQueryService {
    @GetMapping
    List<Department> getDepartments(@RequestParam Map<String, Object> params);
}
