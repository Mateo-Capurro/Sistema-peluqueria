package be.parcial.domain.entities;

import be.parcial.exceptions.TransicionInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EstadoTurnoTest {

    @Nested
    @DisplayName("PENDIENTE")
    class Pendiente {

        @Test
        void confirmar_goesToConfirmado() {
            assertThat(EstadoTurno.PENDIENTE.confirmar()).isEqualTo(EstadoTurno.CONFIRMADO);
        }

        @Test
        void cancelar_goesToCancelado() {
            assertThat(EstadoTurno.PENDIENTE.cancelar()).isEqualTo(EstadoTurno.CANCELADO);
        }

        @Test
        void completar_throws() {
            assertThatThrownBy(EstadoTurno.PENDIENTE::completar)
                    .isInstanceOf(TransicionInvalidaException.class)
                    .hasMessageContaining("completar")
                    .hasMessageContaining("PENDIENTE");
        }
    }

    @Nested
    @DisplayName("CONFIRMADO")
    class Confirmado {

        @Test
        void completar_goesToCompletado() {
            assertThat(EstadoTurno.CONFIRMADO.completar()).isEqualTo(EstadoTurno.COMPLETADO);
        }

        @Test
        void cancelar_goesToCancelado() {
            assertThat(EstadoTurno.CONFIRMADO.cancelar()).isEqualTo(EstadoTurno.CANCELADO);
        }

        @Test
        void confirmar_throws() {
            assertThatThrownBy(EstadoTurno.CONFIRMADO::confirmar)
                    .isInstanceOf(TransicionInvalidaException.class);
        }
    }

    @Nested
    @DisplayName("terminal states reject all transitions")
    class Terminal {

        @Test
        void cancelado_rejectsAll() {
            assertThatThrownBy(EstadoTurno.CANCELADO::confirmar)
                    .isInstanceOf(TransicionInvalidaException.class);
            assertThatThrownBy(EstadoTurno.CANCELADO::cancelar)
                    .isInstanceOf(TransicionInvalidaException.class);
            assertThatThrownBy(EstadoTurno.CANCELADO::completar)
                    .isInstanceOf(TransicionInvalidaException.class);
        }

        @Test
        void completado_rejectsAll() {
            assertThatThrownBy(EstadoTurno.COMPLETADO::confirmar)
                    .isInstanceOf(TransicionInvalidaException.class);
            assertThatThrownBy(EstadoTurno.COMPLETADO::cancelar)
                    .isInstanceOf(TransicionInvalidaException.class);
            assertThatThrownBy(EstadoTurno.COMPLETADO::completar)
                    .isInstanceOf(TransicionInvalidaException.class);
        }
    }
}
