package be.parcial.services.implementations;

import be.parcial.domain.entities.TurnoEntity;
import be.parcial.domain.entities.UserEntity;
import be.parcial.services.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stub email channel: logs the confirmation notice. Swap for a real SMTP/SendGrid
 * client later without changing the callers.
 */
@Component
@Slf4j
public class EmailNotificationChannel implements NotificationChannel {

    @Override
    public void enviar(TurnoEntity turno) {
        UserEntity cliente = turno.getCliente();
        log.info("[EMAIL] Para {} <{}>: turno reservado para el {}. Confirma tu turno.",
                cliente.getName(), cliente.getEmail(), turno.getInicio());
    }
}
