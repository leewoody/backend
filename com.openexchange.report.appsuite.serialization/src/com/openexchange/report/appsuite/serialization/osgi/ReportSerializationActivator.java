
package com.openexchange.report.appsuite.serialization.osgi;

import static com.openexchange.report.appsuite.serialization.osgi.StringParserServiceRegistry.getServiceRegistry;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.report.appsuite.serialization.internal.Services;
import com.openexchange.tools.strings.StringParser;

/**
 * Activates the serialization for the Report bundle by registering the {@link PortableReportFactory}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class ReportSerializationActivator extends HousekeepingActivator {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {
            StringParser.class, ConfigurationService.class
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleAvailability(final Class<?> clazz) {
        getServiceRegistry().addService(clazz, getService(clazz));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        getServiceRegistry().removeService(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startBundle() throws Exception {
        final ServiceRegistry registry = getServiceRegistry();
        registry.clearRegistry();
        final Class<?>[] classes = getNeededServices();
        for (final Class<?> classe : classes) {
            final Object service = getService(classe);
            if (null != service) {
                registry.addService(classe, service);
            }
        }
        Services.setServices(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stopBundle() throws Exception {
        Services.setServices(null);
        getServiceRegistry().clearRegistry();
    }
}
