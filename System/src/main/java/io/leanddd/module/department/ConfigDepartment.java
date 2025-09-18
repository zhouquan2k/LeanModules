package io.leanddd.module.department;

import io.leanddd.component.data.RepositoryImpl;
import io.leanddd.component.data.impl.DictionaryProvider;
import io.leanddd.component.framework.Repository;
import io.leanddd.module.department.infra.DepartmentMapper;
import io.leanddd.module.department.model.Department;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.repository.CrudRepository;

@Configuration
@DependsOn("Init")
public class ConfigDepartment {
    @Bean
    public Repository<Department> myDepartmentRepository(DepartmentRepository springRepo) {
        return new RepositoryImpl<Department>(Department.class, springRepo);
    }

    @Bean
    public DictionaryProvider<Department> departmentDictionaryProvider(DepartmentMapper mapper) {
        return new DictionaryProvider<>(mapper);
    }
}

interface DepartmentRepository extends CrudRepository<Department, String> {
}