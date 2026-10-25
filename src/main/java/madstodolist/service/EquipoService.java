package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EquipoService {

    @Autowired
    EquipoRepository equipoRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public EquipoData crearEquipo(String nombre) {
        //si esta vacio o es nulo lanzamos la excepcion
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new EquipoServiceException("El equipo no tiene nombre");
        }

        //si ya hay un equipo con el mismo nombre ya no deja
        boolean existe = equipoRepository.findAll().stream().anyMatch(equipo -> equipo.getNombre().equalsIgnoreCase(nombre.trim()));
        if (existe) {
            throw new EquipoServiceException("Ya existe un equipo con el nombre " + nombre.trim());
        }

        Equipo equipoNuevo = new Equipo(nombre);
        equipoNuevo = equipoRepository.save(equipoNuevo);
        return modelMapper.map(equipoNuevo, EquipoData.class);
    }

    @Transactional(readOnly = true)
    public EquipoData recuperarEquipo(Long id) {
        //si no existe el equipo se lanza la excepcion
        Equipo equipo = equipoRepository.findById(id).orElseThrow(() -> new EquipoServiceException("No existe el equipo con id " + id));
        return modelMapper.map(equipo, EquipoData.class);
    }

    @Transactional(readOnly = true)
    public List<EquipoData> findAllOrdenadoPorNombre() {
        List<EquipoData> equiposData = new ArrayList<>();

        //itero los equipos y los guardo en la lista aux, luego los ordeno por el nombre
        for (Equipo equipo : equipoRepository.findAll()) {
            equiposData.add(modelMapper.map(equipo, EquipoData.class));
        }
        equiposData.sort(Comparator.comparing(EquipoData::getNombre));
        return equiposData;
    }

    @Transactional
    public void añadirUsuarioAEquipo(Long id, Long id1) {
        //igual, si no existen el equipo o el usuario se lanza excepcion
        Equipo equipo = equipoRepository.findById(id).orElseThrow(() -> new EquipoServiceException("No existe el equipo con id " + id));
        Usuario usuario = usuarioRepository.findById(id1).orElseThrow(() -> new EquipoServiceException("No existe el usuario con id " + id1));
        //si ya esta en el equipo no se vuelve a añadir
        if (equipo.getUsuarios().contains(usuario)) {
            throw new EquipoServiceException("El usuario ya pertenece al equipo");
        }
        equipo.addUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElseThrow(() -> new EquipoServiceException("No existe el equipo con id " + id));
        // Hacemos uso de Java Stream API para mapear la lista de entidades a DTOs.
        return equipo.getUsuarios().stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    //igual que el de arriba pero se hace al reves, de usuario a equipos
    public List<EquipoData> equiposUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new EquipoServiceException("No existe el usuario con id " + id));
        if (usuario == null) {
            return new ArrayList<>();
        }
        //una vez tengo el usuario se busca en los equipos de ese usuario con el metodo de Usuario
        List<EquipoData> equiposData = new ArrayList<>();
        for (Equipo equipo : usuario.getEquipos()) {
            equiposData.add(modelMapper.map(equipo, EquipoData.class));
        }
        //se ordena para que salga igual que en el otro
        equiposData.sort(Comparator.comparing(EquipoData::getNombre));

        return equiposData;
    }

    @Transactional
    public void eliminarUsuarioDeEquipo(Long id, Long id1) {
        Equipo equipo = equipoRepository.findById(id).orElseThrow(() -> new EquipoServiceException("No existe el equipo con id " + id));
        Usuario usuario = usuarioRepository.findById(id1).orElseThrow(() -> new EquipoServiceException("No existe el usuario con id " + id1));
        //no tiene pq el usuario pertenecer a ese equipo
        if (!equipo.getUsuarios().contains(usuario)) {
            throw new EquipoServiceException("El usuario no pertenece al equipo");
        }
        //al ser una relacion muchos a muchos tengo que actualizar las dos partes de la relacion
        equipo.getUsuarios().remove(usuario);
        usuario.getEquipos().remove(equipo);
        equipoRepository.save(equipo);
    }

    @Transactional
    public EquipoData renombrarEquipo(Long id, String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            throw new EquipoServiceException("El equipo no tiene nombre");
        }
        //si no existe ese equipo se lanza excepcion
        Equipo equipo = equipoRepository.findById(id).orElseThrow(() -> new EquipoServiceException("No existe el equipo con id " + id));
        String nombre = nuevoNombre.trim();
        //si ya existe no cambiamos el nombre ya que no puede haber duplicados
        boolean existe = equipoRepository.findAll().stream().anyMatch(e -> !e.getId().equals(id) && e.getNombre().equalsIgnoreCase(nombre));
        if (existe) {
            throw new EquipoServiceException("Ya existe un equipo con el nombre " + nombre);
        }
        equipo.setNombre(nombre);
        equipoRepository.save(equipo);
        return modelMapper.map(equipo, EquipoData.class);
    }
}