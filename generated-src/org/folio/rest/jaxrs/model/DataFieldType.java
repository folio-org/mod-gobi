package org.folio.rest.jaxrs.model;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * MARC21 Variable Data Fields 010-999
 * 
 * <p>Java class for dataFieldType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dataFieldType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded">
 *         &lt;element name="subfield" type="{}subfieldatafieldType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{}idDataType" />
 *       &lt;attribute name="tag" use="required" type="{}tagDataType" />
 *       &lt;attribute name="ind1" use="required" type="{}indicatorDataType" />
 *       &lt;attribute name="ind2" use="required" type="{}indicatorDataType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dataFieldType", propOrder = { "subfield" })
@Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
public class DataFieldType {

    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    protected List<SubfieldatafieldType> subfield;

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    protected String id;

    @XmlAttribute(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    protected String tag;

    @XmlAttribute(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    protected String ind1;

    @XmlAttribute(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    protected String ind2;

    /**
     * Gets the value of the subfield property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subfield property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubfield().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SubfieldatafieldType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public List<SubfieldatafieldType> getSubfield() {
        if (subfield == null) {
            subfield = new ArrayList<SubfieldatafieldType>();
        }
        return this.subfield;
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
     * Gets the value of the tag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public String getTag() {
        return tag;
    }

    /**
     * Sets the value of the tag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public void setTag(String value) {
        this.tag = value;
    }

    /**
     * Gets the value of the ind1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public String getInd1() {
        return ind1;
    }

    /**
     * Sets the value of the ind1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public void setInd1(String value) {
        this.ind1 = value;
    }

    /**
     * Gets the value of the ind2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public String getInd2() {
        return ind2;
    }

    /**
     * Sets the value of the ind2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public void setInd2(String value) {
        this.ind2 = value;
    }
}
