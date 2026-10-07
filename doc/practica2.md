# Documentación ToDoList 

## 1. Introducción

En esta aplicación lo que se ha hecho es ampliar la ToDoList que se nos da de base que solo tiene implementado el login y el registro con la página de las tareas para el usuario. En esta implementación se va a introducir los conceptos de la protección en los accesos junto con el bloqueo de ciertos usuarios mediante la implementación de un usuario administrador, además de añadir la parte del listado de usuarios y la descripción de cada uno para que el admin lo pueda ver para gestionar ese bloqueo que he dicho y la protección va a consistir en que un usuario normal no pueda acceder a esta parte

Esto se ha elaborado siguiendo la arquitectura por capas, donde en el modelo esta los atributos y métodos de las entidades, los repositorios donde se definen los metodos del acceso a datos que se implementan luego en los servicios, los controladores que controla las peticiones que hacen los usuarios con la api y redirige a los templates que tiene que mostrar y los DTOs para transportar los datos

## 2. Nuevas funcionalidades

### Usuario administrador

Se trata de un usuario con privilegios mayores que en esta aplicacion se va a encargar de la gestión de los usuarios. 

Solo va a poder haber un administrador único, donde en el registro hay un checkbox para indicar que eres administrador. Si ya hay registrado un administrador, ese checkbox ya no va a aparecer para asi cumplir este requisito de un único administrador, que se controla en el servicio que no exista mas de uno. 


### Listado de usuarios

Muestra para este administrador todos los usuarios registrados. Para elaborar esto, lo que se ha hecho es crear un endpoint en el servicio para el usuario para `/registrados`, que es el que va a mostrar estos usuarios cuando redirijamos a este. Como ya hemos dicho antes, se hace en el controlador de usuario, `UsuarioController` ya que es el encargado de comunicarse con la API, y luego la plantilla `listaUsuarios.html` que es el front para mostrarlos.


### Bloqueo de usuarios

Esta funcionalidad lo que hace es bloquear, y habilitar en caso contrario, a los usuarios de la aplicación desde el usuario de administrador. 

Para hacer esto, tanto en el modelo como en los DTOs, se ha añadido un booleano `bloqueado` para saber si este lo está.
Para hacer esto en la plantilla `listaUsuarios.html` hay un botón que lee ese booleano y si esta a false muestra un botón de bloquear o habilitar en viceversa. 

Si un usuario se encuentra bloqueado, cuando intente iniciar sesión, no le va a dejar y se le indica con un mensaje que está bloqueado.

### Descripción de usuario

Esta parte se encarga de consultar la información, excepto la contraseña que no se muestra, de un usuario. Esto se hace con el endpoint siguiente:

```java
@GetMapping("/registrados/{id}")
    public String descripcion(@PathVariable Long id, Model model) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null || !usuarioService.esAdministrador(idUsuario)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos suficientes"
            );
        }

        UsuarioData usuario = usuarioService.findById(id);

        //se hace esto para que si no hay usuario se redirige a la misma ya que no hay nada que mostrar
        if (usuario == null) {
            return "redirect:/registrados";
        }

        model.addAttribute("usuario", usuario);
        return "descripcionUsuario";
    }
```
Este llama a la plantilla `descripcionUsuario.html` que se encarga de mostrar estos datos

### Protección de los accesos
El listado de usuarios, la descripción de usuario y la operación de bloqueo están protegidos para que solamente puedan ser utilizados por el administrador.

Para realizar esta comprobación se obtiene el usuario actualmente logueado mediante `ManagerUserSession` y se utiliza `UsuarioService.esAdministrador()`. Si no existe un usuario logueado o el usuario no es administrador, se devuelve un error HTTP 403.


## 3. Cambios en el modelo y servicio

La entidad `Usuario` se ha ampliado, como se ha introducido antes, los booleando de `administrador` y `bloqueado` para saber si se trata de un administrador o un usuario bloqueado en cada caso. 

Para los DTOs de `UsuarioData` y `RegistroData`, también se han añadido estos para que así puedan recibir los datos durante el registro, en concreto para el `UsuarioData` los dos atributos mientras que para el `RegistroData` solo el atributo `administrador` ya que el bloqueo no es necesario.

En el repositorio de `UsuarioRepository`, se han añadido los métodos para poder comprobar que un usuario es administrador:

```java
boolean existsByAdministradorTrue();

boolean existsByIdAndAdministradorTrue(Long id);
```
Donde el primero comprueba si existe ya un usuario administrador en la aplicación y ya luego el segundo es para ver si un usuario es administrador, donde se mira su id.

En `UsuarioService` se han implementado `existeAdministrador()` `esAdministrador(Long)`, `todosUsuarios()` y `cambiarEstadoBloqueo(Long)`, que van a llamar a los anteriores, otro va a sacar a todos los usuarios, y cambiar el estado del usuario.

```java
    @Transactional(readOnly=true)
    public List<UsuarioData> todosUsuarios() {
        List<UsuarioData> usuarios = new ArrayList<>();

        for (Usuario usuario : usuarioRepository.findAll()) {
            usuarios.add(modelMapper.map(usuario, UsuarioData.class));
        }

        return usuarios;
    }

    @Transactional(readOnly = true)
    public boolean existeAdministrador() {
        return usuarioRepository.existsByAdministradorTrue();
    }

    @Transactional(readOnly = true)
    public boolean esAdministrador(Long usuarioId) {
        return usuarioRepository.existsByIdAndAdministradorTrue(usuarioId);
    }

    @Transactional
    public void cambiarEstadoBloqueo(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);

        if (usuario != null) {
            usuario.setBloqueado(!usuario.isBloqueado());
        }
    }
```

Para el bloqueo no he tenido que añadir un método nuevo al repositorio como he hecho con el administrador ya que estaba ya el `findById()` que ya me facilitaba estos datos que buscaba.

En la función `login()` del `UsuarioService` se ha ampliado el comportamiento donde en el status que se define se ha añadido la opción `USER_BLOCKED` de la siguiento manera:

```java
    public enum LoginStatus {LOGIN_OK, USER_NOT_FOUND, ERROR_PASSWORD,USER_BLOCKED}
```

Donde se permite distinguir ahora a un usuario bloqueado de los otros estado que habia.

Una vez he modificado esto, también hay que tratar esto de nuevo en el `LoginController`:

```java
else if (loginStatus == UsuarioService.LoginStatus.USER_BLOCKED) {
            model.addAttribute("error", "El acceso de este usuario está bloqueado");
            return "formLogin";
        }
```
Donde lo que hace es añadir este mensaje de error para el acceso cuando esta bloqueado y se le pasa de nuevo a la plantilla

Además en el controlador `UsuarioController` he añadido la siguiente parte del código para cambiar el estado de bloqueo:

```java
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
```
Donde aquí se obtiene el id del usuario y se comprueba con lo que he explicado antes del `UsuarioService` si tiene estos privilegios de administrador, sino entonces va a lanzar una excepción de 403 que es la `HttpStatus.FORBIDDEN` que se lanza. La aplicación dispone de la plantilla `templates/error/403.html` para representar los errores de acceso prohibido. 

## 4. Plantillas Thymeleaf

Se ha modificado `formRegistro.html` para incluir el checkbox que permite registrarse como administrador cuando corresponde.

Se ha utilizado `listaUsuarios.html` para mostrar el listado de usuarios, los enlaces a sus descripciones y las acciones de bloqueo y habilitación.

La plantilla `descripcionUsuario.html` muestra los datos principales del usuario seleccionado y proporciona un enlace para volver al listado.

También se ha utilizado la plantilla `formLogin.html` para mostrar el mensaje correspondiente cuando un usuario bloqueado intenta iniciar sesión.

## 5. Tests

Se han ampliado los tests de `UsuarioWebTest` para comprobar las nuevas funcionalidades de la capa web.

Se comprueba que un administrador puede acceder al listado y a la
descripción de usuarios, mientras que un usuario no administrador o un usuario no autenticado recibe un error 403.

También se han añadido pruebas para el endpoint de bloqueo, verificando que un administrador puede realizar la operación y que los usuarios sin permisos no pueden hacerlo.

En `UsuarioServiceTest` se han añadido pruebas para comprobar tanto el bloqueo como la posterior habilitación de un usuario.

También se han probado los diferentes resultados del login, incluyendo el caso de un usuario bloqueado.

Dando mas detalle, los test que están implementados en `UsuarioWebTest` utilizan el `MockMvc` para introducirle los datos sin depender de una instancia real de estos.