package com.recoverease;

import com.recoverease.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

/**
 * Shared helper that injects a {@link User} as the authenticated principal
 * in MockMvc tests.  Spring Security's {@code .user(UserDetails)} processor
 * requires {@code UserDetails}, which our {@code User} entity does not implement.
 * We therefore build a full {@code Authentication} object directly.
 */
public class TestSecurityUtils {

    private TestSecurityUtils() {}

    /**
     * Returns a {@link RequestPostProcessor} that sets the given {@link User}
     * as the authenticated principal for a MockMvc request.
     */
    public static RequestPostProcessor asUser(User user) {
        var auth = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
        return SecurityMockMvcRequestPostProcessors.authentication(auth);
    }
}
