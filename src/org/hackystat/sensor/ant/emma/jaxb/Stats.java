//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.01.25 at 09:11:53 AM GMT-10:00 
//


package org.hackystat.sensor.ant.emma.jaxb;

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
 *         &lt;element ref="{}packages"/>
 *         &lt;element ref="{}classes"/>
 *         &lt;element ref="{}methods"/>
 *         &lt;element ref="{}srcfiles"/>
 *         &lt;element ref="{}srclines"/>
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
    "classes",
    "methods",
    "srcfiles",
    "srclines"
})
@XmlRootElement(name = "stats")
public class Stats {

    @XmlElement(required = true)
    protected Packages packages;
    @XmlElement(required = true)
    protected Classes classes;
    @XmlElement(required = true)
    protected Methods methods;
    @XmlElement(required = true)
    protected Srcfiles srcfiles;
    @XmlElement(required = true)
    protected Srclines srclines;

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
     * Gets the value of the classes property.
     * 
     * @return
     *     possible object is
     *     {@link Classes }
     *     
     */
    public Classes getClasses() {
        return classes;
    }

    /**
     * Sets the value of the classes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Classes }
     *     
     */
    public void setClasses(Classes value) {
        this.classes = value;
    }

    /**
     * Gets the value of the methods property.
     * 
     * @return
     *     possible object is
     *     {@link Methods }
     *     
     */
    public Methods getMethods() {
        return methods;
    }

    /**
     * Sets the value of the methods property.
     * 
     * @param value
     *     allowed object is
     *     {@link Methods }
     *     
     */
    public void setMethods(Methods value) {
        this.methods = value;
    }

    /**
     * Gets the value of the srcfiles property.
     * 
     * @return
     *     possible object is
     *     {@link Srcfiles }
     *     
     */
    public Srcfiles getSrcfiles() {
        return srcfiles;
    }

    /**
     * Sets the value of the srcfiles property.
     * 
     * @param value
     *     allowed object is
     *     {@link Srcfiles }
     *     
     */
    public void setSrcfiles(Srcfiles value) {
        this.srcfiles = value;
    }

    /**
     * Gets the value of the srclines property.
     * 
     * @return
     *     possible object is
     *     {@link Srclines }
     *     
     */
    public Srclines getSrclines() {
        return srclines;
    }

    /**
     * Sets the value of the srclines property.
     * 
     * @param value
     *     allowed object is
     *     {@link Srclines }
     *     
     */
    public void setSrclines(Srclines value) {
        this.srclines = value;
    }

}
