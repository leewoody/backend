
package com.openexchange.admin.soap.reseller.service.reseller.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.reseller.service.reseller.rmi.dataobjects.ResellerAdmin;


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
 *         &lt;element name="return" type="{http://dataobjects.rmi.reseller.admin.openexchange.com/xsd}ResellerAdmin" minOccurs="0"/>
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
    "_return"
})
@XmlRootElement(name = "createResponse")
public class CreateResponse {

    @XmlElement(name = "return", nillable = true)
    protected ResellerAdmin _return;

    /**
     * Ruft den Wert der return-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link ResellerAdmin }
     *
     */
    public ResellerAdmin getReturn() {
        return _return;
    }

    /**
     * Legt den Wert der return-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link ResellerAdmin }
     *
     */
    public void setReturn(ResellerAdmin value) {
        this._return = value;
    }

}
