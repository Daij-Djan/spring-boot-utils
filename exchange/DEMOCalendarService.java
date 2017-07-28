
/**
 @file      CustomerService.java
 @author    Dominik Pich
 @date      28/11/2016

Copyright (c) 2016, Sapient GmbH
All rights reserved.

*/
package com.sapient.quickbook.services.ews.calendar;

import com.sapient.quickbook.services.ews.ExchangeProvider;
import com.sapient.quickbook.services.ews.NeedsExchangeService;

@Service
@Slf4j
class CalendarServiceHelper {
    @NeedsExchangeService
    public CalendarInfo readCalendarFromService(String calendarEmail, Date fromDate, Date toDate, ExchangeService exchangeService) throws Exception {
            List<CalendarEntry> calendarEntries = new ArrayList<CalendarEntry>();
            Mailbox userMailbox = new Mailbox(calendarEmail);
            FolderId folderId = new FolderId(WellKnownFolderName.Calendar, userMailbox);
            CalendarFolder calendarFolder = CalendarFolder.bind(exchangeService, folderId);
            CalendarView cView = new CalendarView(fromDate, toDate, Integer.MAX_VALUE);
            cView.setPropertySet(new PropertySet(AppointmentSchema.Subject, AppointmentSchema.Start, AppointmentSchema.End));// we can set other properties as well depending upon our need.
            FindItemsResults appointments = calendarFolder.findAppointments(cView);

            List<Appointment> appList = appointments.getItems();
            for (Appointment appointment : appList) {
                appointment.load();
                CalendarEntry newEntry = CalendarEntry.from(appointment);
                calendarEntries.add(newEntry);
            }
            return new CalendarInfo(calendarEmail, fromDate, toDate, calendarEntries);
        }
    }
}
