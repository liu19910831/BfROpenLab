//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// �nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.08.31 um 01:45:20 PM CEST 
//


package de.nrw.verbraucherschutz.idv.daten;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f�r warenumfang complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="warenumfang">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="anzahlGebinde" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}anzahlGebinde"/>
 *         &lt;element name="mengeEinheit" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}mengeEinheit"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "warenumfang", propOrder = {
    "anzahlGebinde",
    "mengeEinheit"
})
public class Warenumfang {

    @XmlElement(required = true, nillable = true)
    protected AnzahlGebinde anzahlGebinde;
    @XmlElement(required = true, nillable = true)
    protected MengeEinheit mengeEinheit;

    /**
     * Ruft den Wert der anzahlGebinde-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AnzahlGebinde }
     *     
     */
    public AnzahlGebinde getAnzahlGebinde() {
        return anzahlGebinde;
    }

    /**
     * Legt den Wert der anzahlGebinde-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AnzahlGebinde }
     *     
     */
    public void setAnzahlGebinde(AnzahlGebinde value) {
        this.anzahlGebinde = value;
    }

    /**
     * Ruft den Wert der mengeEinheit-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MengeEinheit }
     *     
     */
    public MengeEinheit getMengeEinheit() {
        return mengeEinheit;
    }

    /**
     * Legt den Wert der mengeEinheit-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MengeEinheit }
     *     
     */
    public void setMengeEinheit(MengeEinheit value) {
        this.mengeEinheit = value;
    }

}