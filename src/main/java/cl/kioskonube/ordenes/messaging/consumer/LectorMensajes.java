package cl.kioskonube.ordenes.messaging.consumer;

import cl.kioskonube.ordenes.exception.ErrorDefinitivoException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Convierte el JSON de un mensaje y valida sus campos.
 * Si el mensaje es ilegible o inválido, lanza ErrorDefinitivoException (reintentar no sirve).
 */
@Component
public class LectorMensajes {

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private final Validator validator;

    public LectorMensajes(Validator validator) {
        this.validator = validator;
    }

    public <T> T leerYValidar(byte[] cuerpo, Class<T> tipo) {
        T valor;
        try {
            valor = jsonMapper.readValue(cuerpo, tipo);
        } catch (JacksonException e) {
            throw new ErrorDefinitivoException("JSON ilegible: " + e.getOriginalMessage(), e);
        }
        if (valor == null) {
            throw new ErrorDefinitivoException("El mensaje está vacío", null);
        }

        Set<ConstraintViolation<T>> errores = validator.validate(valor);
        if (!errores.isEmpty()) {
            String detalle = errores.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new ErrorDefinitivoException("Mensaje inválido: " + detalle, null);
        }
        return valor;
    }
}