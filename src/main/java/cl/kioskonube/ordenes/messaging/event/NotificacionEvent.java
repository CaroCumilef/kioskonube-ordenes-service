package cl.kioskonube.ordenes.messaging.event;

import java.time.Instant;

/**
 * Mensaje para notificaciones-service. Sus campos coinciden con el formato que
 * espera ese servicio (id, destinatario, asunto, mensaje, origen, fechaCreacion).
 */
public record NotificacionEvent(
        String id,
        String destinatario,
        String asunto,
        String mensaje,
        String origen,
        Instant fechaCreacion
) {
}