
package com.openexchange.admin.soap.reseller.context.reseller.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f\u00fcr anonymous complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DatabaseUpdateException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}DatabaseUpdateException" minOccurs="0"/>
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
    "databaseUpdateException"
})
@XmlRootElement(name = "DatabaseUpdateException")
public class DatabaseUpdateException {

    @XmlElement(name = "DatabaseUpdateException", nillable = true)
    protected com.openexchange.admin.soap.reseller.context.rmi.exceptions.DatabaseUpdateException databaseUpdateException;

    /**
     * Ruft den Wert der databaseUpdateException-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.reseller.context.rmi.exceptions.DatabaseUpdateException }
     *
     */
    public com.openexchange.admin.soap.reseller.context.rmi.exceptions.DatabaseUpdateException getDatabaseUpdateException() {
        return databaseUpdateException;
    }

    /**
     * Legt den Wert der databaseUpdateException-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.reseller.context.rmi.exceptions.DatabaseUpdateException }
     *
     */
    public void setDatabaseUpdateException(com.openexchange.admin.soap.reseller.context.rmi.exceptions.DatabaseUpdateException value) {
        this.databaseUpdateException = value;
    }

}
