package io.leanddd.module.user.model;

import io.leanddd.component.framework.Context;
import io.leanddd.component.meta.Service;
import io.leanddd.component.meta.Service.Type;
import io.leanddd.module.user.api.UserQueryService;
import io.leanddd.module.user.infra.ConvertUser;
import io.leanddd.module.user.infra.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Service(type = Type.Query)
@Named
@RequiredArgsConstructor
class UserQueryServiceImpl implements UserQueryService, UserDetailsService {

    private final ConvertUser convert;
    private final UserMapper userMapper;

    @Override
    public List<io.leanddd.module.user.api.User> queryByExample(Map<String, Object> example) {
        return convert.doToVo(userMapper.queryByExample(example));
    }

    @Override
    public List<io.leanddd.module.user.api.User> queryUsers() {
        return convert.doToVo(userMapper.queryAll());
    }

    @Override
    public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException {
        var user = userMapper.getUserByLoginName(loginName);
        if (user == null)
            throw new UsernameNotFoundException("no user: " + loginName);
        return user;
    }

    public io.leanddd.module.user.api.User getUserByUsername(String loginName) {
        return convert.doToVo(userMapper.getUserByLoginName(loginName));
    }

    @Override
    public List<io.leanddd.module.user.api.User> queryUsersByOrg(String orgId) {
        return convert.doToVo(userMapper.queryUsersByOrg(orgId));
    }

    @Override
    public io.leanddd.module.user.api.User getMyProfile() {
        var userId = Context.getUserId();
        return convert.doToVo(userMapper.getUser(userId));
    }

}



