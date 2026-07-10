package be.parcial.services;

import be.parcial.domain.entities.TurnoEntity;

/**
 * A single delivery channel (email, WhatsApp, ...). Stub implementations log the
 * message; real providers can be plugged in without touching the callers.
 */
public interface NotificationChannel {
    void enviar(TurnoEntity turno);
}
