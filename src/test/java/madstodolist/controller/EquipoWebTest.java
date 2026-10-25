package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class EquipoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    private Map<String, Long> addUsuarioEquipoBD() {
        UsuarioData usuarioLogueado = new UsuarioData();
        usuarioLogueado.setEmail("admin@ua");
        usuarioLogueado.setPassword("123");
        usuarioLogueado = usuarioService.registrar(usuarioLogueado);

        UsuarioData miembro1 = new UsuarioData();
        miembro1.setEmail("miembro1@ua");
        miembro1.setPassword("123");
        miembro1.setNombre("Miembro Uno");
        miembro1 = usuarioService.registrar(miembro1);

        UsuarioData miembro2 = new UsuarioData();
        miembro2.setEmail("miembro2@ua");
        miembro2.setPassword("123");
        miembro2.setNombre("Miembro Dos");
        miembro2 = usuarioService.registrar(miembro2);

        EquipoData equipo = equipoService.crearEquipo("Proyecto P1");
        equipoService.añadirUsuarioAEquipo(equipo.getId(), miembro1.getId());
        equipoService.añadirUsuarioAEquipo(equipo.getId(), miembro2.getId());

        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuarioLogueado.getId());
        ids.put("equipoId", equipo.getId());
        return ids;
    }

    @Test
    public void listaEquipos() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquipoBD();

        // WHEN
        when(managerUserSession.usuarioLogeado()).thenReturn(ids.get("usuarioId"));

        // THEN
        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(view().name("listaEquipos"))
                .andExpect(content().string(containsString("Proyecto P1")))
                .andExpect(content().string(containsString("Ver miembros")));
    }

    @Test
    public void descripcionEquipo() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquipoBD();

        // WHEN
        when(managerUserSession.usuarioLogeado()).thenReturn(ids.get("usuarioId"));

        // THEN
        this.mockMvc.perform(get("/equipos/" + ids.get("equipoId")))
                .andExpect(status().isOk())
                .andExpect(view().name("descripcionEquipo"))
                .andExpect(content().string(containsString("Proyecto P1")))
                .andExpect(content().string(containsString("miembro1@ua")))
                .andExpect(content().string(containsString("miembro2@ua")))
                .andExpect(content().string(containsString("Unirme al equipo")));
    }

    @Test
    public void formularioNuevoEquipo() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquipoBD();

        // WHEN
        when(managerUserSession.usuarioLogeado()).thenReturn(ids.get("usuarioId"));

        // THEN
        this.mockMvc.perform(get("/equipos/nueva"))
                .andExpect(status().isOk())
                .andExpect(view().name("formNuevoEquipo"))
                .andExpect(content().string(containsString("Nuevo equipo")));
    }

    @Test
    public void crearEquipoWeb() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquipoBD();

        // WHEN
        when(managerUserSession.usuarioLogeado()).thenReturn(ids.get("usuarioId"));

        // THEN
        this.mockMvc.perform(post("/equipos/nueva")
                        .param("nombre", "Proyecto P2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"))
                .andExpect(flash().attribute("mensaje", "Equipo creado correctamente"));

        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Proyecto P2")));
    }

    @Test
    public void unirseEquipo() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquipoBD();

        // WHEN
        when(managerUserSession.usuarioLogeado()).thenReturn(ids.get("usuarioId"));

        // THEN
        this.mockMvc.perform(post("/equipos/" + ids.get("equipoId") + "/unirse"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"))
                .andExpect(flash().attribute("mensaje", "Te has unido al equipo correctamente"));

        // El equipo ya aparece como equipo del usuario y se ofrece salir de él
        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Salir del equipo")));

        this.mockMvc.perform(get("/equipos/" + ids.get("equipoId")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Salir del equipo")));
    }

    @Test
    public void salirEquipo() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquipoBD();
        equipoService.añadirUsuarioAEquipo(ids.get("equipoId"), ids.get("usuarioId"));

        // WHEN
        when(managerUserSession.usuarioLogeado()).thenReturn(ids.get("usuarioId"));

        // THEN
        this.mockMvc.perform(post("/equipos/" + ids.get("equipoId") + "/salir"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"))
                .andExpect(flash().attribute("mensaje", "Has dejado el equipo correctamente"));

        // El equipo ya no aparece como equipo del usuario y se ofrece unirse de nuevo
        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Unirme")));
    }

    @Test
    public void crearEquipoSinNombre() throws Exception {
        // GIVEN
        Map<String, Long> ids = addUsuarioEquipoBD();

        // WHEN
        when(managerUserSession.usuarioLogeado()).thenReturn(ids.get("usuarioId"));

        // THEN
        this.mockMvc.perform(post("/equipos/nueva")
                        .param("nombre", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("formNuevoEquipo"))
                .andExpect(content().string(containsString("El equipo no tiene nombre")));
    }
}
