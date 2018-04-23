
package com.openexchange.admin.soap.util.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.util.dataobjects.Credentials;
import com.openexchange.admin.soap.util.dataobjects.Database;


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
 *         &lt;element name="db" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Database" minOccurs="0"/>
 *         &lt;element name="auth" type="{http://dataobjects.rmi.admin.openexchange.com/xsd}Credentials" minOccurs="0"/>
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
    "db",
    "auth"
})
@XmlRootElement(name = "registerDatabase")
public class RegisterDatabase {

    @XmlElement(nillable = true)
    protected Database db;
    @XmlElement(nillable = true)
    protected Credentials auth;

    /**
     * Ruft den Wert der db-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Database }
     *
     */
    public Database getDb() {
        return db;
    }

    /**
     * Legt den Wert der db-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Database }
     *
     */
    public void setDb(Database value) {
        this.db = value;
    }

    /**
     * Ruft den Wert der auth-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Credentials }
     *
     */
    public Credentials getAuth() {
        return auth;
    }

    /**
     * Legt den Wert der auth-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Credentials }
     *
     */
    public void setAuth(Credentials value) {
        this.auth = value;
    }

}
