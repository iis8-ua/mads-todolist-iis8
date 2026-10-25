package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

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

    //igual que para los usuarios pero para ver si es admin
    private UsuarioData comprobarAdministrador() {
        UsuarioData usuarioLogueado = comprobarUsuarioLogeado();
        if (!usuarioService.esAdministrador(usuarioLogueado.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos suficientes"
            );
        }
        return usuarioLogueado;
    }

    //lista todos los equipos ordenados por nombre
    @GetMapping("/equipos")
    public String listadoEquipos(Model model) {
        UsuarioData usuarioLogueado = comprobarUsuarioLogeado();
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        //lista con todos los usuarios del equipo, solo ids
        List<Long> idsEquiposUsuario = equipoService.equiposUsuario(usuarioLogueado.getId()).stream().map(EquipoData::getId).collect(Collectors.toList());

        model.addAttribute("usuarioLogueado", usuarioLogueado);
        model.addAttribute("equipos", equipos);
        model.addAttribute("idsEquiposUsuario", idsEquiposUsuario);
        return "listaEquipos";
    }

    @GetMapping("/equipos/nueva")
    //manda a crear un equipo
    public String formNuevoEquipo(Model model) {
        UsuarioData usuarioLogueado = comprobarUsuarioLogeado();
        model.addAttribute("usuarioLogueado", usuarioLogueado);
        model.addAttribute("equipoData", new EquipoData());
        return "formNuevoEquipo";
    }

    @PostMapping("/equipos/nueva")
    public String nuevoEquipo(@ModelAttribute EquipoData equipoData, Model model, RedirectAttributes flash) {
        UsuarioData usuarioLogueado = comprobarUsuarioLogeado();
        try {
            equipoService.crearEquipo(equipoData.getNombre());
        } catch (EquipoServiceException e) {
            model.addAttribute("usuarioLogueado", usuarioLogueado);
            model.addAttribute("error", e.getMessage());
            return "formNuevoEquipo";
        }

        //el mensaje que aparece abajo al confirmar
        flash.addFlashAttribute("mensaje", "Equipo creado correctamente");
        return "redirect:/equipos";
    }

    //datos del equipo y los usuarios de este
    @GetMapping("/equipos/{id}")
    public String descripcionEquipo(@PathVariable(value = "id") Long idEquipo, Model model) {
        UsuarioData usuarioLogueado = comprobarUsuarioLogeado();

        EquipoData equipo = equipoService.recuperarEquipo(idEquipo);
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(idEquipo);

        // para que salga en el detalle del equipo el boton de salir o el de unirse luego en la vista
        boolean pertenece = usuarios.stream().anyMatch(usuario -> usuario.getId().equals(usuarioLogueado.getId()));

        model.addAttribute("usuarioLogueado", usuarioLogueado);
        model.addAttribute("equipo", equipo);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("pertenece", pertenece);
        return "descripcionEquipo";
    }

    @PostMapping("/equipos/{id}/unirse")
    //añade al usuario al equipo
    public String unirseEquipo(@PathVariable(value = "id") Long idEquipo, RedirectAttributes flash) {
        UsuarioData usuarioLogueado = comprobarUsuarioLogeado();
        try {
            equipoService.añadirUsuarioAEquipo(idEquipo, usuarioLogueado.getId());
            flash.addFlashAttribute("mensaje", "Te has unido al equipo correctamente");
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/equipos";
    }

    @PostMapping("/equipos/{id}/salir")
    //elimina al usaurio del equipo
    public String salirEquipo(@PathVariable(value = "id") Long idEquipo, RedirectAttributes flash) {
        UsuarioData usuarioLogueado = comprobarUsuarioLogeado();
        try {
            equipoService.eliminarUsuarioDeEquipo(idEquipo, usuarioLogueado.getId());
            flash.addFlashAttribute("mensaje", "Has dejado el equipo correctamente");
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/equipos";
    }

    //devuelve la vista para editar el equipo
    @GetMapping("/equipos/{id}/editar")
    public String formEditarEquipo(@PathVariable(value = "id") Long idEquipo, Model model) {
        UsuarioData usuarioLogueado = comprobarAdministrador();

        EquipoData equipo = equipoService.recuperarEquipo(idEquipo);
        //se crea el equipo nuevo con los datos para pasarselo al formulario para editar luego
        EquipoData equipoData = new EquipoData();
        equipoData.setId(equipo.getId());
        equipoData.setNombre(equipo.getNombre());

        model.addAttribute("usuarioLogueado", usuarioLogueado);
        model.addAttribute("equipo", equipo);
        model.addAttribute("equipoData", equipoData);
        return "formEditarEquipo";
    }

    //va a cambiar el nombre del equipo
    @PostMapping("/equipos/{id}/editar")
    public String editarEquipo(@PathVariable(value = "id") Long idEquipo, @ModelAttribute EquipoData equipoData, Model model, RedirectAttributes flash) {
        UsuarioData usuarioLogueado = comprobarAdministrador();

        try {
            equipoService.renombrarEquipo(idEquipo, equipoData.getNombre());
            flash.addFlashAttribute("mensaje", "Equipo renombrado correctamente");
            return "redirect:/equipos";
        }
        catch (EquipoServiceException e) {
            equipoData.setId(idEquipo);
            model.addAttribute("usuarioLogueado", usuarioLogueado);
            model.addAttribute("equipoData", equipoData);
            model.addAttribute("error", e.getMessage());
            return "formEditarEquipo";
        }
    }

    //va a eliminar el equipo
    @DeleteMapping("/equipos/{id}")
    @ResponseBody
    public String eliminarEquipo(@PathVariable(value = "id") Long idEquipo, RedirectAttributes flash) {
        comprobarAdministrador();
        try {
            equipoService.eliminarEquipo(idEquipo);
            flash.addFlashAttribute("mensaje", "Equipo eliminado correctamente");
        }
        catch (EquipoServiceException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "";
    }
}
