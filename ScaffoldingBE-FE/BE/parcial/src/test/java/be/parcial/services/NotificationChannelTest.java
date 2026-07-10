package be.parcial.services;

import be.parcial.domain.entities.TurnoEntity;
import be.parcial.domain.entities.UserEntity;
import be.parcial.services.implementations.EmailNotificationChannel;
import be.parcial.services.implementations.WhatsappNotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;

class NotificationChannelTest {

    private TurnoEntity turno;

    @BeforeEach
    void setUp() {
        UserEntity cliente = new UserEntity();
        cliente.setName("Juan Perez");
        cliente.setEmail("juan@peluqueria.com");
        cliente.setTelefono("1100000000");

        turno = new TurnoEntity();
        turno.setCliente(cliente);
        turno.setInicio(LocalDateTime.of(2026, 7, 14, 10, 0));
    }

    @Test
    @DisplayName("email channel sends without error")
    void email_enviar_ok() {
        EmailNotificationChannel channel = new EmailNotificationChannel();
        assertThatCode(() -> channel.enviar(turno)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("whatsapp channel sends without error")
    void whatsapp_enviar_ok() {
        WhatsappNotificationChannel channel = new WhatsappNotificationChannel();
        assertThatCode(() -> channel.enviar(turno)).doesNotThrowAnyException();
    }
}
