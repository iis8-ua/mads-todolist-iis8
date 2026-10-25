package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class EquipoController {

    @Autowired
    EquipoService equipoService;

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    ManagerUserSession managerUserSession;

    //esto lo que hace es comprobar si el usuario ha hecho el login
    //igual que el de la tarea
    private UsuarioData comprobarUsuarioLogeado() {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UsuarioNoLogeadoException();
        }
        return usuarioService.findById(idUsuarioLogeado);
    }

    //lista todos los equipos ordenados por nombre
    @GetMapping("/equipos")
    public String listadoEquipos(Model model) {
        UsuarioData usuarioLogueado = comprobarUsuarioLogeado();
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("usuarioLogueado", usuarioLogueado);
        model.addAttribute("equipos", equipos);
        return "listaEquipos";
    }

    //datos del equipo y los usuarios de este
    @GetMapping("/equipos/{id}")
    public String descripcionEquipo(@PathVariable(value = "id") Long idEquipo, Model model) {
        UsuarioData usuarioLogueado = comprobarUsuarioLogeado();

        EquipoData equipo = equipoService.recuperarEquipo(idEquipo);
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(idEquipo);

        model.addAttribute("usuarioLogueado", usuarioLogueado);
        model.addAttribute("equipo", equipo);
        model.addAttribute("usuarios", usuarios);
        return "descripcionEquipo";
    }
}
