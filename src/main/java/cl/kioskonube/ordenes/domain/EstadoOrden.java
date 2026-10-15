package cl.kioskonube.ordenes.domain;

/**
 * Ciclo de vida de una orden.
 */
public enum EstadoOrden {
    CREADA,
    EN_PREPARACION,
    DESPACHADA,
    ENTREGADA,
    CANCELADA
}