package org.fenixedu.bennu;

import org.fenixedu.commons.configuration.ConfigurationInvocationHandler;
import org.fenixedu.commons.configuration.ConfigurationManager;
import org.fenixedu.commons.configuration.ConfigurationProperty;

import pt.ist.renates.domain.DefaultInstitutionCodeProvider;
import pt.ist.renates.domain.InstitutionCodeProvider;

public class RenatesIntegrationConfiguration {

    private static InstitutionCodeProvider institutionCodeProvider;

    @ConfigurationManager(description = "Renates Integration Configuration")
    public interface ConfigurationProperties {
        @ConfigurationProperty(key = "renates.establishment.code", defaultValue = "0000")
        public String getEstablishmentCode();

        @ConfigurationProperty(key = "renates.organic.unit.code", defaultValue = "0000")
        public String getOrganicUnitCode();

        @ConfigurationProperty(key = "renates.orientator.info.request.url")
        public String getOrientatorsInfoURL();

        @ConfigurationProperty(key = "renates.orientator.info.request.header.key")
        public String getOrientatorsInfoURLHeaderKey();

        @ConfigurationProperty(key = "renates.orientator.info.request.header.value")
        public String getOrientatorsInfoURLHeaderValue();

    }

    public static ConfigurationProperties getConfiguration() {
        return ConfigurationInvocationHandler.getConfiguration(ConfigurationProperties.class);
    }

    public static InstitutionCodeProvider getInstitutionCodeProvider() {
        if (institutionCodeProvider == null) {
            setInstitutionCodeProvider(new DefaultInstitutionCodeProvider());
        }
        return institutionCodeProvider;
    }

    public static void setInstitutionCodeProvider(InstitutionCodeProvider institutionCodeProvider) {
        RenatesIntegrationConfiguration.institutionCodeProvider = institutionCodeProvider;
    }
}
