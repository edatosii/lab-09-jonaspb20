package co.umariana.servlets;

import co.umariana.dao.EstudianteDAO;
import co.umariana.modelo.Estudiante;
import java.util.List;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controlador del CRUD de estudiantes: recibe las peticiones HTTP, usa el DAO
 * y decide qué vista mostrar.
 *
 * CONTRATO (las pruebas lo verifican; respete nombres y rutas exactos)
 * --------------------------------------------------------------------
 * GET /estudiantes
 *   (sin accion)  o  accion=listar   atributo "estudiantes" = dao.listar()
 *                                    forward a /WEB-INF/vistas/listar.jsp
 *   accion=nuevo                     forward a /WEB-INF/vistas/formulario.jsp
 *   accion=editar&id=N               atributo "estudiante" = el estudiante encontrado
 *                                    forward a /WEB-INF/vistas/formulario.jsp
 *                                    si el id no existe o no es un número: sendError(404)
 *   cualquier otra accion            sendError(400)
 *
 * POST /estudiantes   (primero: request.setCharacterEncoding("UTF-8"))
 *   accion=crear        parámetros: nombre, correo, semestre
 *   accion=actualizar   parámetros: id, nombre, correo, semestre
 *   accion=eliminar     parámetro:  id
 *   cualquier otra accion            sendError(400)
 *
 *   crear:       1) mensaje = validar(nombre, correo, semestre)
 *                2) si hay mensaje: setAttribute("error", mensaje) y forward a formulario.jsp
 *                3) dao.registrar(...); si devuelve false (correo repetido):
 *                   setAttribute("error", <mensaje>) y forward a formulario.jsp
 *                4) si todo sale bien: response.sendRedirect("estudiantes")
 *   actualizar:  igual que crear, pero ANTES de validar: si el id no es un número o
 *                no existe (dao.buscarPorId devuelve null): sendError(404)
 *   eliminar:    si el id no es un número o dao.eliminar devuelve false: sendError(404);
 *                si no: sendRedirect("estudiantes")
 *
 * El nombre y el correo se guardan sin espacios al inicio/final (trim()).
 *
 * YA están resueltos (úselos como modelo): GET accion=nuevo y POST accion=crear.
 *
 * INSTRUCCIONES: complete los "TODO". NO modifique las pruebas.
 */
@WebServlet("/estudiantes")
public class EstudianteServlet extends HttpServlet {

    static final String VISTA_LISTAR = "/WEB-INF/vistas/listar.jsp";
    static final String VISTA_FORMULARIO = "/WEB-INF/vistas/formulario.jsp";

    private final EstudianteDAO dao = new EstudianteDAO();

    /**
     * YA está hecho: valida los datos del formulario.
     *
     * @return el mensaje de error, o null si los datos son válidos
     */
    static String validar(String nombre, String correo, String semestre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre es obligatorio";
        }
        if (correo == null || !correo.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "El correo no es válido";
        }
        try {
            int s = Integer.parseInt(semestre.trim());
            if (s < 1 || s > 10) {
                return "El semestre debe ser un número entre 1 y 10";
            }
        } catch (NumberFormatException | NullPointerException e) {
            return "El semestre debe ser un número entre 1 y 10";
        }
        return null;
    }

    // =====================================================================
    // ETAPA 6. Mostrar (GET)
    // =====================================================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");

        // YA RESUELTO: "nuevo" muestra el formulario vacío.
        if ("nuevo".equals(accion)) {
            request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
            return;
        }

        if (accion == null || "listar".equals(accion)) {
            request.setAttribute("estudiantes", dao.listar());
            request.getRequestDispatcher(VISTA_LISTAR).forward(request, response);
            return;
        }

        if ("editar".equals(accion)) {
            int id;
            try {
                id = Integer.parseInt(request.getParameter("id"));
            } catch (NumberFormatException | NullPointerException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Estudiante estudiante = dao.buscarPorId(id);
            if (estudiante == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("estudiante", estudiante);
            request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    // =====================================================================
    // ETAPA 7. Crear, actualizar y eliminar (POST)
    // =====================================================================

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        // YA RESUELTO: "crear" (úselo como modelo para "actualizar").
        if ("crear".equals(accion)) {
            String nombre = request.getParameter("nombre");
            String correo = request.getParameter("correo");
            String semestre = request.getParameter("semestre");

            String error = validar(nombre, correo, semestre);
            if (error == null) {
                Estudiante estudiante = new Estudiante(0, nombre.trim(), correo.trim(),
                                                       Integer.parseInt(semestre.trim()));
                if (dao.registrar(estudiante)) {
                    response.sendRedirect("estudiantes");
                    return;
                }
                error = "No se pudo guardar el estudiante. Verifique que el correo no esté repetido.";
            }
            request.setAttribute("error", error);
            request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
            return;
        }

        if ("actualizar".equals(accion)) {
            int id;
            try {
                id = Integer.parseInt(request.getParameter("id"));
            } catch (NumberFormatException | NullPointerException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (dao.buscarPorId(id) == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String nombre = request.getParameter("nombre");
            String correo = request.getParameter("correo");
            String semestre = request.getParameter("semestre");
            String error = validar(nombre, correo, semestre);
            if (error != null) {
                request.setAttribute("error", error);
                request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
                return;
            }

            Estudiante estudiante = new Estudiante(
                id, nombre.trim(), correo.trim(), Integer.parseInt(semestre.trim())
            );
            if (dao.actualizar(estudiante)) {
                response.sendRedirect("estudiantes");
                return;
            }
            request.setAttribute("error", "No se pudo actualizar el estudiante. Verifique que el correo no esté repetido.");
            request.setAttribute("estudiante", estudiante);
            request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
            return;
        }

        if ("eliminar".equals(accion)) {
            int id;
            try {
                id = Integer.parseInt(request.getParameter("id"));
            } catch (NumberFormatException | NullPointerException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (!dao.eliminar(id)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.sendRedirect("estudiantes");
            return;
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
}
