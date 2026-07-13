package be.parcial.services;

import be.parcial.domain.entities.PeluqueroEntity;
import be.parcial.domain.entities.TratamientoEntity;
import be.parcial.domain.entities.TurnoEntity;
import be.parcial.domain.entities.UserEntity;
import be.parcial.services.implementations.EmailNotificationChannel;
import be.parcial.services.implementations.WhatsappNotificationChannel;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

class NotificationChannelTest {

    private TurnoEntity turno;

    @BeforeEach
    void setUp() {
        UserEntity cliente = new UserEntity();
        cliente.setName("Juan Perez");
        cliente.setEmail("juan@peluqueria.com");
        cliente.setTelefono("1100000000");

        UserEntity peluqueroUser = new UserEntity();
        peluqueroUser.setName("Marta Rodriguez");
        PeluqueroEntity peluquero =
                new PeluqueroEntity(1L, peluqueroUser, LocalTime.of(9, 0), LocalTime.of(18, 0), true);
        TratamientoEntity tratamiento =
                new TratamientoEntity(1L, "Corte", 30, new BigDecimal("4000.00"), true);

        turno = new TurnoEntity();
        turno.setCliente(cliente);
        turno.setPeluquero(peluquero);
        turno.setTratamiento(tratamiento);
        turno.setInicio(LocalDateTime.of(2026, 7, 14, 10, 0));
        turno.setConfirmToken("tok-123");
    }

    @Test
    @DisplayName("email deshabilitado solo loguea, no envía")
    void email_disabled_noSend() {
        JavaMailSender sender = mock(JavaMailSender.class);
        EmailNotificationChannel channel =
                new EmailNotificationChannel(sender, false, "http://localhost:4200", "no-reply@x.com");

        assertThatCode(() -> channel.enviar(turno)).doesNotThrowAnyException();
        verify(sender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("email habilitado envía HTML con botón de confirmación")
    void email_enabled_sends() {
        JavaMailSender sender = mock(JavaMailSender.class);
        when(sender.createMimeMessage()).thenReturn(new MimeMessage((jakarta.mail.Session) null));
        EmailNotificationChannel channel =
                new EmailNotificationChannel(sender, true, "http://localhost:4200", "no-reply@x.com");

        channel.enviar(turno);

        verify(sender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("email habilitado no propaga si el envío falla")
    void email_enabled_swallowsError() {
        JavaMailSender sender = mock(JavaMailSender.class);
        when(sender.createMimeMessage()).thenReturn(new MimeMessage((jakarta.mail.Session) null));
        doThrow(new org.springframework.mail.MailSendException("boom"))
                .when(sender).send(any(MimeMessage.class));
        EmailNotificationChannel channel =
                new EmailNotificationChannel(sender, true, "http://localhost:4200", "no-reply@x.com");

        assertThatCode(() -> channel.enviar(turno)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("whatsapp channel sends without error")
    void whatsapp_enviar_ok() {
        WhatsappNotificationChannel channel = new WhatsappNotificationChannel();
        assertThatCode(() -> channel.enviar(turno)).doesNotThrowAnyException();
    }
}
