//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.07.18 at 10:18:47 AM IST 
//


package in.gov.uidai.authentication.uid_auth_request._1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Uses complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Uses"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="pi" use="required" type="{http://www.uidai.gov.in/authentication/uid-auth-request/1.0}UsesFlag" /&gt;
 *       &lt;attribute name="pa" use="required" type="{http://www.uidai.gov.in/authentication/uid-auth-request/1.0}UsesFlag" /&gt;
 *       &lt;attribute name="pfa" use="required" type="{http://www.uidai.gov.in/authentication/uid-auth-request/1.0}UsesFlag" /&gt;
 *       &lt;attribute name="bio" use="required" type="{http://www.uidai.gov.in/authentication/uid-auth-request/1.0}UsesFlag" /&gt;
 *       &lt;attribute name="bt" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="pin" use="required" type="{http://www.uidai.gov.in/authentication/uid-auth-request/1.0}UsesFlag" /&gt;
 *       &lt;attribute name="otp" use="required" type="{http://www.uidai.gov.in/authentication/uid-auth-request/1.0}UsesFlag" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Uses")
public class Uses {

    @XmlAttribute(name = "pi", required = true)
    protected UsesFlag pi;
    @XmlAttribute(name = "pa", required = true)
    protected UsesFlag pa;
    @XmlAttribute(name = "pfa", required = true)
    protected UsesFlag pfa;
    @XmlAttribute(name = "bio", required = true)
    protected UsesFlag bio;
    @XmlAttribute(name = "bt")
    protected String bt;
    @XmlAttribute(name = "pin", required = true)
    protected UsesFlag pin;
    @XmlAttribute(name = "otp", required = true)
    protected UsesFlag otp;

    /**
     * Gets the value of the pi property.
     * 
     * @return
     *     possible object is
     *     {@link UsesFlag }
     *     
     */
    public UsesFlag getPi() {
        return pi;
    }

    /**
     * Sets the value of the pi property.
     * 
     * @param value
     *     allowed object is
     *     {@link UsesFlag }
     *     
     */
    public void setPi(UsesFlag value) {
        this.pi = value;
    }

    /**
     * Gets the value of the pa property.
     * 
     * @return
     *     possible object is
     *     {@link UsesFlag }
     *     
     */
    public UsesFlag getPa() {
        return pa;
    }

    /**
     * Sets the value of the pa property.
     * 
     * @param value
     *     allowed object is
     *     {@link UsesFlag }
     *     
     */
    public void setPa(UsesFlag value) {
        this.pa = value;
    }

    /**
     * Gets the value of the pfa property.
     * 
     * @return
     *     possible object is
     *     {@link UsesFlag }
     *     
     */
    public UsesFlag getPfa() {
        return pfa;
    }

    /**
     * Sets the value of the pfa property.
     * 
     * @param value
     *     allowed object is
     *     {@link UsesFlag }
     *     
     */
    public void setPfa(UsesFlag value) {
        this.pfa = value;
    }

    /**
     * Gets the value of the bio property.
     * 
     * @return
     *     possible object is
     *     {@link UsesFlag }
     *     
     */
    public UsesFlag getBio() {
        return bio;
    }

    /**
     * Sets the value of the bio property.
     * 
     * @param value
     *     allowed object is
     *     {@link UsesFlag }
     *     
     */
    public void setBio(UsesFlag value) {
        this.bio = value;
    }

    /**
     * Gets the value of the bt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBt() {
        return bt;
    }

    /**
     * Sets the value of the bt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBt(String value) {
        this.bt = value;
    }

    /**
     * Gets the value of the pin property.
     * 
     * @return
     *     possible object is
     *     {@link UsesFlag }
     *     
     */
    public UsesFlag getPin() {
        return pin;
    }

    /**
     * Sets the value of the pin property.
     * 
     * @param value
     *     allowed object is
     *     {@link UsesFlag }
     *     
     */
    public void setPin(UsesFlag value) {
        this.pin = value;
    }

    /**
     * Gets the value of the otp property.
     * 
     * @return
     *     possible object is
     *     {@link UsesFlag }
     *     
     */
    public UsesFlag getOtp() {
        return otp;
    }

    /**
     * Sets the value of the otp property.
     * 
     * @param value
     *     allowed object is
     *     {@link UsesFlag }
     *     
     */
    public void setOtp(UsesFlag value) {
        this.otp = value;
    }

}
