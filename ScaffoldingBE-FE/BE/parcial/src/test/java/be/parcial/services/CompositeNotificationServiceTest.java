package be.parcial.services;

import be.parcial.domain.entities.TurnoEntity;
import be.parcial.services.implementations.CompositeNotificationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class CompositeNotificationServiceTest {

    @Test
    void notifyReserva_invokesEveryChannel() {
        NotificationChannel email = mock(NotificationChannel.class);
        NotificationChannel whatsapp = mock(NotificationChannel.class);
        CompositeNotificationService service =
                new CompositeNotificationService(List.of(email, whatsapp));
        TurnoEntity turno = new TurnoEntity();

        service.notifyReserva(turno);

        verify(email).enviar(turno);
        verify(whatsapp).enviar(turno);
    }
}
