package co.edu.uptc.persistence;

import java.io.File;

import co.edu.uptc.controller.RouteTree;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * Implementación de {@link RouteDAO} que utiliza JAXB para la
 * serialización y deserialización de objetos {@link RouteTree}
 * en formato XML.
 * 
 * <p>Permite guardar y cargar árboles de rutas desde archivos XML,
 * manteniendo una representación estructurada y legible de los datos.</p>
 * 
 * <p>Esta clase aplica el patrón DAO (Data Access Object), separando
 * la lógica de acceso a datos de la lógica de negocio.</p>
 * 
 * @author TuNombre
 */
public class XmlRouteDAO implements RouteDAO {

    /**
     * {@inheritDoc}
     * 
     * <p>Convierte el {@link RouteTree} en XML y lo almacena en el archivo especificado.</p>
     */
    @Override
    public void guardar(RouteTree tree, String filePath) {
        try {
            JAXBContext context = JAXBContext.newInstance(RouteTree.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            marshaller.marshal(tree, new File(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Lee el contenido de un archivo XML y lo convierte nuevamente
     * en un objeto {@link RouteTree}.</p>
     */
    @Override
    public RouteTree cargar(String filePath) {
        try {
            JAXBContext context = JAXBContext.newInstance(RouteTree.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (RouteTree) unmarshaller.unmarshal(new File(filePath));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
