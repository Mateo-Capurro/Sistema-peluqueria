package be.parcial.exceptions;

public final class Messages {

    private Messages() {
    }

    public static final String USER_NOT_FOUND = "User not found with username: %s";
    public static final String USER_ALREADY_EXISTS = "User already exists with username: %s";
    public static final String EMAIL_ALREADY_EXISTS = "Ya existe un usuario con el email: %s";
    public static final String DNI_ALREADY_EXISTS = "Ya existe un usuario con el dni: %s";
    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String REFRESH_TOKEN_NOT_FOUND = "Refresh token not found";
    public static final String REFRESH_TOKEN_EXPIRED = "Refresh token has expired";
    public static final String REFRESH_TOKEN_REVOKED = "Refresh token has been revoked";
    public static final String UNAUTHORIZED = "You are not authorized to access this resource";
    public static final String VALIDATION_ERROR = "Validation error";
    public static final String TRATAMIENTO_NOT_FOUND = "Tratamiento not found with id: %s";
    public static final String TRATAMIENTO_ALREADY_EXISTS = "Tratamiento already exists with nombre: %s";
    public static final String PELUQUERO_NOT_FOUND = "Peluquero not found with id: %s";
    public static final String INVALID_JORNADA = "HoraInicio must be before HoraFin";
    public static final String TRANSICION_INVALIDA = "Cannot apply action '%s' on turno in estado %s";
    public static final String TURNO_NOT_FOUND = "Turno not found with id: %s";
    public static final String SLOT_FECHA_PASADA = "No se puede reservar un turno en el pasado";
    public static final String SLOT_DIA_NO_LABORABLE = "La peluqueria no atiende los lunes";
    public static final String SLOT_FUERA_JORNADA = "El horario esta fuera de la jornada del peluquero";
    public static final String SLOT_OCUPADO = "El peluquero ya tiene un turno en ese horario";
    public static final String ACCESS_DENIED = "No tienes permiso para modificar este turno";
}
