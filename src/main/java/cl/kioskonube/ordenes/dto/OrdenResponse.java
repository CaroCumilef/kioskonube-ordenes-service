package cl.kioskonube.ordenes.dto;

import cl.kioskonube.ordenes.domain.Orden;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrdenResponse(
        Long id,
        String clienteEmail,
        String estado,
        String origen,
        BigDecimal total,
        Instant fechaCreacion,
        Instant fechaActualizacion,
        List<ItemResponse> items
) {

    public static OrdenResponse desde(Orden orden) {
        List<ItemResponse> items = orden.getItems().stream()
                .map(i -> new ItemResponse(i.getProductoId(), i.getNombreProducto(),
                        i.getCantidad(), i.getPrecioUnitario(), i.subtotal()))
                .toList();
        return new OrdenResponse(orden.getId(), orden.getClienteEmail(), orden.getEstado().name(),
                orden.getOrigen(), orden.getTotal(), orden.getFechaCreacion(),
                orden.getFechaActualizacion(), items);
    }
}