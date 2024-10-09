package io.leanddd.module.user.infra;

import io.leanddd.component.data.ConvertBase;
import io.leanddd.module.user.model.User;
import io.leanddd.module.user.model.User.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Set;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConvertUser extends ConvertBase<io.leanddd.module.user.api.User, User> {

    UserRole voToDo(io.leanddd.module.user.api.User.UserRole vo);

    Set<UserRole> voToDo(Set<io.leanddd.module.user.api.User.UserRole> vos);

    @Mapping(target = "roleName", source = "role.roleName")
    @Mapping(target = "role", source = "role")
    io.leanddd.module.user.api.User.UserRole doToVo(UserRole Do);
}
