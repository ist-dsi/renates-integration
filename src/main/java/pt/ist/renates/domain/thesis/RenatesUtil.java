package pt.ist.renates.domain.thesis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.degreeStructure.CycleCourseGroup;
import org.fenixedu.academic.domain.student.curriculum.ConclusionProcess;
import org.fenixedu.academic.domain.studentCurriculum.CurriculumGroup;
import org.fenixedu.academic.domain.studentCurriculum.CurriculumLine;
import org.fenixedu.academic.domain.studentCurriculum.Dismissal;
import org.fenixedu.academic.domain.studentCurriculum.Substitution;
import org.fenixedu.academic.domain.thesis.Thesis;
import org.fenixedu.bennu.core.domain.Bennu;

import com.google.common.base.Joiner;

public class RenatesUtil {

    public static String getThesisId(Thesis thesis) {
        String istId = thesis.getStudent() == null ? thesis.getExternalId() : thesis.getStudent().getPerson().getUsername();
        String course = thesis.getEnrolment() == null ? thesis.getExternalId() : thesis.getEnrolment()
                .getDegreeCurricularPlanOfStudent().getDegree().getSigla();
        String approval = thesis.getApproval() == null ? thesis.getExternalId() : Integer.toString(thesis.getApproval().getYear())
                + Integer.toString(thesis.getApproval().getMonthOfYear());

        return Joiner.on("/").join(approval, course, istId);
    }

    public static Set<Thesis> getRenatesThesis() {
        Set<Thesis> thesisSet = new HashSet<Thesis>();
        Set<CurriculumGroup> collect =
                Bennu.getInstance().getProgramConclusionSet().stream().flatMap(pc -> pc.getCourseGroupSet().stream())
                        .filter(cg -> cg.isCycleCourseGroup() && ((CycleCourseGroup) cg).isSecondCycle())
                        .flatMap(cg -> cg.getCurriculumModulesSet().stream()).map(cm -> (CurriculumGroup) cm)
                        .filter(cm -> cm.isConclusionProcessed()).collect(Collectors.toSet());

        for (CurriculumGroup group : collect) {
            Enrolment thesisEnrolment = getThesisEnrolment(group);
            if (thesisEnrolment == null) {
                continue;
            }
            Thesis thesis = thesisEnrolment.getPossibleThesis();

            if (thesis == null) {
                continue;
            }

            thesisSet.add(thesis);

        }

        return thesisSet;
    }

    public static Map<Thesis, ConclusionProcess> getRenatesThesisAndConclusionProcess() {
        Map<Thesis, ConclusionProcess> thesisSet = new HashMap<Thesis, ConclusionProcess>();
        Set<CurriculumGroup> collect =
                Bennu.getInstance().getProgramConclusionSet().stream().flatMap(pc -> pc.getCourseGroupSet().stream())
                        .filter(cg -> cg.isCycleCourseGroup() && ((CycleCourseGroup) cg).isSecondCycle())
                        .flatMap(cg -> cg.getCurriculumModulesSet().stream()).map(cm -> (CurriculumGroup) cm)
                        .filter(cm -> cm.isConclusionProcessed()).collect(Collectors.toSet());

        for (CurriculumGroup group : collect) {
            Enrolment thesisEnrolment = getThesisEnrolment(group);
            if (thesisEnrolment == null) {
                continue;
            }
            Thesis thesis = thesisEnrolment.getPossibleThesis();
            ConclusionProcess conclusionProcess = group.getConclusionProcess();

            if (thesis == null) {
                continue;
            }

            thesisSet.put(thesis, conclusionProcess);

        }

        return thesisSet;
    }

    public static Enrolment getThesisEnrolment(CurriculumGroup group) {
        Predicate<CurriculumLine> isConcludedEnrolment = e -> (e.isEnrolment() && ((Enrolment) e).isDissertation()
                && e.isApproved() && ((Enrolment) e).getPossibleThesis() != null
                && ((Enrolment) e).getPossibleThesis().isFinalAndApprovedThesis());

        Set<CurriculumLine> curriculumLines = group.getAllCurriculumLines().stream()
                .filter(e -> (e.isDismissal() && ((Dismissal) e).getCurricularCourse() != null
                        && ((Dismissal) e).getCurricularCourse().isDissertation()
                        && ((Dismissal) e).getCredits().isSubstitution()) || isConcludedEnrolment.test(e))
                .collect(Collectors.toSet());

        if (curriculumLines.isEmpty()) {
            return null;
        }

        for (CurriculumLine curriculumLine : curriculumLines) {
            if (curriculumLine instanceof Dismissal) {
                Dismissal dismissal = (Dismissal) curriculumLine;
                Substitution substitution = (Substitution) dismissal.getCredits();
                Enrolment enrolment =
                        (Enrolment) substitution.getEnrolmentsSet().stream().filter(e -> e.getIEnrolment().isEnrolment())
                                .filter(e -> isConcludedEnrolment.test((CurriculumLine) e.getIEnrolment()))
                                .map(e -> e.getIEnrolment()).findAny().orElse(null);
                if (enrolment != null) {
                    return enrolment;
                }
            }
            if (curriculumLine instanceof Enrolment) {
                return (Enrolment) curriculumLine;
            }
        }
        return null;
    }

}
