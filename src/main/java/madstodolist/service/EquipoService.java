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
}