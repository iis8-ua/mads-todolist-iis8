package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class UsuarioController {
    @Autowired
    UsuarioService usuarioService;

    @Autowired
    ManagerUserSession managerUserSession;

    @GetMapping("/registrados")
    public String registrados(Model model) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null || !usuarioService.esAdministrador(idUsuario)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos suficientes"
            );
        }

        List<UsuarioData> usuarios = usuarioService.todosUsuarios();
        UsuarioData usuarioLogueado = usuarioService.findById(idUsuario);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("usuarioLogueado", usuarioLogueado);
        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String descripcion(@PathVariable Long id, Model model) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null || !usuarioService.esAdministrador(idUsuario)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos suficientes"
            );
        }

        UsuarioData usuarioLogueado = usuarioService.findById(idUsuario);
        UsuarioData usuario = usuarioService.findById(id);

        //se hace esto para que si no hay usuario se redirige a la misma ya que no hay nada que mostrar
        if (usuario == null) {
            return "redirect:/registrados";
        }

        model.addAttribute("usuarioLogueado", usuarioLogueado);
        model.addAttribute("usuario", usuario);
        return "descripcionUsuario";
    }

    @PostMapping("/registrados/{id}/bloqueo")
    public String cambiarEstadoBloqueo(@PathVariable Long id) {

        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null || !usuarioService.esAdministrador(idUsuario)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos suficientes"
            );
        }

        usuarioService.cambiarEstadoBloqueo(id);

        return "redirect:/registrados";
    }
}