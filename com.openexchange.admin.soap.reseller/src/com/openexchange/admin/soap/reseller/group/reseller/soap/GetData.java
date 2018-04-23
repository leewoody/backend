
package com.openexchange.admin.soap.reseller.group.reseller.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.reseller.group.reseller.soap.dataobjects.ResellerContext;
import com.openexchange.admin.soap.reseller.group.rmi.dataobjects.Credentials;
import com.openexchange.admin.soap.reseller.group.soap.dataobjects.Group;


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
 *         &lt;element name="ctx" type="{http://dataobjects.soap.reseller.admin.openexchange.com/xsd}ResellerContext" minOccurs="0"/>
 *         &lt;element name="grp" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Group" minOccurs="0"/>
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
    "ctx",
    "grp",
    "auth"
})
@XmlRootElement(name = "getData")
public class GetData {

    @XmlElement(nillable = true)
    protected ResellerContext ctx;
    @XmlElement(nillable = true)
    protected Group grp;
    @XmlElement(nillable = true)
    protected Credentials auth;

    /**
     * Ruft den Wert der ctx-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link ResellerContext }
     *
     */
    public ResellerContext getCtx() {
        return ctx;
    }

    /**
     * Legt den Wert der ctx-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link ResellerContext }
     *
     */
    public void setCtx(ResellerContext value) {
        this.ctx = value;
    }

    /**
     * Ruft den Wert der grp-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Group }
     *
     */
    public Group getGrp() {
        return grp;
    }

    /**
     * Legt den Wert der grp-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Group }
     *
     */
    public void setGrp(Group value) {
        this.grp = value;
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
