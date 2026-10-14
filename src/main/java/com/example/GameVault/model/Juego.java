package com.example.GameVault.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "juegos")
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String titulo;

    @Column(length = 50)
    private String genero;

    @Column(length = 50)
    private String plataforma;

    @Column
    private Double precio;

    @Column(length = 1000, nullable = false)
    private String descripcion;

    @Column
    private Integer anio;

    @Column(nullable = false)
    private Boolean activo = true;

    // Almacenaremos el nombre del archivo o de la URL de la imagen
    @Column(name = "portada_url")
    private String portadaUrl;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

}
