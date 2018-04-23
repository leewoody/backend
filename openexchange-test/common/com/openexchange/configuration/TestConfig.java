/**
 *
 */

package com.openexchange.configuration;

import com.openexchange.exception.OXException;
import com.openexchange.tools.conf.AbstractConfig;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TestConfig extends AbstractConfig {

    /**
     * Singleton.
     */
    private static TestConfig singleton;

    /**
     * Prevent instantiation
     */
    private TestConfig() {
        super();
    }

    /**
     * Key of the system property that contains the file name of the
     * system.properties configuration file.
     */
    private static final String KEY = "test.propfile";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPropertyFileName() throws OXException {
        String fileName = System.getProperty(KEY);
        if (null == fileName) {
            fileName = "conf/test.properties";
        }
        return fileName;
    }

    /**
     * Reads the configuration.
     * 
     * @throws OXException if reading configuration fails.
     */
    public static void init() throws OXException {
        if (null == singleton) {
            synchronized (TestConfig.class) {
                if (null == singleton) {
                    singleton = new TestConfig();
                    singleton.loadPropertiesInternal();
                }
            }
        }
    }

    public static String getProperty(final Property key) {
        return singleton.getPropertyInternal(key.getPropertyName());
    }

    /**
     * Enumeration of all properties in the test.properties file.
     */
    public static enum Property {
        /**
         * ajax.properties
         */
        AJAX_PROPS("ajaxPropertiesFile"),
        /**
         * provisioning.properties
         */
        PROV_PROPS("provisioningFile"),
        /**
         * googletest.properties
         */
        GOOGLE_PROPS("googlePropertiesFile"),

        FILESTORE("filestoreId");

        /**
         * Name of the property in the test.properties file.
         */
        private String propertyName;

        /**
         * Default constructor.
         * 
         * @param propertyName Name of the property in the test.properties
         *            file.
         */
        private Property(final String propertyName) {
            this.propertyName = propertyName;
        }

        /**
         * @return the propertyName
         */
        public String getPropertyName() {
            return propertyName;
        }
    }
}
