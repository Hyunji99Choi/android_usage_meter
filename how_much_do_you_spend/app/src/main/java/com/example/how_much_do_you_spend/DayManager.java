package com.example.how_much_do_you_spend;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

// 싱글톤 클래스, 메니저 클래스(기능만 숙지 -> 원리 분석 못함)
public class DayManager {
    public Date nowDay; // 현재 조회하고 있는 날짜
    public String option; // MONTH, WEEK, DAY
    private static DayManager instance;

    public DayManager()
    {
        nowDay = getToday();
    }

    public static DayManager getInstance()
    {
        if(instance == null)
        {
            instance = new DayManager();
        }
        return instance;
    }

    // GET 오늘 날짜
    public Date getToday()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c1 = Calendar.getInstance();

        return stringToDate(sdf.format(c1.getTime()));
    }

    // 파라미터 날짜 0000년 00월 00일 포맷으로 변경 후 리턴
    public String getFormatDay(Date date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        String nowDate = sdf.format(c.getTime());
        String[] parsingDate = nowDate.split("-");

        return String.format("%s년 %s월 %s일",parsingDate[0],parsingDate[1],parsingDate[2]);
    }

    public String getStartWeek(Date date){
        Calendar c = GregorianCalendar.getInstance();
        c.setTime(date);
        c.set(c.DAY_OF_WEEK, Calendar.SUNDAY); // 날짜를 현재 주의 시작 날짜인 일요일로 이동
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        return df.format(c.getTime());
    }

    public String getEndWeek(Date date){
        Calendar c = GregorianCalendar.getInstance();
        c.setTime(date);
        c.set(c.DAY_OF_WEEK, Calendar.SATURDAY); // 날짜를 현재 주의 시작 날짜인 일요일로 이동
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        return df.format(c.getTime());
    }

    // 파라미터 날짜의 한주 0000년 0월 0일-0000년 0월 0일 포맷으로 반환
    public String getWeek(Date date)
    {
        String week = "";
        Calendar c = GregorianCalendar.getInstance();
        c.setTime(date);
        c.set(c.DAY_OF_WEEK, Calendar.SUNDAY); // 날짜를 현재 주의 시작 날짜인 일요일로 이동
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = "", endDate = "";

        startDate = df.format(c.getTime());

        c.add(Calendar.DATE,6); // 날짜를 현재 주의 끝 날짜인 토요일로 이동
        endDate = df.format(c.getTime());

        String[] parsingDate = new String[3];

        parsingDate = startDate.split("-"); // 시작 날짜 년, 월, 일 split

        // 0000년 0월 0일-0000년 0월 0일 포맷
        week = String.format("%s년 %s월 %s일",parsingDate[0],parsingDate[1],parsingDate[2]);
        week += "-";

        parsingDate = endDate.split("-"); // 끝 날짜 년, 월, 일 split
        week += String.format("%s년 %s월 %s일",parsingDate[0],parsingDate[1],parsingDate[2]);

        return week;
    }

    // 파라미터 날짜의 전날 반환
    public Date prevDay(Date date)
    {
        Calendar c = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        c.setTime(date);
        c.add(Calendar.DATE, -1);
        Date prevDate = stringToDate(df.format(c.getTime()));
        return prevDate;
    }

    // 파라미터 날짜의 다음날 반환
    public Date nextDay(Date date)
    {
        Calendar c = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        c.setTime(date);
        c.add(Calendar.DATE, 1);
        Date nextDate = stringToDate(df.format(c.getTime()));
        return nextDate;
    }

    // 파라미터 날짜의 일만 반환
    public String getDay(Date date)
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(date).split("-")[2];
    }

    // 파라미터 날짜의 요일 숫자로 반환 ( 1 : 일요일 , ... , 7: 토요일 )
    public int getWeekDay(Date date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_WEEK);
    }

    // 파라미터 날짜의 일주일 전 날짜 반환
    public Date prevWeek(Date date)
    {
        Calendar c = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        c.setTime(date);
        c.add(Calendar.DATE, -7);
        Date prevDate = stringToDate(df.format(c.getTime()));
        return prevDate;
    }

    // 파라미터 날짜의 일주일 후 날짜 반환
    public Date nextWeek(Date date)
    {
        Calendar c = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        c.setTime(date);
        c.add(Calendar.DATE, 7);
        Date nextDate = stringToDate(df.format(c.getTime()));
        return nextDate;
    }

    // 파라미터 날짜의 월 반환
    public String getMonth(Date date)
    {
        String month = "";

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        String day = df.format(date);
        String[] parsingDate = day.split("-");
        month += String.format("%s년 %s월",parsingDate[0],parsingDate[1]);
        return month;
    }

    // 파라미터 날짜의 한달 전 날짜 반환
    public Date prevMonth(Date date)
    {
        Calendar c = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        c.setTime(date);
        c.add(Calendar.MONTH, -1);
        Date prevDate = stringToDate(df.format(c.getTime()));
        return prevDate;
    }

    // 파라미터 날짜의 한달 후 날짜 반환
    public Date nextMonth(Date date)
    {
        Calendar c = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        c.setTime(date);
        c.add(Calendar.MONTH, 1);
        Date nextDate = stringToDate(df.format(c.getTime()));
        return nextDate;
    }

    // 파라미터 String형 날짜를 Date형으로 변환
    public Date stringToDate(String date)
    {
        SimpleDateFormat trans = new SimpleDateFormat("yyyy-MM-dd");
        Date to = null;
        try {
            to = trans.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return to;
    }

    // 파라미터 Date형 날짜를 String형으로 변환
    public String dateToString(Date date)
    {
        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        String to = fm.format(date);

        return to;
    }


}
