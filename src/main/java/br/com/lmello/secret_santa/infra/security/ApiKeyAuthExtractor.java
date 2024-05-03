package br.com.lmello.secret_santa.infra.security;

import br.com.lmello.secret_santa.model.User;
import br.com.lmello.secret_santa.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthExtractor {
    private final UserService userService;

    public Optional<Authentication> extract(HttpServletRequest request) {
        String providedKey = request.getHeader("X-API-KEY");

        try {
            User user = userService.getUser(providedKey);

            if (providedKey == null || !providedKey.equals(user.getKey())) {
                return Optional.empty();
            }

            userService.increaseUsage(providedKey);

            return Optional.of(new ApiKeyAuth(providedKey, AuthorityUtils.NO_AUTHORITIES));
        } catch (UsernameNotFoundException e) {
            return Optional.empty();
        }
    }
}
