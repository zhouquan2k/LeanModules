package io.leanddd.module.user;

import io.leanddd.component.data.RepositoryImpl;
import io.leanddd.component.data.impl.DictionaryProvider;
import io.leanddd.component.framework.Repository;
import io.leanddd.module.user.infra.ConvertRole;
import io.leanddd.module.user.infra.ConvertUser;
import io.leanddd.module.user.infra.RoleMapper;
import io.leanddd.module.user.infra.UserMapper;
import io.leanddd.module.user.model.PassEncoder;
import io.leanddd.module.user.model.Role;
import io.leanddd.module.user.model.User;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.crypto.password.PasswordEncoder;


// spring data need to have a standalone repository class
interface UserRepository extends CrudRepository<User, String> {
}

interface RoleRepository extends CrudRepository<Role, String> {
}

@Configuration
@DependsOn("Init")
public class ConfigUser {

    @Bean
    public Repository<User> myUserRepository(UserRepository springRepo) {
        return new RepositoryImpl<User>(User.class, springRepo);
    }

    @Bean
    public Repository<Role> myRoleRepository(RoleRepository springRepo) {
        return new RepositoryImpl<Role>(Role.class, springRepo);
    }

    @Bean
    ConvertUser convertUser() {
        return Mappers.getMapper(ConvertUser.class);
    }

    @Bean
    ConvertRole convertRole() {
        return Mappers.getMapper(ConvertRole.class);
    }

    @Bean
    PassEncoder passEncoder(PasswordEncoder passEncoder) {
        return new PassEncoder() {
            @Override
            public String encode(String raw) {
                return passEncoder.encode(raw);
            }

            @Override
            public boolean matches(String raw, String encoded) {
                return passEncoder.matches(raw, encoded);
            }
        };
    }

    @Bean
    public DictionaryProvider<User> userDictioanyProvider(UserMapper mapper) {
        return new DictionaryProvider<>(mapper);
    }

    @Bean
    public DictionaryProvider<Role> roleDictioanyProvider(RoleMapper mapper) {
        return new DictionaryProvider<>(mapper);
    }

}

