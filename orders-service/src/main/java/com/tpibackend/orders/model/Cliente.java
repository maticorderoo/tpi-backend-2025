package com.tpibackend.orders.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 30)
    private String telefono;

    @Column(nullable = false, unique = true, length = 20)
    private String cuit;

    @OneToMany(mappedBy = "cliente")
    private List<Contenedor> contenedores = new ArrayList<>();

    @OneToMany(mappedBy = "cliente")
    private List<Solicitud> solicitudes = new ArrayList<>();
}
