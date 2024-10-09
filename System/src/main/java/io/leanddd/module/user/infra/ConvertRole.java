package io.leanddd.module.user.infra;

import io.leanddd.component.data.ConvertBase;
import io.leanddd.module.user.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConvertRole extends ConvertBase<io.leanddd.module.user.api.Role, Role> {

}


