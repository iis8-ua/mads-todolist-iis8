package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    ManagerUserSession managerUserSession;
    @Autowired
    UsuarioService usuarioService;

    @GetMapping("/about")
    public String about(Model model) {

        Long id = managerUserSession.usuarioLogeado();

        if (id != null) {
            UsuarioData usuario = usuarioService.findById(id);
            model.addAttribute("usuario", usuario);
        }

        return "about";
    }
}
