package cl.kioskonube.ordenes.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología RabbitMQ del ordenes-service. Solo configuración, sin lógica de negocio.
 */
@Configuration
public class RabbitConfig {

    private final MensajeriaProperties props;

    public RabbitConfig(MensajeriaProperties props) {
        this.props = props;
    }

    // ===== Exchanges de dominio (topic) =====

    @Bean
    public TopicExchange ordenesExchange() {
        return new TopicExchange(props.ordenes().exchange(), true, false);
    }

    @Bean
    public TopicExchange carritoExchange() {
        return new TopicExchange(props.checkout().exchange(), true, false);
    }

    @Bean
    public TopicExchange despachosExchange() {
        return new TopicExchange(props.despachos().exchange(), true, false);
    }

    // ===== Caso de uso: checkout del carrito -> crear orden =====

    @Bean
    public Queue checkoutQueue() {
        String q = props.checkout().queue();
        return QueueBuilder.durable(q)
                .deadLetterExchange(props.dlxDe(q))
                .deadLetterRoutingKey(q)
                .build();
    }

    @Bean
    public Binding checkoutBinding() {
        return BindingBuilder.bind(checkoutQueue()).to(carritoExchange()).with(props.checkout().routingKey());
    }

    @Bean
    public DirectExchange checkoutDlx() {
        return new DirectExchange(props.dlxDe(props.checkout().queue()), true, false);
    }

    @Bean
    public Queue checkoutDlq() {
        return QueueBuilder.durable(props.dlqDe(props.checkout().queue())).build();
    }

    @Bean
    public Binding checkoutDlqBinding() {
        return BindingBuilder.bind(checkoutDlq()).to(checkoutDlx()).with(props.checkout().queue());
    }

    // ===== Caso de uso: despacho actualizado -> actualizar estado de la orden =====

    @Bean
    public Queue despachosQueue() {
        String q = props.despachos().queue();
        return QueueBuilder.durable(q)
                .deadLetterExchange(props.dlxDe(q))
                .deadLetterRoutingKey(q)
                .build();
    }

    @Bean
    public Binding despachosBinding() {
        return BindingBuilder.bind(despachosQueue()).to(despachosExchange()).with(props.despachos().routingKey());
    }

    @Bean
    public DirectExchange despachosDlx() {
        return new DirectExchange(props.dlxDe(props.despachos().queue()), true, false);
    }

    @Bean
    public Queue despachosDlq() {
        return QueueBuilder.durable(props.dlqDe(props.despachos().queue())).build();
    }

    @Bean
    public Binding despachosDlqBinding() {
        return BindingBuilder.bind(despachosDlq()).to(despachosDlx()).with(props.despachos().queue());
    }

    // ===== Envío de mensajes en JSON =====

    /**
     * Solo el RabbitTemplate (productor) usa JSON. Los consumidores reciben el Message
     * crudo y lo convierten ellos mismos, para manejar los errores de forma explícita.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new JacksonJsonMessageConverter());
        return template;
    }
}