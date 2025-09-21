package co.edu.uptc.persistence;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import co.edu.uptc.controller.RouteTree;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * Implementación de {@link RouteDAO} que utiliza JAXB para la
 * serialización y deserialización de objetos {@link RouteTree}
 * en formato XML.
 */
public class XmlRouteDAO implements RouteDAO {

    private static final Logger LOGGER = Logger.getLogger(XmlRouteDAO.class.getName());

    @Override
    public void save(RouteTree tree, String filePath) {
        try {
            if (tree == null) {
                throw new PersistenceException("RouteTree nulo al intentar guardar.");
            }
            JAXBContext context = JAXBContext.newInstance(RouteTree.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File out = new File(filePath);
            // Asegurar directorio padre exista
            File parent = out.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            marshaller.marshal(tree, out);
        } catch (PersistenceException p) {
            throw p;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error guardando RouteTree en XML: " + e.getMessage(), e);
            throw new PersistenceException("Error guardando RouteTree en XML: " + e.getMessage(), e);
        }
    }

    @Override
    public RouteTree load(String filePath) {
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                // No consideramos esto una excepción crítica: retornamos null para indicar "no hay datos".
                LOGGER.log(Level.INFO, "Archivo de persistencia no existe: " + filePath);
                return null;
            }

            JAXBContext context = JAXBContext.newInstance(RouteTree.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (RouteTree) unmarshaller.unmarshal(f);
        } catch (PersistenceException p) {
            throw p;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error cargando RouteTree desde XML: " + e.getMessage(), e);
            throw new PersistenceException("Error cargando RouteTree desde XML: " + e.getMessage(), e);
        }
    }
}