package com.example.metau_capstone;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 ** This class is used to easily change the date from a Date object
 ** to a formatted string
 */
public class dateFormatter {

    // Dictionary to convert an integer representation of a Month to
    // the string representation of a month
    Map<Integer, String> intToMonth = new HashMap<Integer, String>() {{
            put(0, "January");
            put(1, "February");
            put(2, "March");
            put(3, "April");
            put(4, "May");
            put(5, "June");
            put(6, "July");
            put(7, "August");
            put(8, "September");
            put(9, "October");
            put(10, "November");
            put(11, "December");
    }};


    // Convert a date to the format: [Month] [day]
    public String toMonthDay(Date date) {
        String month = intToMonth.get(date.getMonth());
        String day = String.valueOf(date.getDate());
        return month + " " + day;
    }

    // Convert a date to the format: [Month] [day] - [hour]:[minutes] [AM/PM]
    public String toMonthDayTime(Date date) {
        String month = intToMonth.get(date.getMonth());
        String day = String.valueOf(date.getDate());
        int hours = date.getHours();
        int mins = date.getMinutes();
        String AMPM;
        if (hours > 12) {
            AMPM = "PM";
        }
        else {
            AMPM = "AM";
        }
        if (hours > 13) {
            hours = hours - 12;
        }
        String h = String.valueOf(hours);
        if (h.length() == 1) {
            h = "0" + h;
        }
        String m = String.valueOf(mins);
        if (m.length() == 1) {
            m = "0" + m;
        }
        return month + " " + day + " - " + h + ":" + m + " " + AMPM;
    }
}
