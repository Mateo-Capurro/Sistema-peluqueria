package be.parcial.services.implementations;

import be.parcial.domain.entities.TurnoEntity;
import be.parcial.services.NotificationChannel;
import be.parcial.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Composite that fans a reservation notice out to every registered
 * {@link NotificationChannel}. Spring injects all channel beans (Registry via DI),
 * so adding a channel needs no change here.
 */
@Service
@RequiredArgsConstructor
public class CompositeNotificationService implements NotificationService {

    private final List<NotificationChannel> channels;

    @Override
    public void notifyReserva(TurnoEntity turno) {
        channels.forEach(channel -> channel.enviar(turno));
    }
}
