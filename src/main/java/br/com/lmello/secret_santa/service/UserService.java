package br.com.lmello.secret_santa.service;

import br.com.lmello.secret_santa.model.User;
import br.com.lmello.secret_santa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getUser(String apiKey) {
        Optional<User> user = userRepository.findByApiKey(apiKey);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        return user.get();
    }
}
