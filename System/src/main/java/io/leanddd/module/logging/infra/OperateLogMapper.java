package io.leanddd.module.logging.infra;

import io.leanddd.component.data.BaseMapper;
import io.leanddd.component.logging.api.OperateLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OperateLogMapper extends BaseMapper<OperateLog> {

    static final String selectByExample = "select * from t_operate_log a\n"
            + "         ${where} order by a.log_id desc";

    @Override
    @Select("select * from t_operate_log")
    @Results(id = "Example")
    List<OperateLog> queryAll();

    default List<OperateLog> queryByExample(Map<String, Object> example) {
        return this.queryByExample(OperateLog.class, example, selectByExample, Map.of());
    }
}
