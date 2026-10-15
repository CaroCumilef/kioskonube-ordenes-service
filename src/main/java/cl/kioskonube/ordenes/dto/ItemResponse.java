package cl.kioskonube.ordenes.dto;

import java.math.BigDecimal;

public record ItemResponse(
        Long productoId,
        String nombreProducto,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}