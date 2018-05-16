package pt.ist.renates.domain.beans;

public class OrientadorBean {

    private String istId = "";

    private String researcherId = "";

    private String scopusId = "";

    private String orcid = "";

    private String fos = "";

    public OrientadorBean() {
    }

    public String getIstId() {
        return istId;
    }

    public void setIstId(String istId) {
        this.istId = istId;
    }

    public String getResearcherId() {
        return researcherId;
    }

    public void setResearcherId(String researcherId) {
        this.researcherId = researcherId;
    }

    public String getScopusId() {
        return scopusId;
    }

    public void setScopusId(String scopusId) {
        this.scopusId = scopusId;
    }

    public String getOrcid() {
        return orcid;
    }

    public void setOrcid(String orcid) {
        this.orcid = orcid;
    }

    public String getFos() {
        return fos;
    }

    public void setFos(String fos) {
        this.fos = fos;
    }

}
