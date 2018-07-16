package org.folio.rest.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

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
 *         &lt;element name="CustomerDetail">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="BaseAccount" type="{}BaseAccount"/>
 *                   &lt;element name="SubAccount" type="{}SubAccount"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Order">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element name="ListedElectronicMonograph">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}collection"/>
 *                             &lt;element name="OrderDetail">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
 *                                       &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
 *                                       &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
 *                                       &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
 *                                       &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
 *                                       &lt;element name="OtherLocalId" type="{}OtherLocalId" minOccurs="0"/>
 *                                       &lt;element name="Location" type="{}Location" minOccurs="0"/>
 *                                       &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *                                       &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *                                       &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                                       &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
 *                                       &lt;element name="ListPrice">
 *                                         &lt;complexType>
 *                                           &lt;complexContent>
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                               &lt;sequence>
 *                                                 &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *                                                 &lt;element name="Currency" type="{}Currency"/>
 *                                               &lt;/sequence>
 *                                             &lt;/restriction>
 *                                           &lt;/complexContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                       &lt;element name="LocalData" maxOccurs="unbounded" minOccurs="0">
 *                                         &lt;complexType>
 *                                           &lt;complexContent>
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                               &lt;sequence>
 *                                                 &lt;element name="Description" type="{}LD-Description"/>
 *                                                 &lt;element name="Value" type="{}LD-Value"/>
 *                                               &lt;/sequence>
 *                                             &lt;/restriction>
 *                                           &lt;/complexContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                       &lt;element name="PurchaseOption">
 *                                         &lt;complexType>
 *                                           &lt;complexContent>
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                               &lt;sequence>
 *                                                 &lt;element name="VendorPOCode" type="{}Vendor-PO-Code"/>
 *                                                 &lt;element name="Code" type="{}PO-Code"/>
 *                                                 &lt;element name="Description" type="{}PO-Description"/>
 *                                                 &lt;element name="VendorCode" type="{}PO-VendorCode"/>
 *                                               &lt;/sequence>
 *                                             &lt;/restriction>
 *                                           &lt;/complexContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="ListedElectronicSerial">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}collection"/>
 *                             &lt;element name="OrderDetail">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
 *                                       &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
 *                                       &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
 *                                       &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
 *                                       &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
 *                                       &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *                                       &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *                                       &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                                       &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
 *                                       &lt;element name="ListPrice">
 *                                         &lt;complexType>
 *                                           &lt;complexContent>
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                               &lt;sequence>
 *                                                 &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *                                                 &lt;element name="Currency" type="{}Currency"/>
 *                                               &lt;/sequence>
 *                                             &lt;/restriction>
 *                                           &lt;/complexContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                       &lt;element name="PurchaseOption">
 *                                         &lt;complexType>
 *                                           &lt;complexContent>
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                               &lt;sequence>
 *                                                 &lt;element name="VendorPOCode" type="{}Vendor-PO-Code"/>
 *                                                 &lt;element name="Code" type="{}PO-Code"/>
 *                                                 &lt;element name="Description" type="{}PO-Description"/>
 *                                                 &lt;element name="VendorCode" type="{}PO-VendorCode"/>
 *                                               &lt;/sequence>
 *                                             &lt;/restriction>
 *                                           &lt;/complexContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="ListedPrintMonograph">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}collection"/>
 *                             &lt;element name="OrderDetail">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
 *                                       &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
 *                                       &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
 *                                       &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
 *                                       &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
 *                                       &lt;element name="OtherLocalId" type="{}OtherLocalId" minOccurs="0"/>
 *                                       &lt;element name="Location" type="{}Location" minOccurs="0"/>
 *                                       &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *                                       &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *                                       &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                                       &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
 *                                       &lt;element name="ListPrice">
 *                                         &lt;complexType>
 *                                           &lt;complexContent>
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                               &lt;sequence>
 *                                                 &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *                                                 &lt;element name="Currency" type="{}Currency"/>
 *                                               &lt;/sequence>
 *                                             &lt;/restriction>
 *                                           &lt;/complexContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                       &lt;element name="LocalData" maxOccurs="unbounded" minOccurs="0">
 *                                         &lt;complexType>
 *                                           &lt;complexContent>
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                               &lt;sequence>
 *                                                 &lt;element name="Description" type="{}LD-Description"/>
 *                                                 &lt;element name="Value" type="{}LD-Value"/>
 *                                               &lt;/sequence>
 *                                             &lt;/restriction>
 *                                           &lt;/complexContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="ListedPrintSerial">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}collection"/>
 *                             &lt;element name="OrderDetail">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
 *                                       &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
 *                                       &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
 *                                       &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
 *                                       &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
 *                                       &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *                                       &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *                                       &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                                       &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
 *                                       &lt;element name="StartWithVolume" type="{}StartWithVolume" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="UnlistedPrintMonograph">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}collection"/>
 *                             &lt;element name="OrderDetail">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
 *                                       &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
 *                                       &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
 *                                       &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
 *                                       &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
 *                                       &lt;element name="OtherLocalId" type="{}OtherLocalId" minOccurs="0"/>
 *                                       &lt;element name="Location" type="{}Location" minOccurs="0"/>
 *                                       &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *                                       &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *                                       &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                                       &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
 *                                       &lt;element name="ListPrice" minOccurs="0">
 *                                         &lt;complexType>
 *                                           &lt;complexContent>
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                               &lt;sequence>
 *                                                 &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *                                                 &lt;element name="Currency" type="{}Currency"/>
 *                                               &lt;/sequence>
 *                                             &lt;/restriction>
 *                                           &lt;/complexContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                       &lt;element name="LocalData" maxOccurs="unbounded" minOccurs="0">
 *                                         &lt;complexType>
 *                                           &lt;complexContent>
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                               &lt;sequence>
 *                                                 &lt;element name="Description" type="{}LD-Description"/>
 *                                                 &lt;element name="Value" type="{}LD-Value"/>
 *                                               &lt;/sequence>
 *                                             &lt;/restriction>
 *                                           &lt;/complexContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="UnlistedPrintSerial">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element ref="{}collection"/>
 *                             &lt;element name="OrderDetail">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
 *                                       &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
 *                                       &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
 *                                       &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
 *                                       &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
 *                                       &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *                                       &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *                                       &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                                       &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
 *                                       &lt;element name="StartWithVolume" type="{}StartWithVolume" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "customerDetail", "order" })
@XmlRootElement(name = "PurchaseOrder")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
public class PurchaseOrder {

    @XmlElement(name = "CustomerDetail", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    protected PurchaseOrder.CustomerDetail customerDetail;

    @XmlElement(name = "Order", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    protected PurchaseOrder.Order order;

    /**
     * Gets the value of the customerDetail property.
     * 
     * @return
     *     possible object is
     *     {@link PurchaseOrder.CustomerDetail }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public PurchaseOrder.CustomerDetail getCustomerDetail() {
        return customerDetail;
    }

    /**
     * Sets the value of the customerDetail property.
     * 
     * @param value
     *     allowed object is
     *     {@link PurchaseOrder.CustomerDetail }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public void setCustomerDetail(PurchaseOrder.CustomerDetail value) {
        this.customerDetail = value;
    }

    /**
     * Gets the value of the order property.
     * 
     * @return
     *     possible object is
     *     {@link PurchaseOrder.Order }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public PurchaseOrder.Order getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     * @param value
     *     allowed object is
     *     {@link PurchaseOrder.Order }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public void setOrder(PurchaseOrder.Order value) {
        this.order = value;
    }

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
     *         &lt;element name="BaseAccount" type="{}BaseAccount"/>
     *         &lt;element name="SubAccount" type="{}SubAccount"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "baseAccount", "subAccount" })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public static class CustomerDetail {

        @XmlElement(name = "BaseAccount")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        protected int baseAccount;

        @XmlElement(name = "SubAccount")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        protected int subAccount;

        /**
         * Gets the value of the baseAccount property.
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public int getBaseAccount() {
            return baseAccount;
        }

        /**
         * Sets the value of the baseAccount property.
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public void setBaseAccount(int value) {
            this.baseAccount = value;
        }

        /**
         * Gets the value of the subAccount property.
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public int getSubAccount() {
            return subAccount;
        }

        /**
         * Sets the value of the subAccount property.
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public void setSubAccount(int value) {
            this.subAccount = value;
        }
    }

    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;choice>
     *         &lt;element name="ListedElectronicMonograph">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}collection"/>
     *                   &lt;element name="OrderDetail">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
     *                             &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
     *                             &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
     *                             &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
     *                             &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
     *                             &lt;element name="OtherLocalId" type="{}OtherLocalId" minOccurs="0"/>
     *                             &lt;element name="Location" type="{}Location" minOccurs="0"/>
     *                             &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
     *                             &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
     *                             &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *                             &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
     *                             &lt;element name="ListPrice">
     *                               &lt;complexType>
     *                                 &lt;complexContent>
     *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                                     &lt;sequence>
     *                                       &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
     *                                       &lt;element name="Currency" type="{}Currency"/>
     *                                     &lt;/sequence>
     *                                   &lt;/restriction>
     *                                 &lt;/complexContent>
     *                               &lt;/complexType>
     *                             &lt;/element>
     *                             &lt;element name="LocalData" maxOccurs="unbounded" minOccurs="0">
     *                               &lt;complexType>
     *                                 &lt;complexContent>
     *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                                     &lt;sequence>
     *                                       &lt;element name="Description" type="{}LD-Description"/>
     *                                       &lt;element name="Value" type="{}LD-Value"/>
     *                                     &lt;/sequence>
     *                                   &lt;/restriction>
     *                                 &lt;/complexContent>
     *                               &lt;/complexType>
     *                             &lt;/element>
     *                             &lt;element name="PurchaseOption">
     *                               &lt;complexType>
     *                                 &lt;complexContent>
     *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                                     &lt;sequence>
     *                                       &lt;element name="VendorPOCode" type="{}Vendor-PO-Code"/>
     *                                       &lt;element name="Code" type="{}PO-Code"/>
     *                                       &lt;element name="Description" type="{}PO-Description"/>
     *                                       &lt;element name="VendorCode" type="{}PO-VendorCode"/>
     *                                     &lt;/sequence>
     *                                   &lt;/restriction>
     *                                 &lt;/complexContent>
     *                               &lt;/complexType>
     *                             &lt;/element>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="ListedElectronicSerial">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}collection"/>
     *                   &lt;element name="OrderDetail">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
     *                             &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
     *                             &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
     *                             &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
     *                             &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
     *                             &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
     *                             &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
     *                             &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *                             &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
     *                             &lt;element name="ListPrice">
     *                               &lt;complexType>
     *                                 &lt;complexContent>
     *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                                     &lt;sequence>
     *                                       &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
     *                                       &lt;element name="Currency" type="{}Currency"/>
     *                                     &lt;/sequence>
     *                                   &lt;/restriction>
     *                                 &lt;/complexContent>
     *                               &lt;/complexType>
     *                             &lt;/element>
     *                             &lt;element name="PurchaseOption">
     *                               &lt;complexType>
     *                                 &lt;complexContent>
     *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                                     &lt;sequence>
     *                                       &lt;element name="VendorPOCode" type="{}Vendor-PO-Code"/>
     *                                       &lt;element name="Code" type="{}PO-Code"/>
     *                                       &lt;element name="Description" type="{}PO-Description"/>
     *                                       &lt;element name="VendorCode" type="{}PO-VendorCode"/>
     *                                     &lt;/sequence>
     *                                   &lt;/restriction>
     *                                 &lt;/complexContent>
     *                               &lt;/complexType>
     *                             &lt;/element>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="ListedPrintMonograph">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}collection"/>
     *                   &lt;element name="OrderDetail">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
     *                             &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
     *                             &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
     *                             &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
     *                             &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
     *                             &lt;element name="OtherLocalId" type="{}OtherLocalId" minOccurs="0"/>
     *                             &lt;element name="Location" type="{}Location" minOccurs="0"/>
     *                             &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
     *                             &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
     *                             &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *                             &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
     *                             &lt;element name="ListPrice">
     *                               &lt;complexType>
     *                                 &lt;complexContent>
     *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                                     &lt;sequence>
     *                                       &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
     *                                       &lt;element name="Currency" type="{}Currency"/>
     *                                     &lt;/sequence>
     *                                   &lt;/restriction>
     *                                 &lt;/complexContent>
     *                               &lt;/complexType>
     *                             &lt;/element>
     *                             &lt;element name="LocalData" maxOccurs="unbounded" minOccurs="0">
     *                               &lt;complexType>
     *                                 &lt;complexContent>
     *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                                     &lt;sequence>
     *                                       &lt;element name="Description" type="{}LD-Description"/>
     *                                       &lt;element name="Value" type="{}LD-Value"/>
     *                                     &lt;/sequence>
     *                                   &lt;/restriction>
     *                                 &lt;/complexContent>
     *                               &lt;/complexType>
     *                             &lt;/element>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="ListedPrintSerial">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}collection"/>
     *                   &lt;element name="OrderDetail">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
     *                             &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
     *                             &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
     *                             &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
     *                             &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
     *                             &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
     *                             &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
     *                             &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *                             &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
     *                             &lt;element name="StartWithVolume" type="{}StartWithVolume" minOccurs="0"/>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="UnlistedPrintMonograph">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}collection"/>
     *                   &lt;element name="OrderDetail">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
     *                             &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
     *                             &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
     *                             &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
     *                             &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
     *                             &lt;element name="OtherLocalId" type="{}OtherLocalId" minOccurs="0"/>
     *                             &lt;element name="Location" type="{}Location" minOccurs="0"/>
     *                             &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
     *                             &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
     *                             &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *                             &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
     *                             &lt;element name="ListPrice" minOccurs="0">
     *                               &lt;complexType>
     *                                 &lt;complexContent>
     *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                                     &lt;sequence>
     *                                       &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
     *                                       &lt;element name="Currency" type="{}Currency"/>
     *                                     &lt;/sequence>
     *                                   &lt;/restriction>
     *                                 &lt;/complexContent>
     *                               &lt;/complexType>
     *                             &lt;/element>
     *                             &lt;element name="LocalData" maxOccurs="unbounded" minOccurs="0">
     *                               &lt;complexType>
     *                                 &lt;complexContent>
     *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                                     &lt;sequence>
     *                                       &lt;element name="Description" type="{}LD-Description"/>
     *                                       &lt;element name="Value" type="{}LD-Value"/>
     *                                     &lt;/sequence>
     *                                   &lt;/restriction>
     *                                 &lt;/complexContent>
     *                               &lt;/complexType>
     *                             &lt;/element>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="UnlistedPrintSerial">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element ref="{}collection"/>
     *                   &lt;element name="OrderDetail">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
     *                             &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
     *                             &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
     *                             &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
     *                             &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
     *                             &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
     *                             &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
     *                             &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *                             &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
     *                             &lt;element name="StartWithVolume" type="{}StartWithVolume" minOccurs="0"/>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/choice>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "listedElectronicMonograph", "listedElectronicSerial", "listedPrintMonograph", "listedPrintSerial", "unlistedPrintMonograph", "unlistedPrintSerial" })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
    public static class Order {

        @XmlElement(name = "ListedElectronicMonograph")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        protected PurchaseOrder.Order.ListedElectronicMonograph listedElectronicMonograph;

        @XmlElement(name = "ListedElectronicSerial")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        protected PurchaseOrder.Order.ListedElectronicSerial listedElectronicSerial;

        @XmlElement(name = "ListedPrintMonograph")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        protected PurchaseOrder.Order.ListedPrintMonograph listedPrintMonograph;

        @XmlElement(name = "ListedPrintSerial")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        protected PurchaseOrder.Order.ListedPrintSerial listedPrintSerial;

        @XmlElement(name = "UnlistedPrintMonograph")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        protected PurchaseOrder.Order.UnlistedPrintMonograph unlistedPrintMonograph;

        @XmlElement(name = "UnlistedPrintSerial")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        protected PurchaseOrder.Order.UnlistedPrintSerial unlistedPrintSerial;

        /**
         * Gets the value of the listedElectronicMonograph property.
         * 
         * @return
         *     possible object is
         *     {@link PurchaseOrder.Order.ListedElectronicMonograph }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public PurchaseOrder.Order.ListedElectronicMonograph getListedElectronicMonograph() {
            return listedElectronicMonograph;
        }

        /**
         * Sets the value of the listedElectronicMonograph property.
         * 
         * @param value
         *     allowed object is
         *     {@link PurchaseOrder.Order.ListedElectronicMonograph }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public void setListedElectronicMonograph(PurchaseOrder.Order.ListedElectronicMonograph value) {
            this.listedElectronicMonograph = value;
        }

        /**
         * Gets the value of the listedElectronicSerial property.
         * 
         * @return
         *     possible object is
         *     {@link PurchaseOrder.Order.ListedElectronicSerial }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public PurchaseOrder.Order.ListedElectronicSerial getListedElectronicSerial() {
            return listedElectronicSerial;
        }

        /**
         * Sets the value of the listedElectronicSerial property.
         * 
         * @param value
         *     allowed object is
         *     {@link PurchaseOrder.Order.ListedElectronicSerial }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public void setListedElectronicSerial(PurchaseOrder.Order.ListedElectronicSerial value) {
            this.listedElectronicSerial = value;
        }

        /**
         * Gets the value of the listedPrintMonograph property.
         * 
         * @return
         *     possible object is
         *     {@link PurchaseOrder.Order.ListedPrintMonograph }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public PurchaseOrder.Order.ListedPrintMonograph getListedPrintMonograph() {
            return listedPrintMonograph;
        }

        /**
         * Sets the value of the listedPrintMonograph property.
         * 
         * @param value
         *     allowed object is
         *     {@link PurchaseOrder.Order.ListedPrintMonograph }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public void setListedPrintMonograph(PurchaseOrder.Order.ListedPrintMonograph value) {
            this.listedPrintMonograph = value;
        }

        /**
         * Gets the value of the listedPrintSerial property.
         * 
         * @return
         *     possible object is
         *     {@link PurchaseOrder.Order.ListedPrintSerial }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public PurchaseOrder.Order.ListedPrintSerial getListedPrintSerial() {
            return listedPrintSerial;
        }

        /**
         * Sets the value of the listedPrintSerial property.
         * 
         * @param value
         *     allowed object is
         *     {@link PurchaseOrder.Order.ListedPrintSerial }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public void setListedPrintSerial(PurchaseOrder.Order.ListedPrintSerial value) {
            this.listedPrintSerial = value;
        }

        /**
         * Gets the value of the unlistedPrintMonograph property.
         * 
         * @return
         *     possible object is
         *     {@link PurchaseOrder.Order.UnlistedPrintMonograph }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public PurchaseOrder.Order.UnlistedPrintMonograph getUnlistedPrintMonograph() {
            return unlistedPrintMonograph;
        }

        /**
         * Sets the value of the unlistedPrintMonograph property.
         * 
         * @param value
         *     allowed object is
         *     {@link PurchaseOrder.Order.UnlistedPrintMonograph }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public void setUnlistedPrintMonograph(PurchaseOrder.Order.UnlistedPrintMonograph value) {
            this.unlistedPrintMonograph = value;
        }

        /**
         * Gets the value of the unlistedPrintSerial property.
         * 
         * @return
         *     possible object is
         *     {@link PurchaseOrder.Order.UnlistedPrintSerial }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public PurchaseOrder.Order.UnlistedPrintSerial getUnlistedPrintSerial() {
            return unlistedPrintSerial;
        }

        /**
         * Sets the value of the unlistedPrintSerial property.
         * 
         * @param value
         *     allowed object is
         *     {@link PurchaseOrder.Order.UnlistedPrintSerial }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public void setUnlistedPrintSerial(PurchaseOrder.Order.UnlistedPrintSerial value) {
            this.unlistedPrintSerial = value;
        }

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
         *         &lt;element ref="{}collection"/>
         *         &lt;element name="OrderDetail">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
         *                   &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
         *                   &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
         *                   &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
         *                   &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
         *                   &lt;element name="OtherLocalId" type="{}OtherLocalId" minOccurs="0"/>
         *                   &lt;element name="Location" type="{}Location" minOccurs="0"/>
         *                   &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
         *                   &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
         *                   &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
         *                   &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
         *                   &lt;element name="ListPrice">
         *                     &lt;complexType>
         *                       &lt;complexContent>
         *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                           &lt;sequence>
         *                             &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
         *                             &lt;element name="Currency" type="{}Currency"/>
         *                           &lt;/sequence>
         *                         &lt;/restriction>
         *                       &lt;/complexContent>
         *                     &lt;/complexType>
         *                   &lt;/element>
         *                   &lt;element name="LocalData" maxOccurs="unbounded" minOccurs="0">
         *                     &lt;complexType>
         *                       &lt;complexContent>
         *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                           &lt;sequence>
         *                             &lt;element name="Description" type="{}LD-Description"/>
         *                             &lt;element name="Value" type="{}LD-Value"/>
         *                           &lt;/sequence>
         *                         &lt;/restriction>
         *                       &lt;/complexContent>
         *                     &lt;/complexType>
         *                   &lt;/element>
         *                   &lt;element name="PurchaseOption">
         *                     &lt;complexType>
         *                       &lt;complexContent>
         *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                           &lt;sequence>
         *                             &lt;element name="VendorPOCode" type="{}Vendor-PO-Code"/>
         *                             &lt;element name="Code" type="{}PO-Code"/>
         *                             &lt;element name="Description" type="{}PO-Description"/>
         *                             &lt;element name="VendorCode" type="{}PO-VendorCode"/>
         *                           &lt;/sequence>
         *                         &lt;/restriction>
         *                       &lt;/complexContent>
         *                     &lt;/complexType>
         *                   &lt;/element>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = { "collection", "orderDetail" })
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public static class ListedElectronicMonograph {

            @XmlElement(required = true, nillable = true)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            protected CollectionType collection;

            @XmlElement(name = "OrderDetail", required = true)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            protected PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail orderDetail;

            /**
             * This is the MARC record object container.
             * 
             * @return
             *     possible object is
             *     {@link CollectionType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public CollectionType getCollection() {
                return collection;
            }

            /**
             * Sets the value of the collection property.
             * 
             * @param value
             *     allowed object is
             *     {@link CollectionType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public void setCollection(CollectionType value) {
                this.collection = value;
            }

            /**
             * Gets the value of the orderDetail property.
             * 
             * @return
             *     possible object is
             *     {@link PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail getOrderDetail() {
                return orderDetail;
            }

            /**
             * Sets the value of the orderDetail property.
             * 
             * @param value
             *     allowed object is
             *     {@link PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public void setOrderDetail(PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail value) {
                this.orderDetail = value;
            }

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
             *         &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
             *         &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
             *         &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
             *         &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
             *         &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
             *         &lt;element name="OtherLocalId" type="{}OtherLocalId" minOccurs="0"/>
             *         &lt;element name="Location" type="{}Location" minOccurs="0"/>
             *         &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
             *         &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
             *         &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
             *         &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
             *         &lt;element name="ListPrice">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;sequence>
             *                   &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
             *                   &lt;element name="Currency" type="{}Currency"/>
             *                 &lt;/sequence>
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="LocalData" maxOccurs="unbounded" minOccurs="0">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;sequence>
             *                   &lt;element name="Description" type="{}LD-Description"/>
             *                   &lt;element name="Value" type="{}LD-Value"/>
             *                 &lt;/sequence>
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="PurchaseOption">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;sequence>
             *                   &lt;element name="VendorPOCode" type="{}Vendor-PO-Code"/>
             *                   &lt;element name="Code" type="{}PO-Code"/>
             *                   &lt;element name="Description" type="{}PO-Description"/>
             *                   &lt;element name="VendorCode" type="{}PO-VendorCode"/>
             *                 &lt;/sequence>
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = { "batchPONumber", "itemPONumber", "fundCode", "mappedFundCode", "orderNotes", "otherLocalId", "location", "quantity", "ybpOrderKey", "orderPlaced", "initials", "listPrice", "localData", "purchaseOption" })
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public static class OrderDetail {

                @XmlElement(name = "BatchPONumber")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String batchPONumber;

                @XmlElement(name = "ItemPONumber")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String itemPONumber;

                @XmlElement(name = "FundCode")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String fundCode;

                @XmlElement(name = "MappedFundCode")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String mappedFundCode;

                @XmlElement(name = "OrderNotes")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String orderNotes;

                @XmlElement(name = "OtherLocalId")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String otherLocalId;

                @XmlElement(name = "Location")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String location;

                @XmlElement(name = "Quantity", required = true)
                @XmlSchemaType(name = "positiveInteger")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected BigInteger quantity;

                @XmlElement(name = "YBPOrderKey", required = true)
                @XmlSchemaType(name = "positiveInteger")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected BigInteger ybpOrderKey;

                @XmlElement(name = "OrderPlaced", required = true)
                @XmlSchemaType(name = "dateTime")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected XMLGregorianCalendar orderPlaced;

                @XmlElement(name = "Initials")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String initials;

                @XmlElement(name = "ListPrice", required = true)
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail.ListPrice listPrice;

                @XmlElement(name = "LocalData")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected List<PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail.LocalData> localData;

                @XmlElement(name = "PurchaseOption", required = true)
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail.PurchaseOption purchaseOption;

                /**
                 * Gets the value of the batchPONumber property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getBatchPONumber() {
                    return batchPONumber;
                }

                /**
                 * Sets the value of the batchPONumber property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setBatchPONumber(String value) {
                    this.batchPONumber = value;
                }

                /**
                 * Gets the value of the itemPONumber property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getItemPONumber() {
                    return itemPONumber;
                }

                /**
                 * Sets the value of the itemPONumber property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setItemPONumber(String value) {
                    this.itemPONumber = value;
                }

                /**
                 * Gets the value of the fundCode property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getFundCode() {
                    return fundCode;
                }

                /**
                 * Sets the value of the fundCode property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setFundCode(String value) {
                    this.fundCode = value;
                }

                /**
                 * Gets the value of the mappedFundCode property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getMappedFundCode() {
                    return mappedFundCode;
                }

                /**
                 * Sets the value of the mappedFundCode property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setMappedFundCode(String value) {
                    this.mappedFundCode = value;
                }

                /**
                 * Gets the value of the orderNotes property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getOrderNotes() {
                    return orderNotes;
                }

                /**
                 * Sets the value of the orderNotes property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setOrderNotes(String value) {
                    this.orderNotes = value;
                }

                /**
                 * Gets the value of the otherLocalId property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getOtherLocalId() {
                    return otherLocalId;
                }

                /**
                 * Sets the value of the otherLocalId property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setOtherLocalId(String value) {
                    this.otherLocalId = value;
                }

                /**
                 * Gets the value of the location property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getLocation() {
                    return location;
                }

                /**
                 * Sets the value of the location property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setLocation(String value) {
                    this.location = value;
                }

                /**
                 * Gets the value of the quantity property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public BigInteger getQuantity() {
                    return quantity;
                }

                /**
                 * Sets the value of the quantity property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setQuantity(BigInteger value) {
                    this.quantity = value;
                }

                /**
                 * Gets the value of the ybpOrderKey property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public BigInteger getYBPOrderKey() {
                    return ybpOrderKey;
                }

                /**
                 * Sets the value of the ybpOrderKey property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setYBPOrderKey(BigInteger value) {
                    this.ybpOrderKey = value;
                }

                /**
                 * Gets the value of the orderPlaced property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link XMLGregorianCalendar }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public XMLGregorianCalendar getOrderPlaced() {
                    return orderPlaced;
                }

                /**
                 * Sets the value of the orderPlaced property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link XMLGregorianCalendar }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setOrderPlaced(XMLGregorianCalendar value) {
                    this.orderPlaced = value;
                }

                /**
                 * Gets the value of the initials property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getInitials() {
                    return initials;
                }

                /**
                 * Sets the value of the initials property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setInitials(String value) {
                    this.initials = value;
                }

                /**
                 * Gets the value of the listPrice property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail.ListPrice }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail.ListPrice getListPrice() {
                    return listPrice;
                }

                /**
                 * Sets the value of the listPrice property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail.ListPrice }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setListPrice(PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail.ListPrice value) {
                    this.listPrice = value;
                }

                /**
                 * Gets the value of the localData property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the localData property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getLocalData().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail.LocalData }
                 * 
                 * 
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public List<PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail.LocalData> getLocalData() {
                    if (localData == null) {
                        localData = new ArrayList<PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail.LocalData>();
                    }
                    return this.localData;
                }

                /**
                 * Gets the value of the purchaseOption property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail.PurchaseOption }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail.PurchaseOption getPurchaseOption() {
                    return purchaseOption;
                }

                /**
                 * Sets the value of the purchaseOption property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail.PurchaseOption }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setPurchaseOption(PurchaseOrder.Order.ListedElectronicMonograph.OrderDetail.PurchaseOption value) {
                    this.purchaseOption = value;
                }

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
                 *         &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
                 *         &lt;element name="Currency" type="{}Currency"/>
                 *       &lt;/sequence>
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = { "amount", "currency" })
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public static class ListPrice {

                    @XmlElement(name = "Amount", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected BigDecimal amount;

                    @XmlElement(name = "Currency", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String currency;

                    /**
                     * Gets the value of the amount property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link BigDecimal }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public BigDecimal getAmount() {
                        return amount;
                    }

                    /**
                     * Sets the value of the amount property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link BigDecimal }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setAmount(BigDecimal value) {
                        this.amount = value;
                    }

                    /**
                     * Gets the value of the currency property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public String getCurrency() {
                        return currency;
                    }

                    /**
                     * Sets the value of the currency property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setCurrency(String value) {
                        this.currency = value;
                    }
                }

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
                 *         &lt;element name="Description" type="{}LD-Description"/>
                 *         &lt;element name="Value" type="{}LD-Value"/>
                 *       &lt;/sequence>
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = { "description", "value" })
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public static class LocalData {

                    @XmlElement(name = "Description", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String description;

                    @XmlElement(name = "Value", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String value;

                    /**
                     * Gets the value of the description property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public String getDescription() {
                        return description;
                    }

                    /**
                     * Sets the value of the description property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setDescription(String value) {
                        this.description = value;
                    }

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
                }

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
                 *         &lt;element name="VendorPOCode" type="{}Vendor-PO-Code"/>
                 *         &lt;element name="Code" type="{}PO-Code"/>
                 *         &lt;element name="Description" type="{}PO-Description"/>
                 *         &lt;element name="VendorCode" type="{}PO-VendorCode"/>
                 *       &lt;/sequence>
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = { "vendorPOCode", "code", "description", "vendorCode" })
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public static class PurchaseOption {

                    @XmlElement(name = "VendorPOCode", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String vendorPOCode;

                    @XmlElement(name = "Code", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String code;

                    @XmlElement(name = "Description", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String description;

                    @XmlElement(name = "VendorCode", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String vendorCode;

                    /**
                     * Gets the value of the vendorPOCode property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public String getVendorPOCode() {
                        return vendorPOCode;
                    }

                    /**
                     * Sets the value of the vendorPOCode property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setVendorPOCode(String value) {
                        this.vendorPOCode = value;
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

                    /**
                     * Gets the value of the description property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public String getDescription() {
                        return description;
                    }

                    /**
                     * Sets the value of the description property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setDescription(String value) {
                        this.description = value;
                    }

                    /**
                     * Gets the value of the vendorCode property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public String getVendorCode() {
                        return vendorCode;
                    }

                    /**
                     * Sets the value of the vendorCode property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setVendorCode(String value) {
                        this.vendorCode = value;
                    }
                }
            }
        }

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
         *         &lt;element ref="{}collection"/>
         *         &lt;element name="OrderDetail">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
         *                   &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
         *                   &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
         *                   &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
         *                   &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
         *                   &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
         *                   &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
         *                   &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
         *                   &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
         *                   &lt;element name="ListPrice">
         *                     &lt;complexType>
         *                       &lt;complexContent>
         *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                           &lt;sequence>
         *                             &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
         *                             &lt;element name="Currency" type="{}Currency"/>
         *                           &lt;/sequence>
         *                         &lt;/restriction>
         *                       &lt;/complexContent>
         *                     &lt;/complexType>
         *                   &lt;/element>
         *                   &lt;element name="PurchaseOption">
         *                     &lt;complexType>
         *                       &lt;complexContent>
         *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                           &lt;sequence>
         *                             &lt;element name="VendorPOCode" type="{}Vendor-PO-Code"/>
         *                             &lt;element name="Code" type="{}PO-Code"/>
         *                             &lt;element name="Description" type="{}PO-Description"/>
         *                             &lt;element name="VendorCode" type="{}PO-VendorCode"/>
         *                           &lt;/sequence>
         *                         &lt;/restriction>
         *                       &lt;/complexContent>
         *                     &lt;/complexType>
         *                   &lt;/element>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = { "collection", "orderDetail" })
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public static class ListedElectronicSerial {

            @XmlElement(required = true, nillable = true)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            protected CollectionType collection;

            @XmlElement(name = "OrderDetail", required = true)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            protected PurchaseOrder.Order.ListedElectronicSerial.OrderDetail orderDetail;

            /**
             * This is the MARC record object container.
             * 
             * @return
             *     possible object is
             *     {@link CollectionType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public CollectionType getCollection() {
                return collection;
            }

            /**
             * Sets the value of the collection property.
             * 
             * @param value
             *     allowed object is
             *     {@link CollectionType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public void setCollection(CollectionType value) {
                this.collection = value;
            }

            /**
             * Gets the value of the orderDetail property.
             * 
             * @return
             *     possible object is
             *     {@link PurchaseOrder.Order.ListedElectronicSerial.OrderDetail }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public PurchaseOrder.Order.ListedElectronicSerial.OrderDetail getOrderDetail() {
                return orderDetail;
            }

            /**
             * Sets the value of the orderDetail property.
             * 
             * @param value
             *     allowed object is
             *     {@link PurchaseOrder.Order.ListedElectronicSerial.OrderDetail }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public void setOrderDetail(PurchaseOrder.Order.ListedElectronicSerial.OrderDetail value) {
                this.orderDetail = value;
            }

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
             *         &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
             *         &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
             *         &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
             *         &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
             *         &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
             *         &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
             *         &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
             *         &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
             *         &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
             *         &lt;element name="ListPrice">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;sequence>
             *                   &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
             *                   &lt;element name="Currency" type="{}Currency"/>
             *                 &lt;/sequence>
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="PurchaseOption">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;sequence>
             *                   &lt;element name="VendorPOCode" type="{}Vendor-PO-Code"/>
             *                   &lt;element name="Code" type="{}PO-Code"/>
             *                   &lt;element name="Description" type="{}PO-Description"/>
             *                   &lt;element name="VendorCode" type="{}PO-VendorCode"/>
             *                 &lt;/sequence>
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = { "batchPONumber", "itemPONumber", "fundCode", "mappedFundCode", "orderNotes", "quantity", "ybpOrderKey", "orderPlaced", "initials", "listPrice", "purchaseOption" })
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public static class OrderDetail {

                @XmlElement(name = "BatchPONumber")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String batchPONumber;

                @XmlElement(name = "ItemPONumber")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String itemPONumber;

                @XmlElement(name = "FundCode")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String fundCode;

                @XmlElement(name = "MappedFundCode")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String mappedFundCode;

                @XmlElement(name = "OrderNotes")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String orderNotes;

                @XmlElement(name = "Quantity", required = true)
                @XmlSchemaType(name = "positiveInteger")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected BigInteger quantity;

                @XmlElement(name = "YBPOrderKey", required = true)
                @XmlSchemaType(name = "positiveInteger")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected BigInteger ybpOrderKey;

                @XmlElement(name = "OrderPlaced", required = true)
                @XmlSchemaType(name = "dateTime")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected XMLGregorianCalendar orderPlaced;

                @XmlElement(name = "Initials")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String initials;

                @XmlElement(name = "ListPrice", required = true)
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected PurchaseOrder.Order.ListedElectronicSerial.OrderDetail.ListPrice listPrice;

                @XmlElement(name = "PurchaseOption", required = true)
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected PurchaseOrder.Order.ListedElectronicSerial.OrderDetail.PurchaseOption purchaseOption;

                /**
                 * Gets the value of the batchPONumber property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getBatchPONumber() {
                    return batchPONumber;
                }

                /**
                 * Sets the value of the batchPONumber property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setBatchPONumber(String value) {
                    this.batchPONumber = value;
                }

                /**
                 * Gets the value of the itemPONumber property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getItemPONumber() {
                    return itemPONumber;
                }

                /**
                 * Sets the value of the itemPONumber property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setItemPONumber(String value) {
                    this.itemPONumber = value;
                }

                /**
                 * Gets the value of the fundCode property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getFundCode() {
                    return fundCode;
                }

                /**
                 * Sets the value of the fundCode property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setFundCode(String value) {
                    this.fundCode = value;
                }

                /**
                 * Gets the value of the mappedFundCode property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getMappedFundCode() {
                    return mappedFundCode;
                }

                /**
                 * Sets the value of the mappedFundCode property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setMappedFundCode(String value) {
                    this.mappedFundCode = value;
                }

                /**
                 * Gets the value of the orderNotes property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getOrderNotes() {
                    return orderNotes;
                }

                /**
                 * Sets the value of the orderNotes property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setOrderNotes(String value) {
                    this.orderNotes = value;
                }

                /**
                 * Gets the value of the quantity property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public BigInteger getQuantity() {
                    return quantity;
                }

                /**
                 * Sets the value of the quantity property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setQuantity(BigInteger value) {
                    this.quantity = value;
                }

                /**
                 * Gets the value of the ybpOrderKey property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public BigInteger getYBPOrderKey() {
                    return ybpOrderKey;
                }

                /**
                 * Sets the value of the ybpOrderKey property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setYBPOrderKey(BigInteger value) {
                    this.ybpOrderKey = value;
                }

                /**
                 * Gets the value of the orderPlaced property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link XMLGregorianCalendar }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public XMLGregorianCalendar getOrderPlaced() {
                    return orderPlaced;
                }

                /**
                 * Sets the value of the orderPlaced property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link XMLGregorianCalendar }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setOrderPlaced(XMLGregorianCalendar value) {
                    this.orderPlaced = value;
                }

                /**
                 * Gets the value of the initials property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getInitials() {
                    return initials;
                }

                /**
                 * Sets the value of the initials property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setInitials(String value) {
                    this.initials = value;
                }

                /**
                 * Gets the value of the listPrice property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link PurchaseOrder.Order.ListedElectronicSerial.OrderDetail.ListPrice }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public PurchaseOrder.Order.ListedElectronicSerial.OrderDetail.ListPrice getListPrice() {
                    return listPrice;
                }

                /**
                 * Sets the value of the listPrice property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link PurchaseOrder.Order.ListedElectronicSerial.OrderDetail.ListPrice }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setListPrice(PurchaseOrder.Order.ListedElectronicSerial.OrderDetail.ListPrice value) {
                    this.listPrice = value;
                }

                /**
                 * Gets the value of the purchaseOption property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link PurchaseOrder.Order.ListedElectronicSerial.OrderDetail.PurchaseOption }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public PurchaseOrder.Order.ListedElectronicSerial.OrderDetail.PurchaseOption getPurchaseOption() {
                    return purchaseOption;
                }

                /**
                 * Sets the value of the purchaseOption property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link PurchaseOrder.Order.ListedElectronicSerial.OrderDetail.PurchaseOption }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setPurchaseOption(PurchaseOrder.Order.ListedElectronicSerial.OrderDetail.PurchaseOption value) {
                    this.purchaseOption = value;
                }

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
                 *         &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
                 *         &lt;element name="Currency" type="{}Currency"/>
                 *       &lt;/sequence>
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = { "amount", "currency" })
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public static class ListPrice {

                    @XmlElement(name = "Amount", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected BigDecimal amount;

                    @XmlElement(name = "Currency", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String currency;

                    /**
                     * Gets the value of the amount property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link BigDecimal }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public BigDecimal getAmount() {
                        return amount;
                    }

                    /**
                     * Sets the value of the amount property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link BigDecimal }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setAmount(BigDecimal value) {
                        this.amount = value;
                    }

                    /**
                     * Gets the value of the currency property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public String getCurrency() {
                        return currency;
                    }

                    /**
                     * Sets the value of the currency property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setCurrency(String value) {
                        this.currency = value;
                    }
                }

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
                 *         &lt;element name="VendorPOCode" type="{}Vendor-PO-Code"/>
                 *         &lt;element name="Code" type="{}PO-Code"/>
                 *         &lt;element name="Description" type="{}PO-Description"/>
                 *         &lt;element name="VendorCode" type="{}PO-VendorCode"/>
                 *       &lt;/sequence>
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = { "vendorPOCode", "code", "description", "vendorCode" })
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public static class PurchaseOption {

                    @XmlElement(name = "VendorPOCode", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String vendorPOCode;

                    @XmlElement(name = "Code", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String code;

                    @XmlElement(name = "Description", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String description;

                    @XmlElement(name = "VendorCode", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String vendorCode;

                    /**
                     * Gets the value of the vendorPOCode property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public String getVendorPOCode() {
                        return vendorPOCode;
                    }

                    /**
                     * Sets the value of the vendorPOCode property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setVendorPOCode(String value) {
                        this.vendorPOCode = value;
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

                    /**
                     * Gets the value of the description property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public String getDescription() {
                        return description;
                    }

                    /**
                     * Sets the value of the description property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setDescription(String value) {
                        this.description = value;
                    }

                    /**
                     * Gets the value of the vendorCode property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public String getVendorCode() {
                        return vendorCode;
                    }

                    /**
                     * Sets the value of the vendorCode property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setVendorCode(String value) {
                        this.vendorCode = value;
                    }
                }
            }
        }

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
         *         &lt;element ref="{}collection"/>
         *         &lt;element name="OrderDetail">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
         *                   &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
         *                   &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
         *                   &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
         *                   &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
         *                   &lt;element name="OtherLocalId" type="{}OtherLocalId" minOccurs="0"/>
         *                   &lt;element name="Location" type="{}Location" minOccurs="0"/>
         *                   &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
         *                   &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
         *                   &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
         *                   &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
         *                   &lt;element name="ListPrice">
         *                     &lt;complexType>
         *                       &lt;complexContent>
         *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                           &lt;sequence>
         *                             &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
         *                             &lt;element name="Currency" type="{}Currency"/>
         *                           &lt;/sequence>
         *                         &lt;/restriction>
         *                       &lt;/complexContent>
         *                     &lt;/complexType>
         *                   &lt;/element>
         *                   &lt;element name="LocalData" maxOccurs="unbounded" minOccurs="0">
         *                     &lt;complexType>
         *                       &lt;complexContent>
         *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                           &lt;sequence>
         *                             &lt;element name="Description" type="{}LD-Description"/>
         *                             &lt;element name="Value" type="{}LD-Value"/>
         *                           &lt;/sequence>
         *                         &lt;/restriction>
         *                       &lt;/complexContent>
         *                     &lt;/complexType>
         *                   &lt;/element>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = { "collection", "orderDetail" })
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public static class ListedPrintMonograph {

            @XmlElement(required = true, nillable = true)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            protected CollectionType collection;

            @XmlElement(name = "OrderDetail", required = true)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            protected PurchaseOrder.Order.ListedPrintMonograph.OrderDetail orderDetail;

            /**
             * This is the MARC record object container.
             * 
             * @return
             *     possible object is
             *     {@link CollectionType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public CollectionType getCollection() {
                return collection;
            }

            /**
             * Sets the value of the collection property.
             * 
             * @param value
             *     allowed object is
             *     {@link CollectionType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public void setCollection(CollectionType value) {
                this.collection = value;
            }

            /**
             * Gets the value of the orderDetail property.
             * 
             * @return
             *     possible object is
             *     {@link PurchaseOrder.Order.ListedPrintMonograph.OrderDetail }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public PurchaseOrder.Order.ListedPrintMonograph.OrderDetail getOrderDetail() {
                return orderDetail;
            }

            /**
             * Sets the value of the orderDetail property.
             * 
             * @param value
             *     allowed object is
             *     {@link PurchaseOrder.Order.ListedPrintMonograph.OrderDetail }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public void setOrderDetail(PurchaseOrder.Order.ListedPrintMonograph.OrderDetail value) {
                this.orderDetail = value;
            }

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
             *         &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
             *         &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
             *         &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
             *         &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
             *         &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
             *         &lt;element name="OtherLocalId" type="{}OtherLocalId" minOccurs="0"/>
             *         &lt;element name="Location" type="{}Location" minOccurs="0"/>
             *         &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
             *         &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
             *         &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
             *         &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
             *         &lt;element name="ListPrice">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;sequence>
             *                   &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
             *                   &lt;element name="Currency" type="{}Currency"/>
             *                 &lt;/sequence>
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="LocalData" maxOccurs="unbounded" minOccurs="0">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;sequence>
             *                   &lt;element name="Description" type="{}LD-Description"/>
             *                   &lt;element name="Value" type="{}LD-Value"/>
             *                 &lt;/sequence>
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = { "batchPONumber", "itemPONumber", "fundCode", "mappedFundCode", "orderNotes", "otherLocalId", "location", "quantity", "ybpOrderKey", "orderPlaced", "initials", "listPrice", "localData" })
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public static class OrderDetail {

                @XmlElement(name = "BatchPONumber")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String batchPONumber;

                @XmlElement(name = "ItemPONumber")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String itemPONumber;

                @XmlElement(name = "FundCode")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String fundCode;

                @XmlElement(name = "MappedFundCode")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String mappedFundCode;

                @XmlElement(name = "OrderNotes")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String orderNotes;

                @XmlElement(name = "OtherLocalId")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String otherLocalId;

                @XmlElement(name = "Location")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String location;

                @XmlElement(name = "Quantity", required = true)
                @XmlSchemaType(name = "positiveInteger")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected BigInteger quantity;

                @XmlElement(name = "YBPOrderKey", required = true)
                @XmlSchemaType(name = "positiveInteger")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected BigInteger ybpOrderKey;

                @XmlElement(name = "OrderPlaced", required = true)
                @XmlSchemaType(name = "dateTime")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected XMLGregorianCalendar orderPlaced;

                @XmlElement(name = "Initials")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String initials;

                @XmlElement(name = "ListPrice", required = true)
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected PurchaseOrder.Order.ListedPrintMonograph.OrderDetail.ListPrice listPrice;

                @XmlElement(name = "LocalData")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected List<PurchaseOrder.Order.ListedPrintMonograph.OrderDetail.LocalData> localData;

                /**
                 * Gets the value of the batchPONumber property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getBatchPONumber() {
                    return batchPONumber;
                }

                /**
                 * Sets the value of the batchPONumber property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setBatchPONumber(String value) {
                    this.batchPONumber = value;
                }

                /**
                 * Gets the value of the itemPONumber property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getItemPONumber() {
                    return itemPONumber;
                }

                /**
                 * Sets the value of the itemPONumber property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setItemPONumber(String value) {
                    this.itemPONumber = value;
                }

                /**
                 * Gets the value of the fundCode property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getFundCode() {
                    return fundCode;
                }

                /**
                 * Sets the value of the fundCode property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setFundCode(String value) {
                    this.fundCode = value;
                }

                /**
                 * Gets the value of the mappedFundCode property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getMappedFundCode() {
                    return mappedFundCode;
                }

                /**
                 * Sets the value of the mappedFundCode property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setMappedFundCode(String value) {
                    this.mappedFundCode = value;
                }

                /**
                 * Gets the value of the orderNotes property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getOrderNotes() {
                    return orderNotes;
                }

                /**
                 * Sets the value of the orderNotes property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setOrderNotes(String value) {
                    this.orderNotes = value;
                }

                /**
                 * Gets the value of the otherLocalId property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getOtherLocalId() {
                    return otherLocalId;
                }

                /**
                 * Sets the value of the otherLocalId property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setOtherLocalId(String value) {
                    this.otherLocalId = value;
                }

                /**
                 * Gets the value of the location property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getLocation() {
                    return location;
                }

                /**
                 * Sets the value of the location property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setLocation(String value) {
                    this.location = value;
                }

                /**
                 * Gets the value of the quantity property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public BigInteger getQuantity() {
                    return quantity;
                }

                /**
                 * Sets the value of the quantity property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setQuantity(BigInteger value) {
                    this.quantity = value;
                }

                /**
                 * Gets the value of the ybpOrderKey property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public BigInteger getYBPOrderKey() {
                    return ybpOrderKey;
                }

                /**
                 * Sets the value of the ybpOrderKey property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setYBPOrderKey(BigInteger value) {
                    this.ybpOrderKey = value;
                }

                /**
                 * Gets the value of the orderPlaced property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link XMLGregorianCalendar }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public XMLGregorianCalendar getOrderPlaced() {
                    return orderPlaced;
                }

                /**
                 * Sets the value of the orderPlaced property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link XMLGregorianCalendar }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setOrderPlaced(XMLGregorianCalendar value) {
                    this.orderPlaced = value;
                }

                /**
                 * Gets the value of the initials property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getInitials() {
                    return initials;
                }

                /**
                 * Sets the value of the initials property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setInitials(String value) {
                    this.initials = value;
                }

                /**
                 * Gets the value of the listPrice property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link PurchaseOrder.Order.ListedPrintMonograph.OrderDetail.ListPrice }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public PurchaseOrder.Order.ListedPrintMonograph.OrderDetail.ListPrice getListPrice() {
                    return listPrice;
                }

                /**
                 * Sets the value of the listPrice property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link PurchaseOrder.Order.ListedPrintMonograph.OrderDetail.ListPrice }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setListPrice(PurchaseOrder.Order.ListedPrintMonograph.OrderDetail.ListPrice value) {
                    this.listPrice = value;
                }

                /**
                 * Gets the value of the localData property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the localData property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getLocalData().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link PurchaseOrder.Order.ListedPrintMonograph.OrderDetail.LocalData }
                 * 
                 * 
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public List<PurchaseOrder.Order.ListedPrintMonograph.OrderDetail.LocalData> getLocalData() {
                    if (localData == null) {
                        localData = new ArrayList<PurchaseOrder.Order.ListedPrintMonograph.OrderDetail.LocalData>();
                    }
                    return this.localData;
                }

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
                 *         &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
                 *         &lt;element name="Currency" type="{}Currency"/>
                 *       &lt;/sequence>
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = { "amount", "currency" })
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public static class ListPrice {

                    @XmlElement(name = "Amount", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected BigDecimal amount;

                    @XmlElement(name = "Currency", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String currency;

                    /**
                     * Gets the value of the amount property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link BigDecimal }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public BigDecimal getAmount() {
                        return amount;
                    }

                    /**
                     * Sets the value of the amount property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link BigDecimal }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setAmount(BigDecimal value) {
                        this.amount = value;
                    }

                    /**
                     * Gets the value of the currency property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public String getCurrency() {
                        return currency;
                    }

                    /**
                     * Sets the value of the currency property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setCurrency(String value) {
                        this.currency = value;
                    }
                }

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
                 *         &lt;element name="Description" type="{}LD-Description"/>
                 *         &lt;element name="Value" type="{}LD-Value"/>
                 *       &lt;/sequence>
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = { "description", "value" })
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public static class LocalData {

                    @XmlElement(name = "Description", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String description;

                    @XmlElement(name = "Value", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String value;

                    /**
                     * Gets the value of the description property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public String getDescription() {
                        return description;
                    }

                    /**
                     * Sets the value of the description property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setDescription(String value) {
                        this.description = value;
                    }

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
                }
            }
        }

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
         *         &lt;element ref="{}collection"/>
         *         &lt;element name="OrderDetail">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
         *                   &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
         *                   &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
         *                   &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
         *                   &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
         *                   &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
         *                   &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
         *                   &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
         *                   &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
         *                   &lt;element name="StartWithVolume" type="{}StartWithVolume" minOccurs="0"/>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = { "collection", "orderDetail" })
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public static class ListedPrintSerial {

            @XmlElement(required = true, nillable = true)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            protected CollectionType collection;

            @XmlElement(name = "OrderDetail", required = true)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            protected PurchaseOrder.Order.ListedPrintSerial.OrderDetail orderDetail;

            /**
             * This is the MARC record object container.
             * 
             * @return
             *     possible object is
             *     {@link CollectionType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public CollectionType getCollection() {
                return collection;
            }

            /**
             * Sets the value of the collection property.
             * 
             * @param value
             *     allowed object is
             *     {@link CollectionType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public void setCollection(CollectionType value) {
                this.collection = value;
            }

            /**
             * Gets the value of the orderDetail property.
             * 
             * @return
             *     possible object is
             *     {@link PurchaseOrder.Order.ListedPrintSerial.OrderDetail }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public PurchaseOrder.Order.ListedPrintSerial.OrderDetail getOrderDetail() {
                return orderDetail;
            }

            /**
             * Sets the value of the orderDetail property.
             * 
             * @param value
             *     allowed object is
             *     {@link PurchaseOrder.Order.ListedPrintSerial.OrderDetail }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public void setOrderDetail(PurchaseOrder.Order.ListedPrintSerial.OrderDetail value) {
                this.orderDetail = value;
            }

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
             *         &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
             *         &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
             *         &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
             *         &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
             *         &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
             *         &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
             *         &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
             *         &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
             *         &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
             *         &lt;element name="StartWithVolume" type="{}StartWithVolume" minOccurs="0"/>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = { "batchPONumber", "itemPONumber", "fundCode", "mappedFundCode", "orderNotes", "quantity", "ybpOrderKey", "orderPlaced", "initials", "startWithVolume" })
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public static class OrderDetail {

                @XmlElement(name = "BatchPONumber")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String batchPONumber;

                @XmlElement(name = "ItemPONumber")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String itemPONumber;

                @XmlElement(name = "FundCode")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String fundCode;

                @XmlElement(name = "MappedFundCode")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String mappedFundCode;

                @XmlElement(name = "OrderNotes")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String orderNotes;

                @XmlElement(name = "Quantity", required = true)
                @XmlSchemaType(name = "positiveInteger")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected BigInteger quantity;

                @XmlElement(name = "YBPOrderKey", required = true)
                @XmlSchemaType(name = "positiveInteger")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected BigInteger ybpOrderKey;

                @XmlElement(name = "OrderPlaced", required = true)
                @XmlSchemaType(name = "dateTime")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected XMLGregorianCalendar orderPlaced;

                @XmlElement(name = "Initials")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String initials;

                @XmlElement(name = "StartWithVolume")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String startWithVolume;

                /**
                 * Gets the value of the batchPONumber property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getBatchPONumber() {
                    return batchPONumber;
                }

                /**
                 * Sets the value of the batchPONumber property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setBatchPONumber(String value) {
                    this.batchPONumber = value;
                }

                /**
                 * Gets the value of the itemPONumber property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getItemPONumber() {
                    return itemPONumber;
                }

                /**
                 * Sets the value of the itemPONumber property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setItemPONumber(String value) {
                    this.itemPONumber = value;
                }

                /**
                 * Gets the value of the fundCode property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getFundCode() {
                    return fundCode;
                }

                /**
                 * Sets the value of the fundCode property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setFundCode(String value) {
                    this.fundCode = value;
                }

                /**
                 * Gets the value of the mappedFundCode property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getMappedFundCode() {
                    return mappedFundCode;
                }

                /**
                 * Sets the value of the mappedFundCode property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setMappedFundCode(String value) {
                    this.mappedFundCode = value;
                }

                /**
                 * Gets the value of the orderNotes property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getOrderNotes() {
                    return orderNotes;
                }

                /**
                 * Sets the value of the orderNotes property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setOrderNotes(String value) {
                    this.orderNotes = value;
                }

                /**
                 * Gets the value of the quantity property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public BigInteger getQuantity() {
                    return quantity;
                }

                /**
                 * Sets the value of the quantity property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setQuantity(BigInteger value) {
                    this.quantity = value;
                }

                /**
                 * Gets the value of the ybpOrderKey property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public BigInteger getYBPOrderKey() {
                    return ybpOrderKey;
                }

                /**
                 * Sets the value of the ybpOrderKey property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setYBPOrderKey(BigInteger value) {
                    this.ybpOrderKey = value;
                }

                /**
                 * Gets the value of the orderPlaced property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link XMLGregorianCalendar }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public XMLGregorianCalendar getOrderPlaced() {
                    return orderPlaced;
                }

                /**
                 * Sets the value of the orderPlaced property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link XMLGregorianCalendar }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setOrderPlaced(XMLGregorianCalendar value) {
                    this.orderPlaced = value;
                }

                /**
                 * Gets the value of the initials property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getInitials() {
                    return initials;
                }

                /**
                 * Sets the value of the initials property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setInitials(String value) {
                    this.initials = value;
                }

                /**
                 * Gets the value of the startWithVolume property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getStartWithVolume() {
                    return startWithVolume;
                }

                /**
                 * Sets the value of the startWithVolume property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setStartWithVolume(String value) {
                    this.startWithVolume = value;
                }
            }
        }

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
         *         &lt;element ref="{}collection"/>
         *         &lt;element name="OrderDetail">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
         *                   &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
         *                   &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
         *                   &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
         *                   &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
         *                   &lt;element name="OtherLocalId" type="{}OtherLocalId" minOccurs="0"/>
         *                   &lt;element name="Location" type="{}Location" minOccurs="0"/>
         *                   &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
         *                   &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
         *                   &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
         *                   &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
         *                   &lt;element name="ListPrice" minOccurs="0">
         *                     &lt;complexType>
         *                       &lt;complexContent>
         *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                           &lt;sequence>
         *                             &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
         *                             &lt;element name="Currency" type="{}Currency"/>
         *                           &lt;/sequence>
         *                         &lt;/restriction>
         *                       &lt;/complexContent>
         *                     &lt;/complexType>
         *                   &lt;/element>
         *                   &lt;element name="LocalData" maxOccurs="unbounded" minOccurs="0">
         *                     &lt;complexType>
         *                       &lt;complexContent>
         *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                           &lt;sequence>
         *                             &lt;element name="Description" type="{}LD-Description"/>
         *                             &lt;element name="Value" type="{}LD-Value"/>
         *                           &lt;/sequence>
         *                         &lt;/restriction>
         *                       &lt;/complexContent>
         *                     &lt;/complexType>
         *                   &lt;/element>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = { "collection", "orderDetail" })
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public static class UnlistedPrintMonograph {

            @XmlElement(required = true, nillable = true)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            protected CollectionType collection;

            @XmlElement(name = "OrderDetail", required = true)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            protected PurchaseOrder.Order.UnlistedPrintMonograph.OrderDetail orderDetail;

            /**
             * This is the MARC record object container.
             * 
             * @return
             *     possible object is
             *     {@link CollectionType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public CollectionType getCollection() {
                return collection;
            }

            /**
             * Sets the value of the collection property.
             * 
             * @param value
             *     allowed object is
             *     {@link CollectionType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public void setCollection(CollectionType value) {
                this.collection = value;
            }

            /**
             * Gets the value of the orderDetail property.
             * 
             * @return
             *     possible object is
             *     {@link PurchaseOrder.Order.UnlistedPrintMonograph.OrderDetail }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public PurchaseOrder.Order.UnlistedPrintMonograph.OrderDetail getOrderDetail() {
                return orderDetail;
            }

            /**
             * Sets the value of the orderDetail property.
             * 
             * @param value
             *     allowed object is
             *     {@link PurchaseOrder.Order.UnlistedPrintMonograph.OrderDetail }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public void setOrderDetail(PurchaseOrder.Order.UnlistedPrintMonograph.OrderDetail value) {
                this.orderDetail = value;
            }

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
             *         &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
             *         &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
             *         &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
             *         &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
             *         &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
             *         &lt;element name="OtherLocalId" type="{}OtherLocalId" minOccurs="0"/>
             *         &lt;element name="Location" type="{}Location" minOccurs="0"/>
             *         &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
             *         &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
             *         &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
             *         &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
             *         &lt;element name="ListPrice" minOccurs="0">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;sequence>
             *                   &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
             *                   &lt;element name="Currency" type="{}Currency"/>
             *                 &lt;/sequence>
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *         &lt;element name="LocalData" maxOccurs="unbounded" minOccurs="0">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;sequence>
             *                   &lt;element name="Description" type="{}LD-Description"/>
             *                   &lt;element name="Value" type="{}LD-Value"/>
             *                 &lt;/sequence>
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = { "batchPONumber", "itemPONumber", "fundCode", "mappedFundCode", "orderNotes", "otherLocalId", "location", "quantity", "ybpOrderKey", "orderPlaced", "initials", "listPrice", "localData" })
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public static class OrderDetail {

                @XmlElement(name = "BatchPONumber")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String batchPONumber;

                @XmlElement(name = "ItemPONumber")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String itemPONumber;

                @XmlElement(name = "FundCode")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String fundCode;

                @XmlElement(name = "MappedFundCode")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String mappedFundCode;

                @XmlElement(name = "OrderNotes")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String orderNotes;

                @XmlElement(name = "OtherLocalId")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String otherLocalId;

                @XmlElement(name = "Location")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String location;

                @XmlElement(name = "Quantity", required = true)
                @XmlSchemaType(name = "positiveInteger")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected BigInteger quantity;

                @XmlElement(name = "YBPOrderKey", required = true)
                @XmlSchemaType(name = "positiveInteger")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected BigInteger ybpOrderKey;

                @XmlElement(name = "OrderPlaced", required = true)
                @XmlSchemaType(name = "dateTime")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected XMLGregorianCalendar orderPlaced;

                @XmlElement(name = "Initials")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String initials;

                @XmlElement(name = "ListPrice")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected PurchaseOrder.Order.UnlistedPrintMonograph.OrderDetail.ListPrice listPrice;

                @XmlElement(name = "LocalData")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected List<PurchaseOrder.Order.UnlistedPrintMonograph.OrderDetail.LocalData> localData;

                /**
                 * Gets the value of the batchPONumber property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getBatchPONumber() {
                    return batchPONumber;
                }

                /**
                 * Sets the value of the batchPONumber property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setBatchPONumber(String value) {
                    this.batchPONumber = value;
                }

                /**
                 * Gets the value of the itemPONumber property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getItemPONumber() {
                    return itemPONumber;
                }

                /**
                 * Sets the value of the itemPONumber property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setItemPONumber(String value) {
                    this.itemPONumber = value;
                }

                /**
                 * Gets the value of the fundCode property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getFundCode() {
                    return fundCode;
                }

                /**
                 * Sets the value of the fundCode property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setFundCode(String value) {
                    this.fundCode = value;
                }

                /**
                 * Gets the value of the mappedFundCode property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getMappedFundCode() {
                    return mappedFundCode;
                }

                /**
                 * Sets the value of the mappedFundCode property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setMappedFundCode(String value) {
                    this.mappedFundCode = value;
                }

                /**
                 * Gets the value of the orderNotes property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getOrderNotes() {
                    return orderNotes;
                }

                /**
                 * Sets the value of the orderNotes property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setOrderNotes(String value) {
                    this.orderNotes = value;
                }

                /**
                 * Gets the value of the otherLocalId property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getOtherLocalId() {
                    return otherLocalId;
                }

                /**
                 * Sets the value of the otherLocalId property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setOtherLocalId(String value) {
                    this.otherLocalId = value;
                }

                /**
                 * Gets the value of the location property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getLocation() {
                    return location;
                }

                /**
                 * Sets the value of the location property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setLocation(String value) {
                    this.location = value;
                }

                /**
                 * Gets the value of the quantity property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public BigInteger getQuantity() {
                    return quantity;
                }

                /**
                 * Sets the value of the quantity property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setQuantity(BigInteger value) {
                    this.quantity = value;
                }

                /**
                 * Gets the value of the ybpOrderKey property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public BigInteger getYBPOrderKey() {
                    return ybpOrderKey;
                }

                /**
                 * Sets the value of the ybpOrderKey property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setYBPOrderKey(BigInteger value) {
                    this.ybpOrderKey = value;
                }

                /**
                 * Gets the value of the orderPlaced property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link XMLGregorianCalendar }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public XMLGregorianCalendar getOrderPlaced() {
                    return orderPlaced;
                }

                /**
                 * Sets the value of the orderPlaced property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link XMLGregorianCalendar }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setOrderPlaced(XMLGregorianCalendar value) {
                    this.orderPlaced = value;
                }

                /**
                 * Gets the value of the initials property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getInitials() {
                    return initials;
                }

                /**
                 * Sets the value of the initials property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setInitials(String value) {
                    this.initials = value;
                }

                /**
                 * Gets the value of the listPrice property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link PurchaseOrder.Order.UnlistedPrintMonograph.OrderDetail.ListPrice }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public PurchaseOrder.Order.UnlistedPrintMonograph.OrderDetail.ListPrice getListPrice() {
                    return listPrice;
                }

                /**
                 * Sets the value of the listPrice property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link PurchaseOrder.Order.UnlistedPrintMonograph.OrderDetail.ListPrice }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setListPrice(PurchaseOrder.Order.UnlistedPrintMonograph.OrderDetail.ListPrice value) {
                    this.listPrice = value;
                }

                /**
                 * Gets the value of the localData property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the localData property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getLocalData().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link PurchaseOrder.Order.UnlistedPrintMonograph.OrderDetail.LocalData }
                 * 
                 * 
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public List<PurchaseOrder.Order.UnlistedPrintMonograph.OrderDetail.LocalData> getLocalData() {
                    if (localData == null) {
                        localData = new ArrayList<PurchaseOrder.Order.UnlistedPrintMonograph.OrderDetail.LocalData>();
                    }
                    return this.localData;
                }

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
                 *         &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
                 *         &lt;element name="Currency" type="{}Currency"/>
                 *       &lt;/sequence>
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = { "amount", "currency" })
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public static class ListPrice {

                    @XmlElement(name = "Amount", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected BigDecimal amount;

                    @XmlElement(name = "Currency", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String currency;

                    /**
                     * Gets the value of the amount property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link BigDecimal }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public BigDecimal getAmount() {
                        return amount;
                    }

                    /**
                     * Sets the value of the amount property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link BigDecimal }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setAmount(BigDecimal value) {
                        this.amount = value;
                    }

                    /**
                     * Gets the value of the currency property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public String getCurrency() {
                        return currency;
                    }

                    /**
                     * Sets the value of the currency property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setCurrency(String value) {
                        this.currency = value;
                    }
                }

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
                 *         &lt;element name="Description" type="{}LD-Description"/>
                 *         &lt;element name="Value" type="{}LD-Value"/>
                 *       &lt;/sequence>
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = { "description", "value" })
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public static class LocalData {

                    @XmlElement(name = "Description", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String description;

                    @XmlElement(name = "Value", required = true)
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    protected String value;

                    /**
                     * Gets the value of the description property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public String getDescription() {
                        return description;
                    }

                    /**
                     * Sets the value of the description property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                    public void setDescription(String value) {
                        this.description = value;
                    }

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
                }
            }
        }

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
         *         &lt;element ref="{}collection"/>
         *         &lt;element name="OrderDetail">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
         *                   &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
         *                   &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
         *                   &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
         *                   &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
         *                   &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
         *                   &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
         *                   &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
         *                   &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
         *                   &lt;element name="StartWithVolume" type="{}StartWithVolume" minOccurs="0"/>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = { "collection", "orderDetail" })
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
        public static class UnlistedPrintSerial {

            @XmlElement(required = true, nillable = true)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            protected CollectionType collection;

            @XmlElement(name = "OrderDetail", required = true)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            protected PurchaseOrder.Order.UnlistedPrintSerial.OrderDetail orderDetail;

            /**
             * This is the MARC record object container.
             * 
             * @return
             *     possible object is
             *     {@link CollectionType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public CollectionType getCollection() {
                return collection;
            }

            /**
             * Sets the value of the collection property.
             * 
             * @param value
             *     allowed object is
             *     {@link CollectionType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public void setCollection(CollectionType value) {
                this.collection = value;
            }

            /**
             * Gets the value of the orderDetail property.
             * 
             * @return
             *     possible object is
             *     {@link PurchaseOrder.Order.UnlistedPrintSerial.OrderDetail }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public PurchaseOrder.Order.UnlistedPrintSerial.OrderDetail getOrderDetail() {
                return orderDetail;
            }

            /**
             * Sets the value of the orderDetail property.
             * 
             * @param value
             *     allowed object is
             *     {@link PurchaseOrder.Order.UnlistedPrintSerial.OrderDetail }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public void setOrderDetail(PurchaseOrder.Order.UnlistedPrintSerial.OrderDetail value) {
                this.orderDetail = value;
            }

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
             *         &lt;element name="BatchPONumber" type="{}BatchPONumber" minOccurs="0"/>
             *         &lt;element name="ItemPONumber" type="{}ItemPONumber" minOccurs="0"/>
             *         &lt;element name="FundCode" type="{}FundCode" minOccurs="0"/>
             *         &lt;element name="MappedFundCode" type="{}MappedFundCode" minOccurs="0"/>
             *         &lt;element name="OrderNotes" type="{}OrderNotes" minOccurs="0"/>
             *         &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
             *         &lt;element name="YBPOrderKey" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
             *         &lt;element name="OrderPlaced" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
             *         &lt;element name="Initials" type="{}Initials" minOccurs="0"/>
             *         &lt;element name="StartWithVolume" type="{}StartWithVolume" minOccurs="0"/>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = { "batchPONumber", "itemPONumber", "fundCode", "mappedFundCode", "orderNotes", "quantity", "ybpOrderKey", "orderPlaced", "initials", "startWithVolume" })
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
            public static class OrderDetail {

                @XmlElement(name = "BatchPONumber")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String batchPONumber;

                @XmlElement(name = "ItemPONumber")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String itemPONumber;

                @XmlElement(name = "FundCode")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String fundCode;

                @XmlElement(name = "MappedFundCode")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String mappedFundCode;

                @XmlElement(name = "OrderNotes")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String orderNotes;

                @XmlElement(name = "Quantity", required = true)
                @XmlSchemaType(name = "positiveInteger")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected BigInteger quantity;

                @XmlElement(name = "YBPOrderKey", required = true)
                @XmlSchemaType(name = "positiveInteger")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected BigInteger ybpOrderKey;

                @XmlElement(name = "OrderPlaced", required = true)
                @XmlSchemaType(name = "dateTime")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected XMLGregorianCalendar orderPlaced;

                @XmlElement(name = "Initials")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String initials;

                @XmlElement(name = "StartWithVolume")
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                protected String startWithVolume;

                /**
                 * Gets the value of the batchPONumber property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getBatchPONumber() {
                    return batchPONumber;
                }

                /**
                 * Sets the value of the batchPONumber property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setBatchPONumber(String value) {
                    this.batchPONumber = value;
                }

                /**
                 * Gets the value of the itemPONumber property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getItemPONumber() {
                    return itemPONumber;
                }

                /**
                 * Sets the value of the itemPONumber property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setItemPONumber(String value) {
                    this.itemPONumber = value;
                }

                /**
                 * Gets the value of the fundCode property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getFundCode() {
                    return fundCode;
                }

                /**
                 * Sets the value of the fundCode property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setFundCode(String value) {
                    this.fundCode = value;
                }

                /**
                 * Gets the value of the mappedFundCode property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getMappedFundCode() {
                    return mappedFundCode;
                }

                /**
                 * Sets the value of the mappedFundCode property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setMappedFundCode(String value) {
                    this.mappedFundCode = value;
                }

                /**
                 * Gets the value of the orderNotes property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getOrderNotes() {
                    return orderNotes;
                }

                /**
                 * Sets the value of the orderNotes property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setOrderNotes(String value) {
                    this.orderNotes = value;
                }

                /**
                 * Gets the value of the quantity property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public BigInteger getQuantity() {
                    return quantity;
                }

                /**
                 * Sets the value of the quantity property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setQuantity(BigInteger value) {
                    this.quantity = value;
                }

                /**
                 * Gets the value of the ybpOrderKey property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public BigInteger getYBPOrderKey() {
                    return ybpOrderKey;
                }

                /**
                 * Sets the value of the ybpOrderKey property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setYBPOrderKey(BigInteger value) {
                    this.ybpOrderKey = value;
                }

                /**
                 * Gets the value of the orderPlaced property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link XMLGregorianCalendar }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public XMLGregorianCalendar getOrderPlaced() {
                    return orderPlaced;
                }

                /**
                 * Sets the value of the orderPlaced property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link XMLGregorianCalendar }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setOrderPlaced(XMLGregorianCalendar value) {
                    this.orderPlaced = value;
                }

                /**
                 * Gets the value of the initials property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getInitials() {
                    return initials;
                }

                /**
                 * Sets the value of the initials property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setInitials(String value) {
                    this.initials = value;
                }

                /**
                 * Gets the value of the startWithVolume property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public String getStartWithVolume() {
                    return startWithVolume;
                }

                /**
                 * Sets the value of the startWithVolume property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", date = "2018-07-13T02:20:23-04:00", comments = "JAXB RI vhudson-jaxb-ri-2.1-520")
                public void setStartWithVolume(String value) {
                    this.startWithVolume = value;
                }
            }
        }
    }
}
