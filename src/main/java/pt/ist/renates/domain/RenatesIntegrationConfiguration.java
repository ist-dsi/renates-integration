package pt.ist.renates.domain;

import org.fenixedu.commons.configuration.ConfigurationInvocationHandler;
import org.fenixedu.commons.configuration.ConfigurationManager;
import org.fenixedu.commons.configuration.ConfigurationProperty;

public class RenatesIntegrationConfiguration {

    private static InstitutionCodeProvider institutionCodeProvider;

    @ConfigurationManager(description = "FenixEdu Academic Configuration")
    public interface ConfigurationProperties {
        @ConfigurationProperty(key = "establishment.code", defaultValue = "0000")
        public String getEstablishmentCode();

        @ConfigurationProperty(key = "organic.unit.code", defaultValue = "0000")
        public String getOrganicUnitCode();
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
