package cl.kioskonube.ordenes.messaging.consumer;

import cl.kioskonube.ordenes.config.OrdenesProperties;
import cl.kioskonube.ordenes.exception.ErrorDefinitivoException;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Manejo común de ACK manual para todos los consumidores del servicio:
 *  - éxito                      -> basicAck
 *  - ErrorDefinitivoException   -> basicNack(requeue=false) -> DLQ inmediata
 *  - cualquier otro error       -> reintento con espera creciente; si se agotan -> DLQ
 */
@Component
public class ProcesadorMensajes {

    private static final Logger log = LoggerFactory.getLogger(ProcesadorMensajes.class);

    @FunctionalInterface
    public interface Accion {
        void ejecutar(byte[] cuerpo);
    }

    private final OrdenesProperties props;

    public ProcesadorMensajes(OrdenesProperties props) {
        this.props = props;
    }

    public void procesar(Message message, Channel channel, String consumidor, Accion accion) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        int maxIntentos = props.reintentos().maxIntentos();

        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                accion.ejecutar(message.getBody());
                channel.basicAck(tag, false);
                log.info("[{}] ACK tag={} (intento {}/{})", consumidor, tag, intento, maxIntentos);
                return;
            } catch (ErrorDefinitivoException e) {
                log.error("[{}] Error definitivo tag={}: {}. NACK → DLQ", consumidor, tag, e.getMessage());
                channel.basicNack(tag, false, false);
                return;
            } catch (RuntimeException e) {
                log.warn("[{}] Error temporal tag={} (intento {}/{}): {}",
                        consumidor, tag, intento, maxIntentos, e.getMessage());
                if (intento < maxIntentos && !esperar(props.reintentos().esperaMs() * intento)) {
                    break;
                }
            }
        }

        log.error("[{}] Reintentos agotados tag={} ({} intentos). NACK → DLQ", consumidor, tag, maxIntentos);
        channel.basicNack(tag, false, false);
    }

    private boolean esperar(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}