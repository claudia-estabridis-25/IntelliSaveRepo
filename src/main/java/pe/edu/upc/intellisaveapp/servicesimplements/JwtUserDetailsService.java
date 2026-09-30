package pe.edu.upc.intellisaveapp.servicesimplements;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.entities.Users;
import pe.edu.upc.intellisaveapp.repositories.IRoleRepository;
import pe.edu.upc.intellisaveapp.repositories.IUsersRepository;

import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    private final IUsersRepository usersRepository;
    private final IRoleRepository roleRepository;

    public JwtUserDetailsService(IUsersRepository usersRepository, IRoleRepository roleRepository) {
        this.usersRepository = usersRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String emailUser)
            throws UsernameNotFoundException {

        Users user = usersRepository.findByEmailUser(emailUser)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado: " + emailUser
                        )
                );

        List<GrantedAuthority> authorities = roleRepository.findByUser_IdUser(user.getIdUser())
                .stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.getNameRole()))
                .toList();

        return User.builder()
                .username(user.getEmailUser())
                .password(user.getPasswordUser())
                .authorities(authorities)
                .disabled(!Boolean.TRUE.equals(user.getStatusUser()))
                .build();
    }
}
