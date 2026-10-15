package cl.kioskonube.ordenes.messaging.consumer;

import cl.kioskonube.ordenes.exception.ErrorDefinitivoException;
import cl.kioskonube.ordenes.messaging.event.DespachoActualizadoEvent;
import cl.kioskonube.ordenes.service.OrdenService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Dominio DESPACHOS: cuando cambia un despacho, se actualiza el estado de la orden.
 */
@Component
public class DespachoConsumer {

    private final ProcesadorMensajes procesador;
    private final LectorMensajes lector;
    private final OrdenService ordenService;

    public DespachoConsumer(ProcesadorMensajes procesador, LectorMensajes lector, OrdenService ordenService) {
        this.procesador = procesador;
        this.lector = lector;
        this.ordenService = ordenService;
    }

    @RabbitListener(id = "ordenes-despachos-consumer", queues = "${kioskonube.mensajeria.despachos.queue}")
    public void recibir(Message message, Channel channel) throws IOException {
        procesador.procesar(message, channel, "despachos", cuerpo -> {
            DespachoActualizadoEvent evento = lector.leerYValidar(cuerpo, DespachoActualizadoEvent.class);
            try {
                ordenService.aplicarEstadoDespacho(evento.ordenId(), evento.estado());
            } catch (IllegalArgumentException e) {
                // Estado desconocido: es un dato malo, reintentar no sirve
                throw new ErrorDefinitivoException(e.getMessage(), e);
            }
            // OrdenNoEncontradaException se trata como TEMPORAL a propósito: el evento
            // de despacho puede llegar antes de que termine de guardarse la orden.
        });
    }
}