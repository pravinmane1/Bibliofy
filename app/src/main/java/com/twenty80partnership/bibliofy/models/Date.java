package com.twenty80partnership.bibliofy.models;

import android.util.Log;

import androidx.core.content.ContextCompat;

import com.twenty80partnership.bibliofy.R;

import java.util.ArrayList;

public class Date {
    ArrayList<Integer> monthsNumber;
    private ArrayList<String>months;
    Long longDate;
    String ampm;
    int day, month, year,hours,minutes, newDay,newMonth,newYear;
    String sDay,sMinutes,sHours,sDate;


    public Date(){
        months = new ArrayList<>();

        months.add("Jan");
        months.add("Feb");
        months.add("Mar");
        months.add("April");
        months.add("May");
        months.add("June");
        months.add("July");
        months.add("Aug");
        months.add("Sept");
        months.add("Oct");
        months.add("Nov");
        months.add("Dec");

        monthsNumber = new ArrayList<>();

        monthsNumber.add(31);
        monthsNumber.add(29);
        monthsNumber.add(31);
        monthsNumber.add(30);
        monthsNumber.add(31);
        monthsNumber.add(30);
        monthsNumber.add(31);
        monthsNumber.add(31);
        monthsNumber.add(30);
        monthsNumber.add(31);
        monthsNumber.add(30);
        monthsNumber.add(31);

    }

    private Long createAbsoluteDate(Long year, Long month, Long day) {
        year = year*10000;
        month = month*100;

        return year+month+day;
    }

    public String incrementDate(Long userTimeAdded,int incr){
        userTimeAdded = userTimeAdded/1000000000L;
        day = (int)(userTimeAdded  % 100);
        userTimeAdded = userTimeAdded/100;

        month = (int)(userTimeAdded%100);

        year = (int)(userTimeAdded/100);

        if ( (day + incr) > monthsNumber.get(month-1)){

            if (month==12){
                newYear = year+1;
                newMonth = 1;

                newDay = day - (31 - incr);
            }
            else {
                newYear = year;
                newMonth = month+1;
                newDay = incr -  (monthsNumber.get(month-1) - day) ;
            }
        }
        else{
            newDay = day + incr;
            newMonth = month;
            newYear = year;
        }

         return newDay+"-"+newMonth+"-"+newYear;
    }

    public String convertLongDateIntoSplit(Long userTimeAdded) {

        userTimeAdded = userTimeAdded/100000L;

        minutes = (int)(userTimeAdded%100);
        userTimeAdded = userTimeAdded/100;

        hours =  (int) (userTimeAdded%100);
        userTimeAdded = userTimeAdded/100;

        ampm = "AM";
        if (hours>12L){
            ampm = "PM";
            hours = hours - 12;
        }
        day =  (int)(userTimeAdded  % 100);
        userTimeAdded = userTimeAdded/100;

        month = (int)(userTimeAdded%100);

        year = (int)(userTimeAdded/100);

        String sDay,sMinutes,sHours;

        if (day>=10) {
            sDay = "" + day;
        }
        else{
            sDay = "0" + day;
        }

        if (minutes>=10) {
            sMinutes = "" + minutes;
        }
        else{
            sMinutes = "0" + minutes;
        }

        if(hours==0){
            sHours = "00";
        }
        else{
            sHours = "" + hours;
        }

        sDate = months.get(month-1)+" "+sDay+", "+sHours+":"+sMinutes+" "+ampm;

        return sDate;
    }
}
