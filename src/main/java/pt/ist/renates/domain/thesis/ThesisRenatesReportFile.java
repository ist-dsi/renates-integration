package pt.ist.renates.domain.thesis;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.QueueJobResult;
import org.fenixedu.academic.domain.QueueJobWithFile;
import org.fenixedu.academic.domain.contacts.EmailAddress;
import org.fenixedu.academic.domain.contacts.PartyContact;
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
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import pt.ist.renates.domain.InstitutionCodeProvider;
import pt.ist.renates.domain.beans.OrientadorBean;
import pt.ist.renates.domain.thesis.exceptions.InternalThesisIdException;

public class ThesisRenatesReportFile extends QueueJobWithFile {

    private static final String DEFAULT_SPREADSHEET_NAME = "Renates-Thesis-%s-%s";

    private static final String ERROR_DESCRIPTION = "Descrição do erro";

    private static final String ERROR_STUDENT_ID = "Student ID";

    private static final String ERROR_SPEADSHEET_NAME = "Erros";

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

    private final static Locale EN = new Locale("en", "GB");

    protected ThesisRenatesReportFile() {
        super();
    }

    @Override
    public QueueJobResult execute() throws Exception {
        logger.debug("running {}", this.getExternalId());

        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(byteArrayOS);
        for (Map.Entry<String, Spreadsheet> spreadsheet_entry : retrieveIndividualThesisData().entrySet()) {
            String spreadsheet_name = spreadsheet_entry.getKey() + ".xls";
            Spreadsheet spreadsheet = spreadsheet_entry.getValue();

            zip.putNextEntry(new ZipEntry(spreadsheet_name));
            ByteArrayOutputStream oStream = new ByteArrayOutputStream();
            spreadsheet.exportToXLSSheet(oStream);

            zip.write(oStream.toByteArray());
            zip.closeEntry();
        }
        zip.close();

        final QueueJobResult queueJobResult = new QueueJobResult();
        queueJobResult.setContentType("application/zip");
        queueJobResult.setContent(byteArrayOS.toByteArray());

        return queueJobResult;
    }

    @Override
    public String getFilename() {
        return String.format("%s_%s.zip", "thesis_information", getRequestDate().toString("dd_MM_yyyy_hh_mm_ss"));
    }

    public static ThesisRenatesReportFile buildRenatesReport() {
        return new ThesisRenatesReportFile();
    }

    private Map<String, Spreadsheet> retrieveIndividualThesisData() {
        Map<String, Spreadsheet> spreadsheets_map = new HashMap<>();
        Map<String, Integer> spreadsheets_counter_map = new HashMap<>();
        Map<String, Spreadsheet> names_and_spreadsheets = new HashMap<>();
        final int entries_limit = RenatesIntegrationConfiguration.getConfiguration().getEntriesLimit();
        final LocalDate entries_startDate =
                LocalDate.parse(RenatesIntegrationConfiguration.getConfiguration().getEntriesStartDate(),
                        DateTimeFormat.forPattern("dd/MM/yyyy"));

        Spreadsheet error_spreadsheet = new Spreadsheet(ERROR_SPEADSHEET_NAME);

        names_and_spreadsheets.put(ERROR_SPEADSHEET_NAME, error_spreadsheet);

        Map<String, OrientadorBean> orientatorsInfo = RenatesUtil.getOrientatorsInfo();

        for (SortedMap.Entry<ConclusionProcess, Thesis> thesisEntry : RenatesUtil.getRenatesThesisAndConclusionProcess().entrySet()) {

            Thesis thesis = thesisEntry.getValue();
            ConclusionProcess conclusionProcess = thesisEntry.getKey();
            Enrolment thesisEnrolment = thesis.getEnrolment();

            if (thesis.getThesisId() != null && thesis.getThesisId().getId() != null) {
                continue;
            }

            LocalDate conclusionDate = conclusionProcess.getConclusionDate();

            if (entries_startDate.isAfter(conclusionDate)) {
                continue;
            }

            final Person person = thesis.getStudent().getPerson();

            String errors_description = check_person_data(person, thesis);

            if (!Strings.isNullOrEmpty(errors_description)) {
                Row errors_row = error_spreadsheet.addRow();
                errors_row.setCell(ERROR_STUDENT_ID, person.getPresentationName());
                errors_row.setCell(ERROR_DESCRIPTION, errors_description);
                continue;
            }

            InstitutionCodeProvider institutionCodeProvider = RenatesIntegrationConfiguration.getInstitutionCodeProvider();
            final DegreeCurricularPlan degreeCurricularPlan = thesisEnrolment.getDegreeCurricularPlanOfStudent();
            final Degree degree = degreeCurricularPlan.getDegree();

            String organicUnitName = degreeCurricularPlan.getLastCampus().getName();

            Spreadsheet spreadsheet = null;

            if (!spreadsheets_map.containsKey(organicUnitName)) {
                String spreadsheet_name = String.format(DEFAULT_SPREADSHEET_NAME, organicUnitName, 0);
                spreadsheet = new Spreadsheet(spreadsheet_name);
                names_and_spreadsheets.put(spreadsheet_name, spreadsheet);
                spreadsheets_map.put(organicUnitName, spreadsheet);
                spreadsheets_counter_map.put(organicUnitName, 1);
            } else {
                spreadsheet = spreadsheets_map.get(organicUnitName);

                if (spreadsheet.getRows().size() >= entries_limit - 1) {
                    int counter = spreadsheets_counter_map.get(organicUnitName);
                    String spreadsheet_name = String.format(DEFAULT_SPREADSHEET_NAME, organicUnitName, counter);
                    spreadsheet = new Spreadsheet(spreadsheet_name);
                    names_and_spreadsheets.put(spreadsheet_name, spreadsheet);
                    spreadsheets_map.put(organicUnitName, spreadsheet);
                    spreadsheets_counter_map.put(organicUnitName, counter + 1);
                }
            }


            final Row row = spreadsheet.addRow();

            row.setCell(NAME, person.getName());
            row.setCell(GENDER, person.getGender() == Gender.MALE ? "H" : "M");
            row.setCell(BIRTH_DATE, person.getDateOfBirthYearMonthDay().toString("dd-MM-yyyy"));
            row.setCell(ID_NUMBER, person.getDocumentIdNumber());

            final String studentIdType = idDocumentTypeToNumber(person.getIdDocumentType());

            row.setCell(ID_TYPE, studentIdType);
            row.setCell(OTHER_ID_TYPE, studentIdType.equals("9") ? person.getIdDocumentType().getLocalizedName(PT) : "");

            row.setCell(NATIONALITY, person.getCountryOfBirth().getCode());
            row.setCell(OTHER_NATIONALITY, "");
            row.setCell(ADRESS, "");
            row.setCell(PHONE, "");

            row.setCell(EMAIL, getPersonEmail(person).getPresentationValue());

            row.setCell(OTHER_EMAIL, "");

            row.setCell(PT_ESTABLISHMENT_CODE, institutionCodeProvider.getEstablishmentCode());

            row.setCell(ORGANIC_UNIT_CODE, institutionCodeProvider.getOrganicUnitCode(degreeCurricularPlan));

            row.setCell(COURSE_CODE, degree.getMinistryCode());
            row.setCell(COURSE, degree.getPresentationName());

            row.setCell(ESPECIALIZACAO_CODE, DEFAULT_SPECIALIZATION_CODE);
            row.setCell(SPECIALIZATION, DEFAULT_SPECIALIZATION);
            row.setCell(TITLE, getThesisTitle(thesis));

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
                    String orcid =
                            orientatorsInfo.containsKey(advisor_istId) ? orientatorsInfo.get(advisor_istId).getOrcid() : "";
                    row.setCell(ADVISOR_X_ORCID + i, orcid);
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

            row.setCell(PT_GRADUATION_DATE, conclusionDate.toString("dd-MM-yyyy"));
            row.setCell(CLASSIFICATION, conclusionProcess.getFinalGrade().getValue());

            row.setCell(HANDLE_DEPOSITO_RCAAP, "");

            try {
                row.setCell(REGISTO_INTERNO_ID, RenatesUtil.getThesisId(thesis));
            } catch (InternalThesisIdException internalThesisIdException) {
                row.setCell(REGISTO_INTERNO_ID, "");
            }

        }

        return names_and_spreadsheets;

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

    private String check_person_data(Person person, Thesis thesis) {
        List<String> errors = new ArrayList<>();

        if (person.getName() == null) {
            errors.add("Não tem nome definido.");
        }

        if (person.getGender() == null) {
            errors.add("Não tem género definido.");
        }

        if (person.getDateOfBirthYearMonthDay() == null) {
            errors.add(" Não tem data de nascimento definido.");
        }

        if (person.getDocumentIdNumber() == null) {
            errors.add("Não tem tipo de cartão de identificação definido.");
        }

        if (person.getCountryOfBirth() == null) {
            errors.add("Não tem local de nascimento definido.");
        }

        if (getPersonEmail(person) == null) {
            errors.add("Não tem email definido.");
        }

        if (getThesisTitle(thesis).isEmpty()) {
            errors.add("Não tem título definido");
        }

        if (person.getCountryOfBirth() != null && person.getCountryOfBirth().getCode().equals("PT")
                && !idDocumentTypeToNumber(person.getIdDocumentType()).equals("1")) {
            errors.add(
                    "Tem nacionalidade Portuguesa mas o documento de identificação não é o cartão de identificação nem o cartão de cidadão");
        }

        try {
            RenatesUtil.getThesisId(thesis);
        } catch (InternalThesisIdException internalThesisIdException) {
            errors.add(internalThesisIdException.getMessage());
        }

        return Joiner.on("\n").join(errors);
    }

    public EmailAddress getPersonEmail(Person person) {
        final EmailAddress defaultEmailAddress = person.getDefaultEmailAddress();
        if (defaultEmailAddress != null) {
            return defaultEmailAddress;
        }
        final EmailAddress institutionalEmailAddress = person.getInstitutionalEmailAddress();
        if (institutionalEmailAddress != null) {
            return institutionalEmailAddress;
        }
        for (final PartyContact partyContact : person.getPartyContactsSet()) {
            if (partyContact.isEmailAddress() && partyContact.isActiveAndValid() && partyContact.isValid()) {
                final EmailAddress otherEmailAddress = (EmailAddress) partyContact;
                return otherEmailAddress;
            }
        }

        return null;
    }

    public String getThesisTitle(Thesis thesis) {

        String title = thesis.getTitle().getContent(PT);

        if (Strings.isNullOrEmpty(title)) {
            title = thesis.getTitle().getContent(EN);
        }

        if (Strings.isNullOrEmpty(title)) {
            title = thesis.getDissertation().getTitle();
        }

        return Strings.isNullOrEmpty(title) ? "" : title;
    }
}