package com.example.GameVault.controller;

import com.example.GameVault.model.Juego;
import com.example.GameVault.model.Usuario;
import com.example.GameVault.service.JuegoService;
import com.example.GameVault.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Controller
public class GameController {

    @Autowired
    private JuegoService juegoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/fragments-demo")
    public String fragmentsDemo() {
        return "fragments-demo";
    }

    @GetMapping({"/", "/juegos"})
    public String listarJuegos(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "plataforma", required = false) String plataforma,
            @RequestParam(value = "precioMax", required = false) Double precioMax,
            @RequestParam(value = "orden", required = false) String orden,
            Model model) {

        model.addAttribute(
                "juegos",
                juegoService.buscarCatalogo(
                        q,
                        plataforma,
                        precioMax,
                        orden
                )
        );

        model.addAttribute("q", q);
        model.addAttribute("plataforma", plataforma);
        model.addAttribute("precioMax", precioMax);
        model.addAttribute("orden", orden);

        return "juegos";
    }

    @GetMapping("/juegos/nuevo")
    public String mostrarFormulario(
            HttpSession session,
            Model model) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return "redirect:/login";
        }

        if (usuarioService.buscarPorId(usuarioId).isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("juego", new Juego());

        return "formulario";
    }

    @GetMapping("/mis-juegos")
    public String mostrarMisJuegos(
            HttpSession session,
            Model model) {

        // Recuperar el identificador del usuario autenticado.
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        // Si no existe una sesión autenticada, regresar al login.
        if (usuarioId == null) {
            return "redirect:/login";
        }

        // Buscar al usuario en la base de datos.
        Optional<Usuario> usuarioEncontrado =
                usuarioService.buscarPorId(usuarioId);

        // Comprobar que el usuario todavía exista.
        if (usuarioEncontrado.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        Usuario usuario = usuarioEncontrado.get();

        // Recuperar únicamente los videojuegos de este usuario.
        model.addAttribute(
                "juegos",
                juegoService.listarPorUsuario(usuario.getId())
        );

        model.addAttribute("usuario", usuario);

        return "mis-juegos";
    }

    @GetMapping("/juegos/editar/{id}")
    public String editarJuego(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return "redirect:/login";
        }

        Juego juego = juegoService.obtenerJuegoPorId(id);

        // Aquí comprobamos que el juego exista y que le pertenezca al usuario.
        if (juego == null
                || juego.getUsuario() == null
                || !juego.getUsuario().getId().equals(usuarioId)) {

            return "redirect:/mis-juegos";
        }

        model.addAttribute("juego", juego);

        return "formulario";
    }

    @PostMapping("/juegos/editar/{id}")
    public String actualizarJuego(
            @PathVariable Long id,
            @RequestParam("titulo") String titulo,
            @RequestParam("genero") String genero,
            @RequestParam("plataforma") String plataforma,
            @RequestParam("precio") Double precio,
            @RequestParam("descripcion") String descripcion,
            @RequestParam(value = "anio", required = false) Integer anio,
            @RequestParam(value = "portada", required = false)
            MultipartFile portada,
            HttpSession session) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return "redirect:/login";
        }

        Juego juego = juegoService.obtenerJuegoPorId(id);

        // Volvemos a validar la propiedad antes de permitir que modifiquen un juego.
        if (juego == null
                || juego.getUsuario() == null
                || !juego.getUsuario().getId().equals(usuarioId)) {

            return "redirect:/mis-juegos";
        }

        juego.setTitulo(titulo);
        juego.setGenero(genero);
        juego.setPlataforma(plataforma);
        juego.setPrecio(precio);
        juego.setDescripcion(descripcion);
        juego.setAnio(anio);

        juegoService.guardarJuego(juego, portada);

        return "redirect:/mis-juegos";
    }

    @PostMapping("/juegos")
    public String guardarJuego(
            @RequestParam("titulo") String titulo,
            @RequestParam("genero") String genero,
            @RequestParam("plataforma") String plataforma,
            @RequestParam("precio") Double precio,
            @RequestParam("descripcion") String descripcion,
            @RequestParam(value = "anio", required = false) Integer anio,
            @RequestParam("portada") MultipartFile portada,
            HttpSession session) {

        // Obtener al usuario autenticado.
        // Primero validamos que el usuario esté autenticado
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return "redirect:/login";
        }

        // Aquí validamos que el usuario exista.
        Optional<Usuario> usuarioEncontrado =
                usuarioService.buscarPorId(usuarioId);

        if (usuarioEncontrado.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        Usuario usuario = usuarioEncontrado.get();

        // Creamos un nuevo juego con sus respectivos atributos.
        Juego nuevoJuego = new Juego();

        nuevoJuego.setTitulo(titulo);
        nuevoJuego.setGenero(genero);
        nuevoJuego.setPlataforma(plataforma);
        nuevoJuego.setPrecio(precio);
        nuevoJuego.setDescripcion(descripcion);
        nuevoJuego.setAnio(anio);

        // El juego creado lo asociamos a un usuario existente, obviamente.
        nuevoJuego.setUsuario(usuario);

        juegoService.guardarJuego(nuevoJuego, portada);

        return "redirect:/mis-juegos";
    }

    @PostMapping("/juegos/eliminar/{id}")
    public String eliminarJuego(
            @PathVariable Long id,
            HttpSession session) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return "redirect:/login";
        }

        Juego juego = juegoService.obtenerJuegoPorId(id);

        // Solamente el usuario propietario podrá eliminar sus juegos
        if (juego != null
                && juego.getUsuario() != null
                && juego.getUsuario().getId().equals(usuarioId)) {

            juegoService.eliminarJuego(id);
        }

        return "redirect:/mis-juegos";
    }
}
