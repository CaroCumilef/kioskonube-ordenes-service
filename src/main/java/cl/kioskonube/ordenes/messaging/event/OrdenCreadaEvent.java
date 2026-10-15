package cl.kioskonube.ordenes.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Evento publicado en ordenes.exchange (orden.creada).
 * Lo consumen despachos-service (crear despacho) y productos-service (descontar stock).
 */
public record OrdenCreadaEvent(
        Long ordenId,
        String clienteEmail,
        BigDecimal total,
        List<Item> items,
        Instant fecha
) {
    public record Item(Long productoId, int cantidad) {
    }
}