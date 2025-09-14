package co.edu.uptc.model;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlAccessType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Station {
    @XmlElement
    private String code;
    @XmlElement
    private String location;

    public Station() {}

    public Station(String code, String location) {
        this.code = code;
        this.location = location;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }    
}
