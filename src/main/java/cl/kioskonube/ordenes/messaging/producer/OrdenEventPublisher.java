package cl.kioskonube.ordenes.messaging.producer;

import cl.kioskonube.ordenes.config.MensajeriaProperties;
import cl.kioskonube.ordenes.domain.Orden;
import cl.kioskonube.ordenes.exception.PublicacionException;
import cl.kioskonube.ordenes.messaging.event.NotificacionEvent;
import cl.kioskonube.ordenes.messaging.event.OrdenCreadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Productor del ordenes-service. Única clase que conoce RabbitTemplate.
 */
@Component
public class OrdenEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrdenEventPublisher.class);
    private static final String ORIGEN = "ordenes-service";

    private final RabbitTemplate rabbitTemplate;
    private final MensajeriaProperties props;

    public OrdenEventPublisher(RabbitTemplate rabbitTemplate, MensajeriaProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    /** Evento crítico: si falla, se lanza excepción y la orden NO se guarda (rollback). */
    public void publicarOrdenCreada(Orden orden) {
        OrdenCreadaEvent evento = new OrdenCreadaEvent(
                orden.getId(),
                orden.getClienteEmail(),
                orden.getTotal(),
                orden.getItems().stream()
                        .map(i -> new OrdenCreadaEvent.Item(i.getProductoId(), i.getCantidad()))
                        .toList(),
                Instant.now());
        try {
            rabbitTemplate.convertAndSend(props.ordenes().exchange(), props.ordenes().routingKey(), evento);
            log.info("Evento orden.creada publicado ordenId={} total={}", orden.getId(), orden.getTotal());
        } catch (AmqpException e) {
            log.error("No se pudo publicar orden.creada ordenId={}: {}", orden.getId(), e.getMessage());
            throw new PublicacionException("No se pudo publicar el evento de la orden en RabbitMQ", e);
        }
    }

    /** Notificación no crítica: si falla, se registra y la operación continúa. */
    public void notificar(String destinatario, String asunto, String mensaje) {
        NotificacionEvent evento = new NotificacionEvent(
                UUID.randomUUID().toString(), destinatario, asunto, mensaje, ORIGEN, Instant.now());
        try {
            rabbitTemplate.convertAndSend(props.notificaciones().exchange(),
                    props.notificaciones().routingKey(), evento);
            log.info("Notificación publicada id={} para={}", evento.id(), destinatario);
        } catch (AmqpException e) {
            log.warn("No se pudo publicar la notificación para={}: {}", destinatario, e.getMessage());
        }
    }
}