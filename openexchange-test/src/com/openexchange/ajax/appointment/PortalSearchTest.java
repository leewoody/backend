
package com.openexchange.ajax.appointment;

import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.NewAppointmentSearchRequest;
import com.openexchange.ajax.appointment.action.NewAppointmentSearchResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;

public class PortalSearchTest extends AppointmentTest {

    private final int[] columns = { DataObject.OBJECT_ID, FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, Appointment.LOCATION, CalendarObject.START_DATE, CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE, Appointment.SHOWN_AS, Appointment.FULL_TIME, Appointment.COLOR_LABEL
    };

    @Test
    public void testNewAppointmentsSearch() throws Exception {
        final Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, -2);

        final Date start = calendar.getTime();

        calendar.add(Calendar.YEAR, 1);

        final Date end = calendar.getTime();

        final Appointment appointmentObj = createAppointmentObject("testNewAppointmentsSearch");
        appointmentObj.setIgnoreConflicts(true);

        final int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);

        final NewAppointmentSearchRequest request = new NewAppointmentSearchRequest(start, end, 10000, timeZone, columns);
        final NewAppointmentSearchResponse response = Executor.execute(getClient(), request);

        if (response.hasError()) {
            throw new Exception("json error: " + response.getResponse().getErrorMessage());
        }

        final Appointment[] appointmentArray = response.getAppointments();

        boolean found = false;
        for (int a = 0; a < appointmentArray.length; a++) {
            if (appointmentArray[a].getObjectID() == objectId) {
                compareObject(appointmentObj, appointmentArray[a]);
                found = true;
            }
        }

        assertTrue("object with id " + objectId + " not found in response", found);
    }
}
