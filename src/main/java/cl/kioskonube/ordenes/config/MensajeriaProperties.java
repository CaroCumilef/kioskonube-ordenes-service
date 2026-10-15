package cl.kioskonube.ordenes.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Nombres de exchanges, colas y routing keys del ordenes-service.
 * Se leen desde application.yml (kioskonube.mensajeria).
 */
@Validated
@ConfigurationProperties(prefix = "kioskonube.mensajeria")
public record MensajeriaProperties(
        @NotNull @Valid Publicacion ordenes,
        @NotNull @Valid Publicacion notificaciones,
        @NotNull @Valid Suscripcion checkout,
        @NotNull @Valid Suscripcion despachos,
        @NotNull @Valid DeadLetter deadLetter
) {

    /** Destino al que este servicio publica. */
    public record Publicacion(@NotBlank String exchange, @NotBlank String routingKey) {
    }

    /** Cola propia que escucha mensajes de otro dominio. */
    public record Suscripcion(@NotBlank String exchange, @NotBlank String queue, @NotBlank String routingKey) {
    }

    /** Convención de nombres para DLX y DLQ (igual que rabbit-admin-service). */
    public record DeadLetter(@NotBlank String dlxSufijo, @NotBlank String dlqSufijo) {
    }

    public String dlxDe(String queue) {
        return queue + deadLetter.dlxSufijo();
    }

    public String dlqDe(String queue) {
        return queue + deadLetter.dlqSufijo();
    }
}