/**
 * Copyright © 2018 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Academic.
 *
 * FenixEdu Academic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Academic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.renates.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.QueueJob;
import org.fenixedu.bennu.core.domain.Bennu;
import org.springframework.stereotype.Service;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.renates.domain.thesis.ThesisRenatesReportFile;

@Service
public class RenatesReportService {

    public List<QueueJob> getDoneThesisRenatesReportFiles() {
        return Bennu.getInstance().getQueueJobSet().stream().filter((ThesisRenatesReportFile.class)::isInstance)
                .filter(r -> r.getDone()).sorted(QueueJob.COMPARATORY_BY_REQUEST_DATE).limit(10).collect(Collectors.toList());
    }

    public List<QueueJob> getUndoneThesisRenatesReportFiles() {
        return Bennu.getInstance().getQueueJobSet().stream().filter((ThesisRenatesReportFile.class)::isInstance)
                .filter(r -> !r.getDone()).sorted(QueueJob.COMPARATORY_BY_REQUEST_DATE).collect(Collectors.toList());
    }

    @Atomic(mode = TxMode.WRITE)
    public void createThesisRenatesReportFile() {
        ThesisRenatesReportFile.buildRenatesReport();
    }

}
