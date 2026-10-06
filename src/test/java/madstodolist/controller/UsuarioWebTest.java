package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
//
// A diferencia de los tests web de tarea, donde usábamos los datos
// de prueba de la base de datos, aquí vamos a practicar otro enfoque:
// moquear el usuarioService.
public class UsuarioWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ManagerUserSession managerUserSession;

    // Moqueamos el usuarioService.
    // En los tests deberemos proporcionar el valor devuelto por las llamadas
    // a los métodos de usuarioService que se van a ejecutar cuando se realicen
    // las peticiones a los endpoint.
    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void servicioLoginUsuarioOK() throws Exception {
        // GIVEN
        // Moqueamos la llamada a usuarioService.login para que
        // devuelva un LOGIN_OK y la llamada a usuarioServicie.findByEmail
        // para que devuelva un usuario determinado.

        UsuarioData anaGarcia = new UsuarioData();
        anaGarcia.setNombre("Ana García");
        anaGarcia.setId(1L);

        when(usuarioService.login("ana.garcia@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
        when(usuarioService.findByEmail("ana.garcia@gmail.com"))
                .thenReturn(anaGarcia);

        // WHEN, THEN
        // Realizamos una petición POST al login pasando los datos
        // esperados en el mock, la petición devolverá una redirección a la
        // URL con las tareas del usuario

        this.mockMvc.perform(post("/login")
                        .param("eMail", "ana.garcia@gmail.com")
                        .param("password", "12345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/tareas"));
    }

    @Test
    public void servicioLoginUsuarioNotFound() throws Exception {
        // GIVEN
        // Moqueamos el método usuarioService.login para que devuelva
        // USER_NOT_FOUND
        when(usuarioService.login("pepito.perez@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.USER_NOT_FOUND);

        // WHEN, THEN
        // Realizamos una petición POST con los datos del usuario mockeado y
        // se debe devolver una página que contenga el mensaja "No existe usuario"
        this.mockMvc.perform(post("/login")
                        .param("eMail","pepito.perez@gmail.com")
                        .param("password","12345678"))
                .andExpect(content().string(containsString("No existe usuario")));
    }

    @Test
    public void servicioLoginUsuarioErrorPassword() throws Exception {
        // GIVEN
        // Moqueamos el método usuarioService.login para que devuelva
        // ERROR_PASSWORD
        when(usuarioService.login("ana.garcia@gmail.com", "000"))
                .thenReturn(UsuarioService.LoginStatus.ERROR_PASSWORD);

        // WHEN, THEN
        // Realizamos una petición POST con los datos del usuario mockeado y
        // se debe devolver una página que contenga el mensaja "Contraseña incorrecta"
        this.mockMvc.perform(post("/login")
                        .param("eMail","ana.garcia@gmail.com")
                        .param("password","000"))
                .andExpect(content().string(containsString("Contraseña incorrecta")));
    }

    @Test
    public void getRegistradosMuestraUsuarios() throws Exception {
        //arrange
        UsuarioData usuario1 = new UsuarioData();
        usuario1.setId(1L);
        usuario1.setEmail("user1@ua");
        usuario1.setNombre("Usuario 1");

        UsuarioData usuario2 = new UsuarioData();
        usuario2.setId(2L);
        usuario2.setEmail("user2@ua");
        usuario2.setNombre("Usuario 2");

        when(usuarioService.todosUsuarios())
                .thenReturn(Arrays.asList(usuario1, usuario2));

        //act y assert
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("1"),
                        containsString("user1@ua"),
                        containsString("2"),
                        containsString("user2@ua")
                )));
    }

    @Test
    public void getRegistradosMuestraEnlacesDescripcion() throws Exception {
        //arrange
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setEmail("user@ua");
        usuario.setNombre("Usuario Ejemplo");

        when(usuarioService.todosUsuarios())
                .thenReturn(Collections.singletonList(usuario));

        //act y assert
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("/usuarios/1")
                ));
    }

    @Test
    public void getDescripcionUsuarioMuestraDatos() throws Exception {
        //arrange
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setEmail("user@ua");
        usuario.setNombre("Usuario Ejemplo");

        when(usuarioService.findById(1L))
                .thenReturn(usuario);

        //act y assert
        this.mockMvc.perform(get("/registrados/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("1"),
                        containsString("user@ua"),
                        containsString("Usuario Ejemplo")
                )));
    }

    @Test
    public void getDescripcionUsuarioNoExistenteRedirigeARegistrados() throws Exception {
        //arrange
        when(usuarioService.findById(999L))
                .thenReturn(null);

        //act y assert
        this.mockMvc.perform(get("/registrados/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));
    }

    @Test
    public void getDescripcionUsuarioNoMuestraPassword() throws Exception {
        //arrange
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setEmail("user@ua");
        usuario.setNombre("Usuario Ejemplo");
        usuario.setPassword("123456");

        when(usuarioService.findById(1L))
                .thenReturn(usuario);

        //act y assert
        this.mockMvc.perform(get("/registrados/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("user@ua"),
                        containsString("Usuario Ejemplo")
                )))
                .andExpect(content().string(
                        not(containsString("123456"))
                ));
    }

    @Test
    public void getRegistroMuestraCheckboxAdministrador() throws Exception {
        //arrange
        when(usuarioService.existeAdministrador()).thenReturn(false);

        //act y assert
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registrarse como administrador")))
                .andExpect(content().string(containsString("id=\"administrador\"")));
    }

    @Test
    public void getRegistroNoMuestraCheckboxSiExisteAdministrador() throws Exception {
        //arrange
        when(usuarioService.existeAdministrador()).thenReturn(true);

        //act y assert
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Registrarse como administrador"))))
                .andExpect(content().string(not(containsString("id=\"administrador\""))));
    }

    @Test
    public void servicioLoginAdministradorRedirigeARegistrados() throws Exception {
        //arrange
        UsuarioData administrador = new UsuarioData();
        administrador.setId(1L);
        administrador.setEmail("admin@ua");
        administrador.setNombre("Administrador");
        administrador.setAdministrador(true);

        when(usuarioService.login("admin@ua", "1234"))
                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);

        when(usuarioService.findByEmail("admin@ua"))
                .thenReturn(administrador);

        //act y assert
        mockMvc.perform(post("/login")
                        .param("eMail", "admin@ua")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));
    }

    @Test
    public void administradorPuedeAccederAListaUsuarios() throws Exception {
        //arrange
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.esAdministrador(1L)).thenReturn(true);
        when(usuarioService.todosUsuarios()).thenReturn(new ArrayList<>());

        //act y assert
        mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk());
    }

    @Test
    public void usuarioNoAdministradorNoPuedeAccederAListaUsuarios() throws Exception {
        //arrange
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.esAdministrador(1L)).thenReturn(false);

        //act y assert
        mockMvc.perform(get("/registrados"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void usuarioNoAdministradorNoPuedeAccederADescripcion() throws Exception {
        //arrange
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.esAdministrador(1L)).thenReturn(false);

        //act y assert
        mockMvc.perform(get("/registrados/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void administradorPuedeAccederADescripcion() throws Exception {
        //arrange
        UsuarioData usuario = new UsuarioData();
        usuario.setId(2L);
        usuario.setEmail("user@ua");
        usuario.setNombre("Usuario");

        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.esAdministrador(1L)).thenReturn(true);
        when(usuarioService.findById(2L)).thenReturn(usuario);

        //act y assert
        mockMvc.perform(get("/registrados/2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("user@ua")));
    }
}
