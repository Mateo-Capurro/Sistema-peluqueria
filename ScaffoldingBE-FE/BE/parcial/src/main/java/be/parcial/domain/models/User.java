package be.parcial.domain.models;

import be.parcial.domain.entities.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String email;
    private String telefono;
    private String dni;
    private UserEntity.Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
