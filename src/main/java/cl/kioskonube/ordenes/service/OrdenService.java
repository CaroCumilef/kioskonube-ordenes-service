package cl.kioskonube.ordenes.service;

import cl.kioskonube.ordenes.domain.EstadoOrden;
import cl.kioskonube.ordenes.domain.Orden;
import cl.kioskonube.ordenes.domain.OrdenItem;
import cl.kioskonube.ordenes.dto.CrearOrdenRequest;
import cl.kioskonube.ordenes.dto.OrdenResponse;
import cl.kioskonube.ordenes.exception.OrdenNoEncontradaException;
import cl.kioskonube.ordenes.messaging.producer.OrdenEventPublisher;
import cl.kioskonube.ordenes.repository.OrdenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio de órdenes. No conoce RabbitMQ: delega en OrdenEventPublisher.
 */
@Service
public class OrdenService {

    private static final Logger log = LoggerFactory.getLogger(OrdenService.class);

    private final OrdenRepository repository;
    private final OrdenEventPublisher publisher;

    public OrdenService(OrdenRepository repository, OrdenEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional
    public OrdenResponse crear(CrearOrdenRequest request, String origen) {
        Orden orden = new Orden(request.clienteEmail().trim().toLowerCase(), origen);
        request.items().forEach(i -> orden.agregarItem(
                new OrdenItem(i.productoId(), i.nombreProducto().trim(), i.cantidad(), i.precioUnitario())));

        Orden guardada = repository.save(orden);
        log.info("Orden creada id={} origen={} total={}", guardada.getId(), origen, guardada.getTotal());

        publisher.publicarOrdenCreada(guardada);
        publisher.notificar(guardada.getClienteEmail(),
                "KioskoNube: orden #" + guardada.getId() + " recibida",
                "Hola, recibimos tu orden #" + guardada.getId() + " por un total de $"
                        + guardada.getTotal() + ". Te avisaremos cuando sea despachada.");

        return OrdenResponse.desde(guardada);
    }

    @Transactional(readOnly = true)
    public OrdenResponse obtener(Long id) {
        return repository.findById(id)
                .map(OrdenResponse::desde)
                .orElseThrow(() -> new OrdenNoEncontradaException(id));
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarPorCliente(String clienteEmail) {
        return repository.findByClienteEmailOrderByFechaCreacionDesc(clienteEmail.trim().toLowerCase())
                .stream()
                .map(OrdenResponse::desde)
                .toList();
    }

    /**
     * Aplica un cambio de estado informado por despachos-service.
     * Es idempotente: si la orden ya tiene ese estado, no hace nada.
     */
    @Transactional
    public void aplicarEstadoDespacho(Long ordenId, String estadoDespacho) {
        EstadoOrden nuevoEstado = traducirEstado(estadoDespacho);
        Orden orden = repository.findById(ordenId)
                .orElseThrow(() -> new OrdenNoEncontradaException(ordenId));

        if (orden.getEstado() == nuevoEstado) {
            log.info("Orden id={} ya estaba en estado {}; se ignora", ordenId, nuevoEstado);
            return;
        }

        orden.cambiarEstado(nuevoEstado);
        log.info("Orden id={} cambió a estado {}", ordenId, nuevoEstado);

        publisher.notificar(orden.getClienteEmail(),
                "KioskoNube: tu orden #" + ordenId + " está " + nuevoEstado.name().replace('_', ' ').toLowerCase(),
                "Hola, tu orden #" + ordenId + " cambió al estado " + nuevoEstado.name() + ".");
    }

    private EstadoOrden traducirEstado(String estadoDespacho) {
        if (estadoDespacho == null) {
            throw new IllegalArgumentException("Estado de despacho vacío");
        }
        return switch (estadoDespacho.trim().toUpperCase()) {
            case "EN_PREPARACION" -> EstadoOrden.EN_PREPARACION;
            case "DESPACHADO" -> EstadoOrden.DESPACHADA;
            case "ENTREGADO" -> EstadoOrden.ENTREGADA;
            case "CANCELADO" -> EstadoOrden.CANCELADA;
            default -> throw new IllegalArgumentException("Estado de despacho desconocido: " + estadoDespacho);
        };
    }
}