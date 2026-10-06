package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class UsuarioController {
    @Autowired
    UsuarioService usuarioService;

    @GetMapping("/registrados")
    public String registrados(Model model) {
        List<UsuarioData> usuarios = usuarioService.todosUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String descripcion(@PathVariable Long id, Model model) {
        UsuarioData usuario = usuarioService.findById(id);

        //se hace esto para que si no hay usuario se redirige a la misma ya que no hay nada que mostrar
        if (usuario == null) {
            return "redirect:/registrados";
        }

        model.addAttribute("usuario", usuario);
        return "descripcionUsuario";
    }
}