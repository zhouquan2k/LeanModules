package io.leanddd.module.misc.infra;

import io.leanddd.component.data.BaseMapper;
import io.leanddd.module.misc.model.DictionaryImpl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DictionaryMapper extends BaseMapper<DictionaryImpl> {

    @Override
    @Select("select * from t_dictionary")
    @Results(id = "Example")
    List<DictionaryImpl> queryAll();
}
