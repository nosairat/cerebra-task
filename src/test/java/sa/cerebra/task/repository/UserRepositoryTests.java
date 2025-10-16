package sa.cerebra.task.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import sa.cerebra.task.entity.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByPhone_returnsPersistedUser() {
        User u = new User();
        u.setPhone("+1111");
        u = userRepository.save(u);

        var result = userRepository.findByPhone("+1111");
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(u.getId());
        assertThat(result.get().getPhone()).isEqualTo("+1111");
    }
}


