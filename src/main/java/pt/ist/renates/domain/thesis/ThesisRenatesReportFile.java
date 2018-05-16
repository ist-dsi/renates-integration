package pt.ist.renates.domain.thesis;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.QueueJobResult;
import org.fenixedu.academic.domain.QueueJobWithFile;
import org.fenixedu.academic.domain.contacts.EmailAddress;
import org.fenixedu.academic.domain.organizationalStructure.Party;
import org.fenixedu.academic.domain.person.Gender;
import org.fenixedu.academic.domain.person.IDDocumentType;
import org.fenixedu.academic.domain.student.curriculum.ConclusionProcess;
import org.fenixedu.academic.domain.thesis.Thesis;
import org.fenixedu.academic.domain.thesis.ThesisEvaluationParticipant;
import org.fenixedu.academic.domain.thesis.ThesisParticipationType;
import org.fenixedu.bennu.RenatesIntegrationConfiguration;
import org.fenixedu.commons.spreadsheet.Spreadsheet;
import org.fenixedu.commons.spreadsheet.Spreadsheet.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.renates.domain.InstitutionCodeProvider;
import pt.ist.renates.domain.beans.OrientadorBean;

public class ThesisRenatesReportFile extends QueueJobWithFile {

    private static final String DEFAULT_PARNTERSHIP_COURSE = "Não";

    private static final String DEFAULT_SPECIALIZATION = "Sem especialização";

    private static final String DEFAULT_SPECIALIZATION_CODE = "1000017";

    private static final String REGISTO_INTERNO_ID = "RegistoInternoID";

    private static final String HANDLE_DEPOSITO_RCAAP = "HandleDepositoRCAAP";

    private static final String CLASSIFICATION = "ClassificacaoID";

    private static final String PT_GRADUATION_DATE = "DataGrauEmPortugal";

    private static final String OBSERVATIONS = "Observacoes";

    private static final String ESTABLISHMENT_8 = "Estabelecimento8";

    private static final String ESTABLISHMENT_7 = "Estabelecimento7";

    private static final String ESTABLISHMENT_6 = "Estabelecimento6";

    private static final String ESTABLISHMENT_5 = "Estabelecimento5";

    private static final String ESTABLISHMENT_4 = "Estabelecimento4";

    private static final String ESTABLISHMENT_3 = "Estabelecimento3";

    private static final String ESTABLISHMENT_2 = "Estabelecimento2";

    private static final String ESTABLISHMENT_1 = "Estabelecimento1";

    private static final String PARTNERSHIP_COURSE = "Curso_Parceria";

    private static final String KEY_WORDS = "Palavras_chave";

    private static final String CO_ADVISOR = "Co_orientador";

    private static final String ADVISOR_X_ORCID = "ORCID_Orientador";

    private static final String ADVISOR_X_OTHER_ID_TYPE = "OutroTipoIdentificacao_Orientador";

    private static final String ADVISOR_X_ID_TYPE = "TipoIdentificacao_Orientador";

    private static final String ADVISOR_X_ID_NUMBER = "NumIdentificacao_Orientador";

    private static final String ADVISOR_X_NAME = "NomeOrientador";

    private static final String TITLE = "TituloTrabalho";

    private static final String ESPECIALIZACAO_CODE = "CodEspecializacao";

    private static final String SPECIALIZATION = "Especializacao";

    private static final String COURSE = "Curso";

    private static final String COURSE_CODE = "CodCurso";

    private static final String ORGANIC_UNIT_CODE = "codUnidadeOrganica";

    private static final String PT_ESTABLISHMENT_CODE = "CodEstabelecimentoPortugues";

    private static final String OTHER_EMAIL = "OutroEmail";

    private static final String EMAIL = "Email";

    private static final String PHONE = "Telefone";

    private static final String ADRESS = "Morada";

    private static final String OTHER_NATIONALITY = "OutraNacionalidade";

    private static final String NATIONALITY = "PaisNacionalidade";

    private static final String OTHER_ID_TYPE = "OutroTipoIdentificacao";

    private static final String ID_TYPE = "TipoIdentificacao";

    private static final String ID_NUMBER = "NumIdentificacao";

    private static final String BIRTH_DATE = "DataNascimento";

    private static final String GENDER = "Sexo";

    private static final String NAME = "NomeCompleto";

    private static final Logger logger = LoggerFactory.getLogger(ThesisRenatesReportFile.class);

    private final static Locale PT = new Locale("pt", "PT");

    protected ThesisRenatesReportFile() {
        super();
    }

    @Override
    public QueueJobResult execute() throws Exception {
        logger.debug("running {}", this.getExternalId());
        Spreadsheet spreasheet = retrieveIndividualThesisData();
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();

        spreasheet.exportToXLSSheet(byteArrayOS);
        byteArrayOS.close();

        final QueueJobResult queueJobResult = new QueueJobResult();
        queueJobResult.setContentType("application/excel");
        queueJobResult.setContent(byteArrayOS.toByteArray());

        return queueJobResult;
    }

    @Override
    public String getFilename() {
        return String.format("%s_%s.xls", "thesis_information", getRequestDate().toString("dd_MM_yyyy_hh_mm_ss"));
    }

    public static ThesisRenatesReportFile buildRenatesReport() {
        return new ThesisRenatesReportFile();
    }

    private Spreadsheet retrieveIndividualThesisData() {
        Spreadsheet spreadsheet = new Spreadsheet("Renates-Thesis");

        Map<String, OrientadorBean> orientatorsInfo = RenatesUtil.getOrientatorsInfo();

        for (Map.Entry<Thesis, ConclusionProcess> thesisEntry : RenatesUtil.getRenatesThesisAndConclusionProcess().entrySet()) {

            Thesis thesis = thesisEntry.getKey();
            ConclusionProcess conclusionProcess = thesisEntry.getValue();
            Enrolment thesisEnrolment = thesis.getEnrolment();

            if (thesis.getThesisId() != null && thesis.getThesisId().getId() != null) {
                continue;
            }

            final Row row = spreadsheet.addRow();

            final Person person = thesis.getStudent().getPerson();

            row.setCell(NAME, person.getName());
            row.setCell(GENDER, person.getGender() == Gender.MALE ? "H" : "F");
            row.setCell(BIRTH_DATE, person.getDateOfBirthYearMonthDay().toString("dd-MM-yyyy"));
            row.setCell(ID_NUMBER, person.getDocumentIdNumber());

            final String studentIdType = idDocumentTypeToNumber(person.getIdDocumentType());

            row.setCell(ID_TYPE, studentIdType);
            row.setCell(OTHER_ID_TYPE, studentIdType.equals("9") ? person.getIdDocumentType().getLocalizedName(PT) : "");
            row.setCell(NATIONALITY, person.getCountryOfBirth().getCode());

            row.setCell(OTHER_NATIONALITY, "");
            row.setCell(ADRESS, "");
            row.setCell(PHONE, "");

            final EmailAddress email = person.getEmailAddressForSendingEmails();
            final String email_string = email == null ? "ist1" + thesis.getStudent().getNumber().toString()
                    + "@tecnico.ulisboa.pt" : email.getPresentationValue();

            row.setCell(EMAIL, email_string);

            row.setCell(OTHER_EMAIL, "");

            InstitutionCodeProvider institutionCodeProvider = RenatesIntegrationConfiguration.getInstitutionCodeProvider();
            row.setCell(PT_ESTABLISHMENT_CODE, institutionCodeProvider.getEstablishmentCode());
            final DegreeCurricularPlan degreeCurricularPlan = thesisEnrolment.getDegreeCurricularPlanOfStudent();

            row.setCell(ORGANIC_UNIT_CODE, institutionCodeProvider.getOrganicUnitCode(degreeCurricularPlan));
            final Degree degree = degreeCurricularPlan.getDegree();

            row.setCell(COURSE_CODE, degree.getMinistryCode());
            row.setCell(COURSE, degree.getPresentationName());

            row.setCell(ESPECIALIZACAO_CODE, DEFAULT_SPECIALIZATION_CODE);
            row.setCell(SPECIALIZATION, DEFAULT_SPECIALIZATION);
            row.setCell(TITLE, thesis.getTitle().getContent(PT));

            //Writing on advisor columns            
            Iterator<Person> person_iterator = getAdvisors(thesis).iterator();
            for (int i = 1; i <= 5; i++) {
                if (person_iterator.hasNext()) {
                    final Person advisor = person_iterator.next();
                    row.setCell(ADVISOR_X_NAME + i, advisor.getName());
                    row.setCell(ADVISOR_X_ID_NUMBER + i, advisor.getDocumentIdNumber());

                    final String advisor1IdType = idDocumentTypeToNumber(advisor.getIdDocumentType());
                    row.setCell(ADVISOR_X_ID_TYPE + i, advisor1IdType);
                    row.setCell(ADVISOR_X_OTHER_ID_TYPE + i,
                            advisor1IdType.equals("9") ? advisor.getIdDocumentType().getLocalizedName(PT) : "");

                    final String advisor_istId = advisor.getUser().getUsername();
                    if (orientatorsInfo != null && orientatorsInfo.containsKey(advisor_istId)) {
                        row.setCell(ADVISOR_X_ORCID + i, orientatorsInfo.get(advisor_istId).getOrcid());
                    } else {
                        row.setCell(ADVISOR_X_ORCID + i, "");

                    }
                }

                else {
                    row.setCell(ADVISOR_X_NAME + i, "");
                    row.setCell(ADVISOR_X_ID_NUMBER + i, "");
                    row.setCell(ADVISOR_X_ID_TYPE + i, "");
                    row.setCell(ADVISOR_X_OTHER_ID_TYPE + i, "");
                    row.setCell(ADVISOR_X_ORCID + i, "");
                }

            }

            row.setCell(CO_ADVISOR, "");

            row.setCell(KEY_WORDS, thesis.getKeywordsPt());
            row.setCell(PARTNERSHIP_COURSE, DEFAULT_PARNTERSHIP_COURSE);

            row.setCell(ESTABLISHMENT_1, "");
            row.setCell(ESTABLISHMENT_2, "");
            row.setCell(ESTABLISHMENT_3, "");
            row.setCell(ESTABLISHMENT_4, "");
            row.setCell(ESTABLISHMENT_5, "");
            row.setCell(ESTABLISHMENT_6, "");
            row.setCell(ESTABLISHMENT_7, "");
            row.setCell(ESTABLISHMENT_8, "");
            row.setCell(OBSERVATIONS, "");

            row.setCell(PT_GRADUATION_DATE, conclusionProcess.getConclusionDate().toString("dd-MM-yyyy"));
            row.setCell(CLASSIFICATION, conclusionProcess.getFinalGrade().getValue());

            row.setCell(HANDLE_DEPOSITO_RCAAP, "");
            row.setCell(REGISTO_INTERNO_ID, RenatesUtil.getThesisId(thesis));

        }

        return spreadsheet;

    }

    private String idDocumentTypeToNumber(IDDocumentType idDocumentType) {

        switch (idDocumentType) {
        case IDENTITY_CARD:
            return "1";

        case CITIZEN_CARD:
            return "1";

        case PASSPORT:
            return "2";

        case RESIDENCE_AUTHORIZATION:
            return "3";

        case FOREIGNER_IDENTITY_CARD:
            return "4";

        case OTHER:
            return "9";

        case NATIVE_COUNTRY_IDENTITY_CARD:
            return "9";

        case NAVY_IDENTITY_CARD:
            return "9";

        case AIR_FORCE_IDENTITY_CARD:
            return "9";

        case MILITARY_IDENTITY_CARD:
            return "9";

        case EXTERNAL:
            return "9";

        default:
            return "9";
        }
    }

    private Set<Person> getAdvisors(Thesis thesis) {
        Set<Person> advisors = new TreeSet<Person>(Party.COMPARATOR_BY_NAME_AND_ID);
        for (ThesisEvaluationParticipant thesisEvaluationParticipant : thesis
                .getAllParticipants(ThesisParticipationType.ORIENTATOR)) {
            Person advisor = thesisEvaluationParticipant.getPerson();
            if (advisor != null) {
                advisors.add(advisor);
            }
        }

        return advisors;
    }

}
