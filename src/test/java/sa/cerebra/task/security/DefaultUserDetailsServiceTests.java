package sa.cerebra.task.security;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DefaultUserDetailsServiceTests {

    @Test
    void createsUserWhenMissing_andReturnsIt() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.findByPhone("+1000")).thenReturn(Optional.empty());
        when(repo.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        DefaultUserDetailsService service = new DefaultUserDetailsService(repo);

        var details = service.loadUserByUsername("+1000");
        assertThat(details).isInstanceOf(User.class);
        assertThat(((User) details).getId()).isEqualTo(1L);
        assertThat(details.getUsername()).isEqualTo("+1000");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getPhone()).isEqualTo("+1000");
    }

    @Test
    void returnsExistingUser_whenFound() {
        UserRepository repo = mock(UserRepository.class);
        User existing = new User();
        existing.setId(42L);
        existing.setPhone("+2000");
        when(repo.findByPhone("+2000")).thenReturn(Optional.of(existing));

        DefaultUserDetailsService service = new DefaultUserDetailsService(repo);

        var details = service.loadUserByUsername("+2000");
        assertThat(details).isSameAs(existing);
        verify(repo, never()).save(any());
    }
}


