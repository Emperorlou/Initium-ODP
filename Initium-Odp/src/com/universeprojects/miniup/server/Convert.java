/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.universeprojects.miniup.server;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author Remote
 */
public class Convert 
{

    public static Date CalendarToDate(Calendar value)
    {
        return value.getTime();
    }

    public static String IntToStr(Long value)
    {
        return value.toString();
    }
    
    public static String IntToStr(Integer value)
    {
        return value.toString();
    }

    public static Boolean StrToBoolean(String parameter)
    {
        if (parameter==null)
            return Boolean.FALSE;
        else if (parameter.trim().matches("(?i)(1|-1|true|t|tru|positive|p|yes|y|on|check|checked)"))
            return Boolean.TRUE;
        else
            return Boolean.FALSE;
    }
    
    public static Integer StrToInteger(String value)
    {
        Integer result = null;
        try
        {
            result = new Integer(value);
        }
        catch(Exception e)
        {
            return null;
        }
        return result;
    }
    
    public static Long StrToLong(String value)
    {
    	Long result = null;
        try
        {
            result = new Long(value);
        }
        catch(Exception e)
        {
            return null;
        }
        return result;
    }
    
    public static long CalendarToSeconds(GregorianCalendar cal)
    {
        long result = cal.get(GregorianCalendar.SECOND);
        result += cal.get(GregorianCalendar.MINUTE)*60;
        result += cal.get(GregorianCalendar.HOUR_OF_DAY)*3600; // 60*60
        result += cal.get(GregorianCalendar.DAY_OF_YEAR)*86400; // 60*60*24
        result += cal.get(GregorianCalendar.YEAR)*31536000; // 60*60*24*365
        return result;
    }

    public static GregorianCalendar DateToCalendar(Date date)
    {
        if (date==null) return null;
        
        GregorianCalendar result = new GregorianCalendar();
        result.setTime(date);
        return result;
    }

    public static String SecondsToStandardString(long seconds)
    {
        if (seconds<0)
            seconds=seconds*-1;
        if (seconds==0)
            return "0";
        
        double dSeconds = seconds;
        double dDays = Math.floor(dSeconds/86400);
        dSeconds = dSeconds-(dDays*86400);
        double dHours = Math.floor(dSeconds/3600);
        dSeconds = dSeconds-(dHours*3600);
        double dMinutes = Math.floor(dSeconds/60);
        dSeconds = dSeconds-(dMinutes*60);
        int days = (int) dDays;
        int hours = (int) dHours;
        int minutes = (int) dMinutes;
        seconds = (long) dSeconds;
        
        if (dDays>0)
            return days+" days "+hours+":"+minutes+":"+seconds;
        if (dHours>0)
            return hours+":"+minutes+":"+seconds;
        if (dMinutes>0)
            return minutes+":"+seconds;
        if (dSeconds>0)
            return ""+seconds;
            
        return "";
    }

    public static String SecondsToStandardString2(long secondsToConvert)
    {
        if (secondsToConvert<0)
            secondsToConvert=secondsToConvert*-1;
        if (secondsToConvert==0)
            return "0";

        Double dSeconds = new Double(secondsToConvert);
        Double dDays = Math.floor(dSeconds/86400);
        dSeconds = dSeconds-(dDays*86400);
        Double dHours = Math.floor(dSeconds/3600);
        dSeconds = dSeconds-(dHours*3600);
        Double dMinutes = Math.floor(dSeconds/60);
        dSeconds = dSeconds-(dMinutes*60);
        String days = "";
        if (dDays.intValue()==1)
            days = dDays.intValue()+" day";
        else if (dDays.intValue()>1)
            days = dDays.intValue()+" days";
        String hours = "";
        if (dHours.intValue()==1)
            hours = dHours.intValue()+" hour";
        else if (dHours.intValue()>1)
            hours = dHours.intValue()+" hours";
        String minutes = "";
        if (dMinutes.intValue()==1)
            minutes = dMinutes.intValue()+" minute";
        else if (dMinutes.intValue()>1)
            minutes = dMinutes.intValue()+" minutes";
        String seconds = "";
        if (dSeconds.intValue()==1)
            seconds = dSeconds.intValue()+" second";
        else if (dSeconds.intValue()>1)
            seconds = dSeconds.intValue()+" seconds";

        return days+" "+hours+" "+minutes+" "+seconds;

    }
            
    public static String SecondsToStandardShortString(long secondsToConvert)
    {
        if (secondsToConvert<0)
            secondsToConvert=secondsToConvert*-1;
        if (secondsToConvert==0)
            return "0";

        Double dSeconds = new Double(secondsToConvert);
        Double dDays = Math.floor(dSeconds/86400);
        dSeconds = dSeconds-(dDays*86400);
        Double dHours = Math.floor(dSeconds/3600);
        dSeconds = dSeconds-(dHours*3600);
        Double dMinutes = Math.floor(dSeconds/60);
        dSeconds = dSeconds-(dMinutes*60);
  
        if (dDays.intValue()==1)
            return dDays.intValue()+" day";
        else if (dDays.intValue()>1)
            return dDays.intValue()+" days";

        if (dHours.intValue()==1)
            return dHours.intValue()+" hour";
        else if (dHours.intValue()>1)
            return dHours.intValue()+" hours";

        if (dMinutes.intValue()==1)
            return dMinutes.intValue()+" minute";
        else if (dMinutes.intValue()>1)
            return dMinutes.intValue()+" minutes";

        if (dSeconds.intValue()==1)
            return dSeconds.intValue()+" second";
        else if (dSeconds.intValue()>1)
            return dSeconds.intValue()+" seconds";

        return "less than 1 second";
    }

	public static String CalToStr(Calendar date, String pattern)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);

		return sdf.format(date.getTime());
	}

    public static Double StrToDouble(String value)
    { 
        Double result = null;
        try
        {
            result = new Double(value);
        }
        catch(Exception e)
        {
            return null;
        }
        return result;
    }

	public static Integer LongToInteger(Long value) 
	{
		if (value==null) return null;
		return value.intValue();
	}
}
