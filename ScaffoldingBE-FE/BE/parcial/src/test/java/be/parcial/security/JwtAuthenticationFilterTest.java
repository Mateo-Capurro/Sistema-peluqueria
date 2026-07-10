package be.parcial.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();

        userDetails = new User(
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE")));
    }

    @Test
    @DisplayName("should pass through when no Authorization header")
    void doFilterInternal_noAuthHeader_passesThrough() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("should pass through when Authorization header does not start with Bearer")
    void doFilterInternal_nonBearerHeader_passesThrough() throws ServletException, IOException {
        request.addHeader("Authorization", "Basic some-token");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("should set authentication when token is valid")
    void doFilterInternal_validToken_setsAuthentication() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer valid-jwt-token");
        when(jwtService.extractUsername("valid-jwt-token")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid-jwt-token", userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(userDetails);
    }

    @Test
    @DisplayName("should not set authentication when username is null")
    void doFilterInternal_nullEmail_doesNotSetAuthentication() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer some-jwt");
        when(jwtService.extractUsername("some-jwt")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("should not set authentication when token is invalid")
    void doFilterInternal_invalidToken_doesNotSetAuthentication() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer invalid-jwt");
        when(jwtService.extractUsername("invalid-jwt")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.isTokenValid("invalid-jwt", userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("should not re-authenticate when already authenticated")
    void doFilterInternal_alreadyAuthenticated_doesNotReauthenticate() throws ServletException, IOException {
        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken("existing", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        request.addHeader("Authorization", "Bearer some-jwt");
        when(jwtService.extractUsername("some-jwt")).thenReturn("testuser");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);
    }
}
