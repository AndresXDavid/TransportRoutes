package co.edu.uptc.persistence;

/**
 * Excepción personalizada para errores de persistencia.
 * <p>
 * Se usa para encapsular errores ocurridos en DAO (I/O, JAXB, permisos, etc.).
 * </p>
 */
public class PersistenceException extends RuntimeException {

     public PersistenceException(String message) {
          super(message);
     }

     public PersistenceException(String message, Throwable cause) {
          super(message, cause);
     }
}