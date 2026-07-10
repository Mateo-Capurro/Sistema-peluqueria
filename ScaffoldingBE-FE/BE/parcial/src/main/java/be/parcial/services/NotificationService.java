package be.parcial.services;

import be.parcial.domain.entities.TurnoEntity;

public interface NotificationService {
    void notifyReserva(TurnoEntity turno);
}
