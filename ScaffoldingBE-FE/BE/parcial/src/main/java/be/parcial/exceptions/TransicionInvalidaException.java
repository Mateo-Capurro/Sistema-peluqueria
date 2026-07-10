package be.parcial.exceptions;

public class TransicionInvalidaException extends RuntimeException {

    public TransicionInvalidaException(String estado, String accion) {
        super(String.format(Messages.TRANSICION_INVALIDA, accion, estado));
    }
}
