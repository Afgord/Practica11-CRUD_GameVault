package com.example.GameVault.repository;

import com.example.GameVault.model.Juego;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JuegoRepository extends JpaRepository<Juego, Long> {

    // SELECT * FROM Juegos WHERE titulo LIKE '%titulo%'
    List<Juego> findByTituloContainingIgnoreCase(String titulo);

    // Vamos a buscar juegos por su Título pero que estén activos.
    List<Juego> findByTituloContainingIgnoreCaseAndActivoTrue(String titulo);

    List<Juego> findByGeneroContainingIgnoreCase(String genero);

    List<Juego> findByPlataforma(String plataforma);

    List<Juego> findByPrecioLessThan(Double precio);

    List<Juego> findByActivoTrue();

    List<Juego> findByUsuarioId(Long usuarioId);

    @Query("SELECT j FROM Juego j WHERE LOWER(j.descripcion) LIKE LOWER(CONCAT('%', :palabra, '%'))")
    List<Juego> findJuegoBy(@Param("palabra") String palabra);

    @Query("""
    SELECT j FROM Juego j
    WHERE j.activo = true
      AND (:texto IS NULL
           OR LOWER(j.titulo) LIKE LOWER(CONCAT('%', :texto, '%')))
      AND (:plataforma IS NULL
           OR j.plataforma = :plataforma)
      AND (:precioMax IS NULL
           OR j.precio <= :precioMax)
""")
    List<Juego> buscarCatalogo(
            @Param("texto") String texto,
            @Param("plataforma") String plataforma,
            @Param("precioMax") Double precioMax,
            Sort sort
    );

}
