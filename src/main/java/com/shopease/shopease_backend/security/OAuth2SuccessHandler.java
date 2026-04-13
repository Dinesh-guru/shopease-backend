package com.shopease.shopease_backend.security;

import com.shopease.shopease_backend.entity.Role;
import com.shopease.shopease_backend.entity.User;
import com.shopease.shopease_backend.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public OAuth2SuccessHandler(UserRepository userRepository,
                                 JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extract user info from Google
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Find existing user or create new one
        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            // First time Google login — create account automatically
        	user = new User();
        	user.setEmail(email);
        	user.setName(name);
        	user.setPassword("");        // empty string instead of null — safer for MySQL
        	user.setRole(Role.USER);
        	user.setProvider("google");
        	userRepository.save(user);
        }

        // Generate our JWT for this user
        String token = jwtTokenProvider.generateToken(user.getEmail());

        // Redirect to frontend with token as query param
        // Frontend will extract it and store in localStorage
        String redirectUrl = frontendUrl + "/oauth2/callback?token=" + token
                + "&name=" + user.getName()
                + "&email=" + user.getEmail()
                + "&role=" + user.getRole().name();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}