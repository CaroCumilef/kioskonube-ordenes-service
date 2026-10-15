package cl.kioskonube.ordenes.controller;

import cl.kioskonube.ordenes.dto.CrearOrdenRequest;
import cl.kioskonube.ordenes.dto.OrdenResponse;
import cl.kioskonube.ordenes.service.OrdenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/ordenes")
public class OrdenController {

    private final OrdenService service;

    public OrdenController(OrdenService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrdenResponse> crear(@Valid @RequestBody CrearOrdenRequest request) {
        OrdenResponse creada = service.crear(request, "API");
        return ResponseEntity.created(URI.create("/ordenes/" + creada.id())).body(creada);
    }

    @GetMapping("/{id}")
    public OrdenResponse obtener(@PathVariable @Positive(message = "El id debe ser positivo") Long id) {
        return service.obtener(id);
    }

    @GetMapping
    public List<OrdenResponse> listarPorCliente(
            @RequestParam
            @NotBlank(message = "El clienteEmail es obligatorio")
            @Email(message = "El clienteEmail no es válido")
            String clienteEmail) {
        return service.listarPorCliente(clienteEmail);
    }
}