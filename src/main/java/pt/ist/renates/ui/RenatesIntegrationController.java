package pt.ist.renates.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.academic.domain.QueueJob;
import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pt.ist.renates.domain.thesis.ThesisRenatesReportFile;

@RequestMapping("/renates-integration")
@SpringApplication(group = "logged", path = "renates-integration", title = "title.RenatesIntegration")
@SpringFunctionality(app = RenatesIntegrationController.class, title = "title.RenatesIntegration")
public class RenatesIntegrationController {

    @Autowired
    private RenatesReportService renatesreportservice;

    @RequestMapping(method = RequestMethod.GET)
    public String list(Model model) {
        return getList(model, new ArrayList<String>());
    }

    @RequestMapping(value = "/requestRenatesReport", method = RequestMethod.GET)
    public String requestRenatesReport(Model model) {
        if (renatesreportservice.getUndoneThesisRenatesReportFiles().size() < 10) {
            renatesreportservice.createThesisRenatesReportFile();
        }
        return "redirect:/renates-integration";
    }

    @RequestMapping(value = "/download/renates-report/{job}", method = RequestMethod.GET)
    public String downloadExcel(@PathVariable ThesisRenatesReportFile job, Model model, HttpServletResponse httpServletResponse) {

        try {
            if (job.getFile() != null) {
                httpServletResponse.setContentType(job.getContentType());
                httpServletResponse.setHeader("Content-disposition", "attachment;filename=" + job.getFilename());
                final OutputStream outputStream = httpServletResponse.getOutputStream();
                outputStream.write(job.getFile().getContent());
                outputStream.close();
            } else {
                List<String> errors = new ArrayList<String>();
                errors.add("label.renates.exceptions.file.not.found");
                return getList(model, errors);
            }
        } catch (IOException e) {
            List<String> errors = new ArrayList<String>();
            errors.add("label.renates.exceptions.ioexception");
            return getList(model, errors);
        }

        return null;

    }

    public String getList(Model model, List<String> errors) {
        model.addAttribute("doneQueuejobs", renatesreportservice.getDoneThesisRenatesReportFiles());
        final List<QueueJob> undoneJobs = renatesreportservice.getUndoneThesisRenatesReportFiles();
        model.addAttribute("undoneQueuejobs", undoneJobs);
        if (undoneJobs.size() >= 10) {
            errors.add("label.renates.exceptions.max.requests.reached");
        }
        model.addAttribute("errors", errors);
        return "renates-integration/list";
    }

}
