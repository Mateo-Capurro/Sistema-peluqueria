package be.parcial.controllers;

import be.parcial.dtos.*;
import be.parcial.domain.entities.RefreshTokenEntity;
import be.parcial.domain.entities.UserEntity;
import be.parcial.security.JwtService;
import be.parcial.services.RefreshTokenService;
import be.parcial.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "JWT authentication endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        UserEntity user = userService.registerUser(request, UserEntity.Role.CLIENTE);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                java.util.Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name()))
        );

        String accessToken = jwtService.generateAccessToken(userDetails);
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user);

        AuthResponseDTO response = new AuthResponseDTO(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                user.getUsername(),
                user.getRole().name()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/authenticate")
    @Operation(summary = "Authenticate a user")
    public ResponseEntity<AuthResponseDTO> authenticate(@Valid @RequestBody AuthRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserEntity user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String accessToken = jwtService.generateAccessToken(userDetails);
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user);

        AuthResponseDTO response = new AuthResponseDTO(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                user.getUsername(),
                user.getRole().name()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthResponseDTO> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO request) {
        RefreshTokenEntity refreshToken = refreshTokenService.validateRefreshToken(
                request.getRefreshToken());

        UserEntity user = refreshToken.getUser();
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                java.util.Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name()))
        );

        refreshTokenService.revokeRefreshToken(request.getRefreshToken());

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        RefreshTokenEntity newRefreshToken = refreshTokenService.createRefreshToken(user);

        AuthResponseDTO response = new AuthResponseDTO(
                newAccessToken,
                newRefreshToken.getToken(),
                "Bearer",
                user.getUsername(),
                user.getRole().name()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequestDTO request) {
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }
}
