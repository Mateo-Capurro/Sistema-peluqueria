package be.parcial.services.implementations;

import be.parcial.domain.entities.TurnoEntity;
import be.parcial.domain.entities.UserEntity;
import be.parcial.services.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stub WhatsApp channel: logs the confirmation notice. Swap for a real Twilio/
 * WhatsApp Business client later without changing the callers.
 */
@Component
@Slf4j
public class WhatsappNotificationChannel implements NotificationChannel {

    @Override
    public void enviar(TurnoEntity turno) {
        UserEntity cliente = turno.getCliente();
        log.info("[WHATSAPP] Para {} ({}): turno reservado para el {}. Confirma tu turno.",
                cliente.getName(), cliente.getTelefono(), turno.getInicio());
    }
}
