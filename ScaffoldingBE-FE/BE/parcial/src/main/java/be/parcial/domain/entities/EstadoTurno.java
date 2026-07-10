package be.parcial.domain.entities;

import be.parcial.exceptions.TransicionInvalidaException;

/**
 * State pattern implemented as an enum: each estado defines which transitions
 * are valid. Invalid transitions throw {@link TransicionInvalidaException}.
 *
 * <pre>
 * PENDIENTE  -> CONFIRMADO | CANCELADO
 * CONFIRMADO -> COMPLETADO | CANCELADO
 * CANCELADO  -> (terminal)
 * COMPLETADO -> (terminal)
 * </pre>
 */
public enum EstadoTurno {

    PENDIENTE {
        @Override
        public EstadoTurno confirmar() {
            return CONFIRMADO;
        }

        @Override
        public EstadoTurno cancelar() {
            return CANCELADO;
        }
    },

    CONFIRMADO {
        @Override
        public EstadoTurno cancelar() {
            return CANCELADO;
        }

        @Override
        public EstadoTurno completar() {
            return COMPLETADO;
        }
    },

    CANCELADO,

    COMPLETADO;

    public EstadoTurno confirmar() {
        throw new TransicionInvalidaException(name(), "confirmar");
    }

    public EstadoTurno cancelar() {
        throw new TransicionInvalidaException(name(), "cancelar");
    }

    public EstadoTurno completar() {
        throw new TransicionInvalidaException(name(), "completar");
    }
}
