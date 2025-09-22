package co.edu.uptc.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.ConsoleHandler;
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
 *
 * Características:
 * - try-with-resources para manejo seguro de streams.
 * - Backup automático antes de sobrescribir.
 * - Uso explícito de UTF-8.
 * - Logger configurado para no saturar la consola.
 */
public class XmlRouteDAO implements RouteDAO {

    private static final Logger LOGGER = Logger.getLogger(XmlRouteDAO.class.getName());

    // Configuración del logger: solo WARNING y SEVERE se mostrarán
    static {
        LOGGER.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.WARNING);
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.WARNING);
    }

    @Override
    public void save(RouteTree tree, String filePath) {
        if (tree == null) {
            throw new PersistenceException("RouteTree nulo al intentar guardar.");
        }

        File out = new File(filePath);
        File parent = out.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        // Backup si existe archivo previo
        if (out.exists()) {
            File backup = new File(filePath + ".bak");
            try {
                Files.copy(out.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                LOGGER.log(Level.INFO, "Backup creado en: " + backup.getAbsolutePath());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "No se pudo crear backup del archivo: " + filePath, e);
            }
        }

        try {
            JAXBContext context = JAXBContext.newInstance(RouteTree.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            try (FileOutputStream fos = new FileOutputStream(out);
                 OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                marshaller.marshal(tree, writer);
            }

            LOGGER.log(Level.INFO, "RouteTree guardado exitosamente en: " + filePath);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error guardando RouteTree en XML: " + e.getMessage(), e);
            throw new PersistenceException("Error guardando RouteTree en XML: " + e.getMessage(), e);
        }
    }

    @Override
    public RouteTree load(String filePath) {
        File f = new File(filePath);
        if (!f.exists()) {
            LOGGER.log(Level.INFO, "Archivo de persistencia no existe: " + filePath);
            return null;
        }

        try {
            JAXBContext context = JAXBContext.newInstance(RouteTree.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            try (FileInputStream fis = new FileInputStream(f)) {
                RouteTree tree = (RouteTree) unmarshaller.unmarshal(fis);
                LOGGER.log(Level.INFO, "RouteTree cargado exitosamente desde: " + filePath);
                return tree;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error cargando RouteTree desde XML: " + e.getMessage(), e);
            throw new PersistenceException("Error cargando RouteTree desde XML: " + e.getMessage(), e);
        }
    }
}