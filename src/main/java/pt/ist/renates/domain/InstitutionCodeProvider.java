package pt.ist.renates.domain;

import org.fenixedu.academic.domain.DegreeCurricularPlan;

public interface InstitutionCodeProvider {

    public String getEstablishmentCode();

    public String getOrganicUnitCode(DegreeCurricularPlan degreeCurricularPlan);

}
