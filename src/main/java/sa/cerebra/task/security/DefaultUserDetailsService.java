package sa.cerebra.task.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultUserDetailsService implements UserDetailsService {
    final  UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByPhone(username);

        if (user.isEmpty()) {
            var newUser = new User();
            newUser.setPhone(username);
            newUser = userRepository.save(newUser);
            user= Optional.of(newUser);
        }
        return user.get();

    }
}
