//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.03.24 at 05:35:12 PM HST 
//


package org.hackystat.sensor.ant.jdepend.jaxb;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.hackystat.sensor.ant.jdepend.jaxb package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.hackystat.sensor.ant.jdepend.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JDepend }
     * 
     */
    public JDepend createJDepend() {
        return new JDepend();
    }

    /**
     * Create an instance of {@link Stats }
     * 
     */
    public Stats createStats() {
        return new Stats();
    }

    /**
     * Create an instance of {@link DependsUpon }
     * 
     */
    public DependsUpon createDependsUpon() {
        return new DependsUpon();
    }

    /**
     * Create an instance of {@link Cycles.Package }
     * 
     */
    public Cycles.Package createCyclesPackage() {
        return new Cycles.Package();
    }

    /**
     * Create an instance of {@link Class }
     * 
     */
    public Class createClass() {
        return new Class();
    }

    /**
     * Create an instance of {@link org.hackystat.sensor.ant.jdepend.jaxb.Package }
     * 
     */
    public org.hackystat.sensor.ant.jdepend.jaxb.Package createPackage() {
        return new org.hackystat.sensor.ant.jdepend.jaxb.Package();
    }

    /**
     * Create an instance of {@link Cycles }
     * 
     */
    public Cycles createCycles() {
        return new Cycles();
    }

    /**
     * Create an instance of {@link AbstractClasses }
     * 
     */
    public AbstractClasses createAbstractClasses() {
        return new AbstractClasses();
    }

    /**
     * Create an instance of {@link Packages }
     * 
     */
    public Packages createPackages() {
        return new Packages();
    }

    /**
     * Create an instance of {@link UsedBy }
     * 
     */
    public UsedBy createUsedBy() {
        return new UsedBy();
    }

    /**
     * Create an instance of {@link ConcreteClasses }
     * 
     */
    public ConcreteClasses createConcreteClasses() {
        return new ConcreteClasses();
    }

}
