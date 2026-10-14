package com.example.GameVault.service;

import com.cloudinary.Cloudinary;
import com.example.GameVault.model.Juego;
import com.example.GameVault.repository.JuegoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class JuegoService {

    @Autowired
    private JuegoRepository juegoRepository;

    @Autowired(required = false)
    private Cloudinary cloudinary;

    private static final String UPLOAD_DIR =
            "src/main/resources/static/uploads/";

    public List<Juego> listarTodos() {
        return juegoRepository.findAll();
    }

    public List<Juego> listarPorUsuario(Long usuarioId) {
        return juegoRepository.findByUsuarioId(usuarioId);
    }

    public Juego obtenerJuegoPorId(Long id) {
        return juegoRepository.findById(id).orElse(null);
    }

    public List<Juego> listarActivos() {
        return juegoRepository.findByActivoTrue();
    }

    public List<Juego> buscarActivosPorTitulo(String titulo) {
        return juegoRepository.findByTituloContainingIgnoreCaseAndActivoTrue(titulo);
    }

    public List<Juego> buscarCatalogo(String texto,
                                      String plataforma,
                                      Double precioMax,
                                      String orden) {

        if (texto != null && texto.isBlank()) {
            texto = null;
        }

        if (plataforma != null && plataforma.isBlank()) {
            plataforma = null;
        }

        Sort sort;

        if ("precioAsc".equals(orden)) {
            sort = Sort.by(Sort.Direction.ASC, "precio");

        } else if ("precioDesc".equals(orden)) {
            sort = Sort.by(Sort.Direction.DESC, "precio");

        } else if ("tituloDesc".equals(orden)) {
            sort = Sort.by(Sort.Direction.DESC, "titulo");

        } else {
            sort = Sort.by(Sort.Direction.ASC, "titulo");
        }

        return juegoRepository.buscarCatalogo(
                texto,
                plataforma,
                precioMax,
                sort
        );
    }

    public void eliminarJuego(Long id) {
        juegoRepository.deleteById(id);
    }

    public void guardarJuego(Juego juego, MultipartFile portada) {

        if (juego.getPrecio() == null || juego.getPrecio() < 0) {
            throw new IllegalArgumentException(
                    "El precio debe ser mayor o igual que cero (F2P)"
            );
        }

        if (juego.getTitulo() == null || juego.getTitulo().isBlank()) {
            throw new IllegalArgumentException(
                    "El título es obligatorio"
            );
        }

        if (juego.getPlataforma() == null || juego.getPlataforma().isBlank()) {
            throw new IllegalArgumentException(
                    "La plataforma es obligatoria"
            );
        }

        if (juego.getActivo() == null) {
            juego.setActivo(true);
        }

        if (portada != null && !portada.isEmpty()) {

            String nombreArchivo = guardarImagenEnProyecto(portada);

            juego.setPortadaUrl(nombreArchivo);

        } else if (juego.getPortadaUrl() == null
                || juego.getPortadaUrl().isBlank()) {

            juego.setPortadaUrl("default.jpg");
        }

        juegoRepository.save(juego);
    }

    private String guardarImagenEnProyecto(MultipartFile portada) {

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String nombreArchivo =
                    UUID.randomUUID().toString()
                            + "_"
                            + portada.getOriginalFilename();

            Path filePath = uploadPath.resolve(nombreArchivo);

            Files.copy(
                    portada.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            System.out.println(
                    "Archivo guardado correctamente en: "
                            + filePath.toAbsolutePath()
            );

            return nombreArchivo;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "default.jpg";
    }
}
