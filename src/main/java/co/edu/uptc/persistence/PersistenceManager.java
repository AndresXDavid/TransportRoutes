package co.edu.uptc.persistence;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase Singleton que administra la instancia de {@link RouteDAO}.
 *
 * <p>Encapsula la lógica para obtener la implementación concreta de DAO,
 * permitiendo cambiar entre diferentes estrategias de persistencia
 * (por ejemplo, XML, CSV o mocks en pruebas unitarias).</p>
 *
 * <p>Por defecto, utiliza {@link XmlRouteDAO}, pero puede inyectarse
 * otra implementación con {@link #setRouteDAO(RouteDAO)}.</p>
 */
public class PersistenceManager {

    private static final Logger LOGGER = Logger.getLogger(PersistenceManager.class.getName());

    /** Instancia única del Singleton. */
    private static PersistenceManager instance;

    /** Objeto de acceso a datos actual. */
    private RouteDAO routeDAO;

    /**
     * Constructor privado para evitar instanciación externa.
     *
     * <p>Por defecto, se inicializa con {@link XmlRouteDAO}.</p>
     */
    private PersistenceManager() {
        try {
            this.routeDAO = new XmlRouteDAO();
        } catch (Exception e) {
            // Si XmlRouteDAO falla en la carga, registramos y dejamos routeDAO en null
            LOGGER.log(Level.SEVERE, "Error inicializando XmlRouteDAO: " + e.getMessage(), e);
            this.routeDAO = null;
        }
    }

    /**
     * Obtiene la instancia única de {@link PersistenceManager}.
     *
     * @return Instancia Singleton de {@link PersistenceManager}.
     */
    public static synchronized PersistenceManager getInstance() {
        if (instance == null) {
            instance = new PersistenceManager();
        }
        return instance;
    }

    /**
     * Obtiene el {@link RouteDAO} configurado actualmente.
     *
     * @return Instancia de {@link RouteDAO} o {@code null} si no hay una implementada.
     */
    public RouteDAO getRouteDAO() {
        return routeDAO;
    }

    /**
     * Establece un nuevo {@link RouteDAO}.
     *
     * <p>Este método se usa principalmente para pruebas unitarias,
     * permitiendo inyectar un mock o una implementación diferente.</p>
     *
     * @param routeDAO Nueva implementación de {@link RouteDAO}.
     */
    public void setRouteDAO(RouteDAO routeDAO) {
        this.routeDAO = routeDAO;
    }
}