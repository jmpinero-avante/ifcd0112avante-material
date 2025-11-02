// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.htmlapp.model.db.User;
import com.example.htmlapp.model.db.UserRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * Servicio responsable de la autenticación y gestión de sesión.
 *
 * Este servicio se encarga de:
 *  - Iniciar sesión validando email y contraseña.
 *  - Mantener en sesión el ID del usuario autenticado.
 *  - Obtener el usuario actualmente logado.
 *  - Cerrar la sesión cuando sea necesario.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA ANOTACIÓN @Service
 * ----------------------------------------------------------------------------
 * Esta anotación indica que la clase pertenece a la capa de lógica
 * de negocio o "servicios". Spring la detecta automáticamente y la
 * registra como un bean dentro del contexto de la aplicación.
 *
 * Esto permite inyectarla en controladores u otros servicios.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA GESTIÓN DE SESIONES HTTP
 * ----------------------------------------------------------------------------
 * Este servicio usa HttpSession, un componente estándar de Java EE
 * y Spring MVC que permite almacenar datos entre peticiones HTTP.
 *
 * Cada usuario conectado tiene su propia sesión, y en ella podemos
 * guardar información temporal (como su id o nombre).
 *
 * La sesión se mantiene mientras el usuario navega por la aplicación,
 * incluso al pasar de una página a otra, y solo desaparece si:
 *   - Expira por inactividad (tras un tiempo máximo configurado).
 *   - Se cierra explícitamente con session.invalidate().
 *   - El navegador elimina las cookies de sesión.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LOS DATOS DISPONIBLES EN UNA PETICIÓN HTTP
 * ----------------------------------------------------------------------------
 * En una request HTTP podemos acceder a distintos tipos de datos:
 *
 * 1. Datos de sesión:
 *    - Son los valores almacenados dentro del objeto HttpSession.
 *    - Persisten mientras la sesión esté activa.
 *    - Se comparten entre todas las páginas del mismo usuario.
 *
 * 2. Parámetros GET:
 *    - Datos enviados en la URL (por ejemplo: ?id=3).
 *    - Se acceden con request.getParameter("id").
 *
 * 3. Parámetros POST:
 *    - Datos enviados desde un formulario HTML con method="post".
 *    - También se acceden con request.getParameter(...).
 *
 * 4. Cabeceras HTTP:
 *    - Metadatos enviados por el navegador (User-Agent, Referer...).
 *    - Se consultan con request.getHeader("User-Agent").
 *
 * 5. Cookies:
 *    - Pequeños datos almacenados en el navegador del usuario.
 *    - Se envían automáticamente en cada petición.
 *
 * 6. Cuerpo o payload:
 *    - Contenido enviado directamente en la petición (por ejemplo,
 *      JSON en peticiones AJAX o REST).
 *
 * A diferencia de los datos de sesión, todos los anteriores se
 * crean de nuevo en cada petición y no perduran entre páginas.
 *
 * La sesión es el único espacio que se mantiene durante la
 * navegación del usuario, permitiendo que el servidor "recuerde"
 * quién está autenticado o qué datos tenía activos.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

	private static final String SESSION_USER_ID = "userId";

	private final UserRepository userRepository;
	private final PasswordService passwordService;
	private final HttpSession session;

	/**
	 * Intenta iniciar sesión con el email y contraseña indicados.
	 *
	 * Si la autenticación es correcta, se guarda el id del usuario
	 * en la sesión y se devuelve true.
	 *
	 * @param email    Email del usuario.
	 * @param password Contraseña en texto plano introducida.
	 * @return true si el login es correcto, false si no.
	 */
	public boolean login(String email, String password) {
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty()) return false;

		User user = userOpt.get();

		// Utiliza verifyPassword para validar las credenciales.
		if (verifyPassword(user.getId(), password)) {
			session.setAttribute(SESSION_USER_ID, user.getId());
			return true;
		}
		return false;
	}

	/**
	 * Cierra la sesión actual.
	 */
	public void logout() {
		session.invalidate();
	}

	/**
	 * Devuelve el usuario actualmente logado (si lo hay).
	 *
	 * @return Optional<User> con el usuario logado o vacío si no hay.
	 */
	public Optional<User> getLoggedUser() {
		Object userIdObj = session.getAttribute(SESSION_USER_ID);
		if (userIdObj == null) return Optional.empty();

		Integer id = (Integer) userIdObj;
		return userRepository.findById(id);
	}

	/**
	 * Indica si hay un usuario logado actualmente.
	 *
	 * @return true si hay sesión de usuario activa.
	 */
	public boolean isLogged() {
		return session.getAttribute(SESSION_USER_ID) != null;
	}

	/**
	 * Comprueba si la contraseña introducida coincide con la registrada
	 * en la base de datos para el usuario con el ID especificado.
	 *
	 * Este método no tiene efectos secundarios (no inicia sesión ni
	 * modifica el estado de HttpSession).
	 *
	 * @param id          ID del usuario que se desea verificar.
	 * @param rawPassword Contraseña en texto plano introducida.
	 * @return true si la contraseña coincide; false si no coincide o
	 *         el usuario no existe.
	 */
	public boolean verifyPassword(int id, String rawPassword) {
		return userRepository.findById(id)
			.map(user -> passwordService.verifyPassword(
				rawPassword,
				user.getSalt(),
				user.getPasswordHash()
			))
			.orElse(false);
	}
}

/*
 * ----------------------------------------------------------------------------
 * SOBRE EL USO DE @RequiredArgsConstructor Y LA INYECCIÓN DE DEPENDENCIAS
 * ----------------------------------------------------------------------------
 *
 * @RequiredArgsConstructor es una anotación de Lombok que genera de forma
 * automática un constructor con todos los campos declarados como final.
 *
 * En este caso, el constructor generado sería equivalente a:
 *
 *   public AuthService(UserRepository userRepository,
 *                      PasswordService passwordService,
 *                      HttpSession session) { ... }
 *
 * No es necesario añadir @Autowired al constructor generado, porque desde
 * Spring 4.3 el framework detecta automáticamente el constructor único
 * de la clase y lo utiliza para realizar la inyección de dependencias.
 *
 * Solo sería necesario añadir @Autowired si hubiera varios constructores
 * o si se usara una versión antigua de Spring (anterior a 4.3).
 *
 * ----------------------------------------------------------------------------
 * DIFERENCIAS ENTRE LOS TIPOS DE INYECCIÓN
 * ----------------------------------------------------------------------------
 *
 * 1. Inyección por campo:
 *      @Autowired
 *      private UserRepository repo;
 *    - Simple, pero los campos no pueden ser final.
 *    - Menos adecuada para testing o inyección manual.
 *
 * 2. Inyección por constructor:
 *      @Autowired
 *      public AuthService(UserRepository repo, PasswordService ps) { ... }
 *    - Más limpia, explícita y segura (permite campos final).
 *
 * 3. Con Lombok (@RequiredArgsConstructor):
 *    - No hace falta escribir el constructor ni usar @Autowired.
 *    - Combina claridad y buenas prácticas con menor código.
 *
 * Esta forma es la recomendada actualmente: limpia, inmutable
 * y totalmente compatible con el sistema de inyección de Spring.
 *
 * ----------------------------------------------------------------------------
 * SOBRE verifyPassword(int, String)
 * ----------------------------------------------------------------------------
 *
 * Este método realiza una comprobación directa de contraseña
 * sin efectos secundarios, ideal para validar la contraseña actual
 * antes de permitir un cambio de contraseña o una operación sensible.
 *
 * Internamente busca al usuario por su ID en la base de datos y
 * compara el hash resultante del texto plano con el hash almacenado.
 *
 * A diferencia de login(), no modifica la sesión ni crea variables
 * en HttpSession, garantizando que solo actúa como verificador puro.
 */