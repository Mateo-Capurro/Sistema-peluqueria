package be.parcial.security;

import be.parcial.domain.entities.UserEntity;
import be.parcial.exceptions.ResourceNotFoundException;
import be.parcial.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        userEntity.setPassword("encodedPassword");
        userEntity.setName("Test User");
        userEntity.setRole(UserEntity.Role.CLIENTE);
    }

    @Test
    @DisplayName("should load user by username")
    void loadUserByUsername_existingUsername_returnsUserDetails() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));

        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_CLIENTE");
    }

    @Test
    @DisplayName("should throw exception when user not found")
    void loadUserByUsername_nonExistingUsername_throwsException() {
        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("notfound"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with username: notfound");
    }

    @Test
    @DisplayName("should load admin user with correct role")
    void loadUserByUsername_adminUsername_returnsAdminRole() {
        userEntity.setRole(UserEntity.Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(userEntity));

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }
}
