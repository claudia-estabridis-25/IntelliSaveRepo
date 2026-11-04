package pe.edu.upc.intellisaveapp.securities;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pe.edu.upc.intellisaveapp.entities.Users;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IUsersService;

@Component
public class CompanyAccess {
    private final IUsersService uS;

    public CompanyAccess(IUsersService uS) {
        this.uS = uS;
    }

    //Este metodo devuelve el id de la empresa del usuario que inició sesión, o null si es el admin de la plataforma
    public Long idEmpresaUsuario(){
        //Obteniendo los datos del usuario autenticado (lo saca de su token)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        //Buscando el correo del usuario autenticado para identificarlo en la base de datos y obtener su empresa
        Users usuario = uS.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("No se pudo identificar al usuario autenticado"));

        //Verificando si el usuario tiene un área asignada, si no la tiene y es admin de la plataforma entonces
        //devuelve null; si no es admin lanza una excepción (403 Forbidden)
        if (usuario.getDepartment() == null) {
            boolean esAdmin = auth.getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (esAdmin) {
                return null; //El admin no tiene restricciones de empresa, por eso devuelve null
            }
            throw new AccessDeniedException("El usuario no tiene un área asignada");
        }

        //Si el usuario sí tiene área, sube por las relaciones hasta la empresa
        return usuario.getDepartment().getBranch().getCompany().getIdCompany();
    }

    //Validando que el id de empresa asociado el usuario que hace la consulta, sea igual al id de la empresa que
    //está consultando, es decir, que pertenezca a la empresa. Si no es igual lanza una excepción (403 Forbidden).
    public void validarEmpresa(Long idCompany) {
        Long idEmpresa = idEmpresaUsuario();

        if (idEmpresa != null && !idEmpresa.equals(idCompany)) {
            throw new AccessDeniedException("No tiene acceso a la información de otra empresa");
        }
    }

    //Metodo para dejar pasar solo al admin de la plataforma, si el usuario autenticado tiene
    //un id de empresa asociado, significa que no es admin y se le prohíbe el acceso. Este metodo se utiliza en
    //registrar y eliminar empresas, ya que solo el admin de la plataforma puede realizar estas acciones.
    public void validarAdminDePlataforma() {
        if (idEmpresaUsuario() != null) {
            throw new AccessDeniedException("Solo el administrador de la plataforma puede realizar esta acción");
        }
    }

}
