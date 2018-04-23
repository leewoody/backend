
package com.openexchange.oauth.provider.soap;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.6.16
 * 2015-04-14T10:21:14.827+02:00
 * Generated source version: 2.6.16
 */

@WebFault(name = "Exception", targetNamespace = "http://soap.provider.oauth.openexchange.com")
public class OAuthClientServiceException extends java.lang.Exception {

    private static final long serialVersionUID = -481741590153542549L;

    private com.openexchange.oauth.provider.soap.Exception exception;

    public OAuthClientServiceException() {
        super();
    }

    public OAuthClientServiceException(String message) {
        super(message);
    }

    public OAuthClientServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public OAuthClientServiceException(String message, com.openexchange.oauth.provider.soap.Exception exception) {
        super(message);
        this.exception = exception;
    }

    public OAuthClientServiceException(String message, com.openexchange.oauth.provider.soap.Exception exception, Throwable cause) {
        super(message, cause);
        this.exception = exception;
    }

    public com.openexchange.oauth.provider.soap.Exception getFaultInfo() {
        return this.exception;
    }
}