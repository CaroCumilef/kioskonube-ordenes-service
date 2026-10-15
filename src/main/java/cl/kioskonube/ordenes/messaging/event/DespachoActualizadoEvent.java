package cl.kioskonube.ordenes.messaging.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Evento que llega desde despachos-service (despacho.actualizado).
 */
public record DespachoActualizadoEvent(
        @NotNull Long ordenId,
        @NotNull Long despachoId,
        @NotBlank String estado,
        Instant fecha
) {
}