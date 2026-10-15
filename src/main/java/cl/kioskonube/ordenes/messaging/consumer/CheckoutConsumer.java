package cl.kioskonube.ordenes.messaging.consumer;

import cl.kioskonube.ordenes.dto.CrearOrdenRequest;
import cl.kioskonube.ordenes.service.OrdenService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Dominio CARRITO: cuando el carrito hace checkout, se crea la orden.
 */
@Component
public class CheckoutConsumer {

    private final ProcesadorMensajes procesador;
    private final LectorMensajes lector;
    private final OrdenService ordenService;

    public CheckoutConsumer(ProcesadorMensajes procesador, LectorMensajes lector, OrdenService ordenService) {
        this.procesador = procesador;
        this.lector = lector;
        this.ordenService = ordenService;
    }

    @RabbitListener(id = "ordenes-checkout-consumer", queues = "${kioskonube.mensajeria.checkout.queue}")
    public void recibir(Message message, Channel channel) throws IOException {
        procesador.procesar(message, channel, "checkout", cuerpo -> {
            CrearOrdenRequest request = lector.leerYValidar(cuerpo, CrearOrdenRequest.class);
            ordenService.crear(request, "CARRITO");
        });
    }
}