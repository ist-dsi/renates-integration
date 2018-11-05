package pt.ist.renates.domain.thesis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
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
import org.fenixedu.bennu.RenatesIntegrationConfiguration;
import org.fenixedu.bennu.core.domain.Bennu;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import pt.ist.renates.domain.beans.OrientadorBean;
import pt.ist.renates.domain.thesis.exceptions.InternalThesisIdException;

public class RenatesUtil {

    private static Comparator<ConclusionProcess> COMPARATOR_BY_CONCLUSION_DATE = new Comparator<ConclusionProcess>() {
        @Override
        public int compare(final ConclusionProcess c1, final ConclusionProcess c2) {
            int result = c1.getConclusionDate().compareTo(c2.getConclusionDate());
            return result == 0 ? c1.getExternalId().compareTo(c2.getExternalId()) : result;
        }
    };

    public static String getThesisId(Thesis thesis) throws InternalThesisIdException {
        List<String> errors = new ArrayList<>();

        if (thesis.getStudent() == null) {
            errors.add("A tese nao esta associada a um aluno");
        }

        if (thesis.getEnrolment() == null) {
            errors.add("A tese nao esta associada a uma matricula");
        }

        if (thesis.getApproval() == null) {
            errors.add("A tese nao tem data de aprovacao");
        }

        String error_description = Joiner.on("\n").join(errors);

        if (!Strings.isNullOrEmpty(error_description)) {
            throw new InternalThesisIdException(error_description);
        }

        String istId = thesis.getStudent().getPerson().getUsername();
        String course = thesis.getEnrolment().getDegreeCurricularPlanOfStudent().getDegree().getSigla();
        String approval =
                Integer.toString(thesis.getApproval().getYear()) + Integer.toString(thesis.getApproval().getMonthOfYear());

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

    public static SortedMap<ConclusionProcess, Thesis> getRenatesThesisAndConclusionProcess() {
        SortedMap<ConclusionProcess, Thesis> thesisSet = new TreeMap<ConclusionProcess, Thesis>(COMPARATOR_BY_CONCLUSION_DATE);
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

            thesisSet.put(conclusionProcess, thesis);

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

    public static Map<String, OrientadorBean> getOrientatorsInfo() {

        String orcid_info_url = RenatesIntegrationConfiguration.getConfiguration().getOrientatorsInfoURL();

        String extra_header_value = RenatesIntegrationConfiguration.getConfiguration().getOrientatorsInfoURLHeaderValue();

        try {
            URL url = new URL(orcid_info_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", extra_header_value);

            Gson gson = new GsonBuilder().create();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            String jsonString = "";

            while ((output = br.readLine()) != null) {
                jsonString += output + "\n";
            }

            List<OrientadorBean> orientadorBeans = gson.fromJson(jsonString, new TypeToken<List<OrientadorBean>>() {
            }.getType());

            conn.disconnect();

            Map<String, OrientadorBean> result =
                    orientadorBeans.stream().collect(Collectors.toMap(OrientadorBean::getIstId, Function.identity()));

            return result;

        } catch (IOException e) {
            return new HashMap<String, OrientadorBean>();
        }
    }

}
