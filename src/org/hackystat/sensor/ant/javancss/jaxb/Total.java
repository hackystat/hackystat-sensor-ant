//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.01.25 at 09:11:56 AM GMT-10:00 
//


package org.hackystat.sensor.ant.javancss.jaxb;

import java.math.BigInteger;
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
 *         &lt;element name="classes" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="functions" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="ncss" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="javadocs" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="javadoc_lines" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="single_comment_lines" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="multi_comment_lines" type="{http://www.w3.org/2001/XMLSchema}integer"/>
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
    "classes",
    "functions",
    "ncss",
    "javadocs",
    "javadocLines",
    "singleCommentLines",
    "multiCommentLines"
})
@XmlRootElement(name = "total")
public class Total {

    @XmlElement(required = true)
    protected BigInteger classes;
    @XmlElement(required = true)
    protected BigInteger functions;
    @XmlElement(required = true)
    protected String ncss;
    @XmlElement(required = true)
    protected BigInteger javadocs;
    @XmlElement(name = "javadoc_lines", required = true)
    protected BigInteger javadocLines;
    @XmlElement(name = "single_comment_lines", required = true)
    protected BigInteger singleCommentLines;
    @XmlElement(name = "multi_comment_lines", required = true)
    protected BigInteger multiCommentLines;

    /**
     * Gets the value of the classes property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getClasses() {
        return classes;
    }

    /**
     * Sets the value of the classes property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setClasses(BigInteger value) {
        this.classes = value;
    }

    /**
     * Gets the value of the functions property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFunctions() {
        return functions;
    }

    /**
     * Sets the value of the functions property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setFunctions(BigInteger value) {
        this.functions = value;
    }

    /**
     * Gets the value of the ncss property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNcss() {
        return ncss;
    }

    /**
     * Sets the value of the ncss property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNcss(String value) {
        this.ncss = value;
    }

    /**
     * Gets the value of the javadocs property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getJavadocs() {
        return javadocs;
    }

    /**
     * Sets the value of the javadocs property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setJavadocs(BigInteger value) {
        this.javadocs = value;
    }

    /**
     * Gets the value of the javadocLines property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getJavadocLines() {
        return javadocLines;
    }

    /**
     * Sets the value of the javadocLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setJavadocLines(BigInteger value) {
        this.javadocLines = value;
    }

    /**
     * Gets the value of the singleCommentLines property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSingleCommentLines() {
        return singleCommentLines;
    }

    /**
     * Sets the value of the singleCommentLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSingleCommentLines(BigInteger value) {
        this.singleCommentLines = value;
    }

    /**
     * Gets the value of the multiCommentLines property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMultiCommentLines() {
        return multiCommentLines;
    }

    /**
     * Sets the value of the multiCommentLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMultiCommentLines(BigInteger value) {
        this.multiCommentLines = value;
    }

}
