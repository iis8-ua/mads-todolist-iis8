package madstodolist.service;

import madstodolist.model.Equipo;
import madstodolist.model.Tarea;
import madstodolist.model.Usuario;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.TareaRepository;
import madstodolist.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Profile({"dev", "postgres"})
public class InitDbService {

    private static final String EMAIL_USUARIO = "user@ua";
    private static final String EMAIL_USUARIO2 = "maria@ua";

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TareaRepository tareaRepository;
    @Autowired
    private EquipoRepository equipoRepository;

    // Se ejecuta tras crear el contexto de la aplicación
    // para inicializar la base de datos
    @PostConstruct
    public void initDatabase() {
        Usuario usuario = usuarioRepository.findByEmail(EMAIL_USUARIO).orElse(null);

        if (usuario == null) {
            usuario = crearUsuario(EMAIL_USUARIO, "Usuario Ejemplo");

            Tarea tarea1 = new Tarea(usuario, "Lavar coche");
            tareaRepository.save(tarea1);

            Tarea tarea2 = new Tarea(usuario, "Renovar DNI");
            tareaRepository.save(tarea2);
        }

        Usuario usuario2 = usuarioRepository.findByEmail(EMAIL_USUARIO2)
                .orElseGet(() -> crearUsuario(EMAIL_USUARIO2, "María García"));

        crearEquipoSiNoExiste("Proyecto P1", usuario, usuario2);
        crearEquipoSiNoExiste("Proyecto P2", usuario);
    }

    private Usuario crearUsuario(String email, String nombre) {
        Usuario usuario = new Usuario(email);
        usuario.setNombre(nombre);
        usuario.setPassword("123");
        return usuarioRepository.save(usuario);
    }

    private void crearEquipoSiNoExiste(String nombre, Usuario... miembros) {
        boolean existe = equipoRepository.findAll().stream()
                .anyMatch(equipo -> equipo.getNombre().equals(nombre));
        if (existe) {
            return;
        }

        Equipo equipo = new Equipo(nombre);
        for (Usuario miembro : miembros) {
            equipo.getUsuarios().add(miembro);
        }
        equipoRepository.save(equipo);
    }

}
