/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST Integration.
 *
 * FenixEdu IST Integration is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST Integration is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST Integration.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.renates.domain.tasks;

import org.fenixedu.academic.domain.thesis.Thesis;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.tempuri.ws.renates.RenatesWS;
import org.tempuri.ws.renates.RenatesWSSoap;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.renates.domain.thesis.RenatesUtil;
import pt.ist.renates.domain.thesis.ThesisId;
import pt.ist.renates.domain.thesis.exceptions.InternalThesisIdException;

@Task(englishTitle = "GetThesisTids", readOnly = true)
public class GetTidTask extends CronTask {

    @Override
    public void runTask() {
        RenatesWS ss = new RenatesWS(RenatesWS.WSDL_LOCATION);
        RenatesWSSoap port = ss.getRenatesWSSoap12();

        for (Thesis thesis : RenatesUtil.getRenatesThesis()) {
            if (thesis.getThesisId() == null || thesis.getThesisId().getId() == null) {
                String internalId;
                try {
                    internalId = RenatesUtil.getThesisId(thesis);
                } catch (InternalThesisIdException e) {
                    continue;
                }
                String tid = port.tid(internalId);
                if (tid != null) {
                    FenixFramework.atomic(() -> {
                        thesis.setThesisId(new ThesisId(tid));
                    });
                }
            }
        }
    }

}
