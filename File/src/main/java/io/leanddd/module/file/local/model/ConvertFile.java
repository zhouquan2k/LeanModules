package io.leanddd.module.file.local.model;

import io.leanddd.component.data.ConvertBase;
import org.mapstruct.Mapper;

import javax.inject.Named;

@Mapper
@Named
public interface ConvertFile extends ConvertBase<io.leanddd.module.file.api.File, File> {

}
