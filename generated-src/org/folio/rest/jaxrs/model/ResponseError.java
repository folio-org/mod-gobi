package org.folio.rest.jaxrs.model;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * API Error Response
 * 
 * <p>Java class for ResponseError complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResponseError">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Message" type="{}ResponseErrorMessage"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponseError", propOrder = { "code", "message" })
@Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-16T02:14:12-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
public class ResponseError {

    @XmlElement(name = "Code", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-16T02:14:12-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    protected String code;

    @XmlElement(name = "Message", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-16T02:14:12-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    protected String message;

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-16T02:14:12-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-16T02:14:12-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-16T02:14:12-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-16T02:14:12-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public void setMessage(String value) {
        this.message = value;
    }
}
