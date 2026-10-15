package cl.kioskonube.ordenes.exception;

/**
 * Error al procesar un mensaje que NO se arregla reintentando
 * (JSON ilegible, datos inválidos, orden inexistente). El mensaje va directo a la DLQ.
 */
public class ErrorDefinitivoException extends RuntimeException {

    public ErrorDefinitivoException(String message, Throwable cause) {
        super(message, cause);
    }
}