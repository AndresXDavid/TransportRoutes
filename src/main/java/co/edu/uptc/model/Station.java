package co.edu.uptc.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.Objects;

/**
 * Representa una estación en el sistema de rutas.
 * <p>
 * Esta clase es usada como contenido principal de {@link Node}.
 * Está preparada para (un)marshalling con JAXB (Jakarta XML Bind).
 * </p>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Station {

    @XmlElement(required = true)
    private String code;

    @XmlElement(required = true)
    private String location;

    /**
     * Constructor sin-args requerido por JAXB.
     */
    public Station() {
    }

    /**
     * Crea una estación con código y ubicación.
     *
     * @param code     código único de la estación (p. ej. "CEN")
     * @param location nombre o descripción de la estación (p. ej. "Centro")
     */
    public Station(String code, String location) {
        this.code = code;
        this.location = location;
    }

    /**
     * Devuelve el código de la estación.
     *
     * @return código único
     */
    public String getCode() {
        return code;
    }

    /**
     * Establece el código de la estación.
     *
     * @param code código único (no nulo preferiblemente)
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Devuelve la ubicación / nombre de la estación.
     *
     * @return ubicación
     */
    public String getLocation() {
        return location;
    }

    /**
     * Establece la ubicación / nombre de la estación.
     *
     * @param location nombre o descripción
     */
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return (location == null ? "" : location) + (code == null ? "" : " (" + code + ")");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Station station = (Station) o;
        // Se asume que 'code' identifica de forma única la estación
        return Objects.equals(code, station.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}