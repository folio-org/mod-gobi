package org.folio.rest.jaxrs.model;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>Java class for subfieldatafieldType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="subfieldatafieldType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;>subfieldDataType">
 *       &lt;attribute name="id" type="{}idDataType" />
 *       &lt;attribute name="code" use="required" type="{}subfieldcodeDataType" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "subfieldatafieldType", propOrder = { "value" })
@Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
public class SubfieldatafieldType {

    @XmlValue
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    protected String value;

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    protected String id;

    @XmlAttribute(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    protected String code;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public void setCode(String value) {
        this.code = value;
    }
}
