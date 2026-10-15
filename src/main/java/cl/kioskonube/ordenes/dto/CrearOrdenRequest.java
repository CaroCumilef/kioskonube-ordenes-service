package cl.kioskonube.ordenes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Datos para crear una orden (vía REST o desde el checkout del carrito).
 */
public record CrearOrdenRequest(
        @NotBlank(message = "El email del cliente es obligatorio")
        @Email(message = "El email del cliente no es válido")
        String clienteEmail,

        @NotEmpty(message = "La orden debe tener al menos un item")
        List<@Valid ItemRequest> items
) {
}