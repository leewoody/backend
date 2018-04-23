
package com.openexchange.admin.soap.context.soap;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.6.0
 * 2012-06-01T18:39:07.966+02:00
 * Generated source version: 2.6.0
 */

@WebFault(name = "NoSuchReasonException", targetNamespace = "http://soap.admin.openexchange.com")
public class NoSuchReasonException_Exception extends java.lang.Exception {

    private com.openexchange.admin.soap.context.soap.NoSuchReasonException noSuchReasonException;

    public NoSuchReasonException_Exception() {
        super();
    }

    public NoSuchReasonException_Exception(String message) {
        super(message);
    }

    public NoSuchReasonException_Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchReasonException_Exception(String message, com.openexchange.admin.soap.context.soap.NoSuchReasonException noSuchReasonException) {
        super(message);
        this.noSuchReasonException = noSuchReasonException;
    }

    public NoSuchReasonException_Exception(String message, com.openexchange.admin.soap.context.soap.NoSuchReasonException noSuchReasonException, Throwable cause) {
        super(message, cause);
        this.noSuchReasonException = noSuchReasonException;
    }

    public com.openexchange.admin.soap.context.soap.NoSuchReasonException getFaultInfo() {
        return this.noSuchReasonException;
    }
}