//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.12.18 at 12:08:22 PM GMT-10:00 
//


package org.hackystat.sensor.ant.clover.resource.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *       &lt;attribute name="classes" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="methods" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="conditionals" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="ncloc" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="coveredstatements" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="coveredmethods" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="coveredconditionals" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="statements" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="loc" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="coveredelements" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="elements" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "metrics")
public class Metrics {

    @XmlAttribute
    protected Integer classes;
    @XmlAttribute
    protected Integer methods;
    @XmlAttribute
    protected Integer conditionals;
    @XmlAttribute
    protected Integer ncloc;
    @XmlAttribute
    protected Integer coveredstatements;
    @XmlAttribute
    protected Integer coveredmethods;
    @XmlAttribute
    protected Integer coveredconditionals;
    @XmlAttribute
    protected Integer statements;
    @XmlAttribute
    protected Integer loc;
    @XmlAttribute
    protected Integer coveredelements;
    @XmlAttribute
    protected Integer elements;

    /**
     * Gets the value of the classes property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getClasses() {
        return classes;
    }

    /**
     * Sets the value of the classes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setClasses(Integer value) {
        this.classes = value;
    }

    /**
     * Gets the value of the methods property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMethods() {
        return methods;
    }

    /**
     * Sets the value of the methods property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMethods(Integer value) {
        this.methods = value;
    }

    /**
     * Gets the value of the conditionals property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getConditionals() {
        return conditionals;
    }

    /**
     * Sets the value of the conditionals property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setConditionals(Integer value) {
        this.conditionals = value;
    }

    /**
     * Gets the value of the ncloc property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNcloc() {
        return ncloc;
    }

    /**
     * Sets the value of the ncloc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNcloc(Integer value) {
        this.ncloc = value;
    }

    /**
     * Gets the value of the coveredstatements property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCoveredstatements() {
        return coveredstatements;
    }

    /**
     * Sets the value of the coveredstatements property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCoveredstatements(Integer value) {
        this.coveredstatements = value;
    }

    /**
     * Gets the value of the coveredmethods property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCoveredmethods() {
        return coveredmethods;
    }

    /**
     * Sets the value of the coveredmethods property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCoveredmethods(Integer value) {
        this.coveredmethods = value;
    }

    /**
     * Gets the value of the coveredconditionals property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCoveredconditionals() {
        return coveredconditionals;
    }

    /**
     * Sets the value of the coveredconditionals property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCoveredconditionals(Integer value) {
        this.coveredconditionals = value;
    }

    /**
     * Gets the value of the statements property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getStatements() {
        return statements;
    }

    /**
     * Sets the value of the statements property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setStatements(Integer value) {
        this.statements = value;
    }

    /**
     * Gets the value of the loc property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLoc() {
        return loc;
    }

    /**
     * Sets the value of the loc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLoc(Integer value) {
        this.loc = value;
    }

    /**
     * Gets the value of the coveredelements property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCoveredelements() {
        return coveredelements;
    }

    /**
     * Sets the value of the coveredelements property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCoveredelements(Integer value) {
        this.coveredelements = value;
    }

    /**
     * Gets the value of the elements property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getElements() {
        return elements;
    }

    /**
     * Sets the value of the elements property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setElements(Integer value) {
        this.elements = value;
    }

}
