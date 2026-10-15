package cl.kioskonube.ordenes.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Orden de compra (pedido). Una venta es una orden ENTREGADA.
 */
@Entity
@Table(name = "ordenes")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_email", nullable = false, length = 150)
    private String clienteEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOrden estado;

    /** De dónde vino la orden: API (POST /ordenes) o CARRITO (checkout por RabbitMQ). */
    @Column(nullable = false, length = 20)
    private String origen;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private Instant fechaActualizacion;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenItem> items = new ArrayList<>();

    protected Orden() {
        // requerido por JPA
    }

    public Orden(String clienteEmail, String origen) {
        this.clienteEmail = clienteEmail;
        this.origen = origen;
        this.estado = EstadoOrden.CREADA;
        this.total = BigDecimal.ZERO;
        this.fechaCreacion = Instant.now();
    }

    public void agregarItem(OrdenItem item) {
        item.asignarOrden(this);
        items.add(item);
        total = total.add(item.subtotal());
    }

    public void cambiarEstado(EstadoOrden nuevoEstado) {
        this.estado = nuevoEstado;
        this.fechaActualizacion = Instant.now();
    }

    public Long getId() { return id; }
    public String getClienteEmail() { return clienteEmail; }
    public EstadoOrden getEstado() { return estado; }
    public String getOrigen() { return origen; }
    public BigDecimal getTotal() { return total; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public Instant getFechaActualizacion() { return fechaActualizacion; }
    public List<OrdenItem> getItems() { return items; }
}