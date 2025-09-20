package co.edu.uptc.persistence;

import java.io.File;

import co.edu.uptc.controller.RouteTree;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

public class RoutePersistence {
    public void guardar(RouteTree tree, String filePath) {
        try {
            JAXBContext context = JAXBContext.newInstance(RouteTree.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // bonito

            marshaller.marshal(tree, new File(filePath)); // Guardar en archivo
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
