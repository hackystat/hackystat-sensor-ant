//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.03.20 at 10:53:57 AM GMT-10:00 
//


package org.hackystat.sensor.ant.jdepend.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}Packages"/>
 *         &lt;element ref="{}Cycles"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "packages",
    "cycles"
})
@XmlRootElement(name = "JDepend")
public class JDepend {

    @XmlElement(name = "Packages", required = true)
    protected Packages packages;
    @XmlElement(name = "Cycles", required = true)
    protected Cycles cycles;

    /**
     * Gets the value of the packages property.
     * 
     * @return
     *     possible object is
     *     {@link Packages }
     *     
     */
    public Packages getPackages() {
        return packages;
    }

    /**
     * Sets the value of the packages property.
     * 
     * @param value
     *     allowed object is
     *     {@link Packages }
     *     
     */
    public void setPackages(Packages value) {
        this.packages = value;
    }

    /**
     * Gets the value of the cycles property.
     * 
     * @return
     *     possible object is
     *     {@link Cycles }
     *     
     */
    public Cycles getCycles() {
        return cycles;
    }

    /**
     * Sets the value of the cycles property.
     * 
     * @param value
     *     allowed object is
     *     {@link Cycles }
     *     
     */
    public void setCycles(Cycles value) {
        this.cycles = value;
    }

}
