package be.parcial.services;

import be.parcial.dtos.RegisterRequestDTO;
import be.parcial.domain.entities.UserEntity;
import be.parcial.domain.models.User;
import be.parcial.exceptions.Messages;
import be.parcial.repositories.UserRepository;
import be.parcial.services.implementations.UserServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplementationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceImplementation userService;

    private UserEntity userEntity;
    private User userModel;
    private RegisterRequestDTO registerRequest;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        userEntity.setPassword("encodedPassword");
        userEntity.setName("Test User");
        userEntity.setRole(UserEntity.Role.CLIENTE);

        userModel = new User();
        userModel.setUsername("testuser");
        userModel.setPassword("password123");
        userModel.setName("Test User");
        userModel.setRole(UserEntity.Role.CLIENTE);

        registerRequest = new RegisterRequestDTO("testuser", "password123", "Test User",
                "test@peluqueria.com", "1100000000", "40000000");
    }

    @Nested
    @DisplayName("registerUser")
    class RegisterUser {

        @Test
        @DisplayName("should register user successfully")
        void registerUser_validData_returnsUser() {
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(modelMapper.map(registerRequest, User.class)).thenReturn(userModel);
            when(modelMapper.map(userModel, UserEntity.class)).thenReturn(userEntity);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

            UserEntity result = userService.registerUser(registerRequest, UserEntity.Role.CLIENTE);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should throw when username already exists")
        void registerUser_existingEmail_throwsException() {
            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            assertThatThrownBy(() -> userService.registerUser(registerRequest, UserEntity.Role.CLIENTE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User already exists with username:");
        }
    }

    @Nested
    @DisplayName("findByUsername")
    class FindByUsername {

        @Test
        @DisplayName("should return user when found")
        void findByUsername_existingUsername_returnsUser() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));

            Optional<UserEntity> result = userService.findByUsername("testuser");

            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should return empty when not found")
        void findByUsername_nonExistingUsername_returnsEmpty() {
            when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());

            Optional<UserEntity> result = userService.findByUsername("notfound");

            assertThat(result).isEmpty();
        }
    }
}
