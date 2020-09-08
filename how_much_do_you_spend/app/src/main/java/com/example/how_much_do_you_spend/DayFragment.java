package com.example.how_much_do_you_spend;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DayFragment extends Fragment {
    TextView[] selectText;
    LinearLayout[] selectLayout;
    TextView nowDayText;
    ImageButton prevButton;
    ImageButton nextButton;
    DateFormat df;
    DayManager dayManager = DayManager.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day,null);
        selectText = new TextView[7];
        selectLayout = new LinearLayout[7];
        selectText[0] = (TextView)view.findViewById(R.id.sunSelectText);
        selectText[1] = (TextView)view.findViewById(R.id.monSelectText);
        selectText[2] = (TextView)view.findViewById(R.id.tueSelectText);
        selectText[3] = (TextView)view.findViewById(R.id.wedSelectText);
        selectText[4] = (TextView)view.findViewById(R.id.thuSelectText);
        selectText[5] = (TextView)view.findViewById(R.id.friSelectText);
        selectText[6] = (TextView)view.findViewById(R.id.satSelectText);

        selectLayout[0] = (LinearLayout)view.findViewById(R.id.sunSelectLayout);
        selectLayout[1] = (LinearLayout)view.findViewById(R.id.monSelectLayout);
        selectLayout[2] = (LinearLayout)view.findViewById(R.id.tueSelectLayout);
        selectLayout[3] = (LinearLayout)view.findViewById(R.id.wedSelectLayout);
        selectLayout[4] = (LinearLayout)view.findViewById(R.id.thuSelectLayout);
        selectLayout[5] = (LinearLayout)view.findViewById(R.id.friSelectLayout);
        selectLayout[6] = (LinearLayout)view.findViewById(R.id.satSelectLayout);

        nowDayText = (TextView)view.findViewById(R.id.nowDayText);
        prevButton = (ImageButton)view.findViewById(R.id.leftButton);
        nextButton = (ImageButton)view.findViewById(R.id.rightButton);
        df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


        initializeDate();

        LinearLayout.OnClickListener onClickListener = new LinearLayout.OnClickListener(){
            @Override
            public void onClick(View v) {
                Calendar c;
                c = GregorianCalendar.getInstance();
                c.setTime(dayManager.nowDay);
                switch (v.getId())
                {
                    case R.id.sunSelectLayout:
                        Log.d("debug","ok");
                        c.set(c.DAY_OF_WEEK, Calendar.SUNDAY);
                        break;
                    case R.id.monSelectLayout:
                        c.set(c.DAY_OF_WEEK, Calendar.MONDAY);
                        break;
                    case R.id.tueSelectLayout:
                        c.set(c.DAY_OF_WEEK, Calendar.TUESDAY);
                        break;
                    case R.id.wedSelectLayout:
                        c.set(c.DAY_OF_WEEK, Calendar.WEDNESDAY);
                        break;
                    case R.id.thuSelectLayout:
                        c.set(c.DAY_OF_WEEK, Calendar.THURSDAY);
                        break;
                    case R.id.friSelectLayout:
                        c.set(c.DAY_OF_WEEK, Calendar.FRIDAY);
                        break;
                    case R.id.satSelectLayout:
                        c.set(c.DAY_OF_WEEK, Calendar.SATURDAY);
                        break;
                }
                dayManager.nowDay = c.getTime();
                settingNowDay();
            }
        };

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayManager.nowDay = dayManager.prevWeek(dayManager.nowDay);
                settingNowDay();

            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayManager.nowDay = dayManager.nextWeek(dayManager.nowDay);
                settingNowDay();
            }
        });

        for(int i=0;i<7;++i)
        {
            selectLayout[i].setOnClickListener(onClickListener);
        }

        return view;
    }

    // 오늘 날짜로 적용
    void initializeDate(){
        dayManager.option = "DAY";
        dayManager.nowDay = dayManager.getToday();
        settingNowDay();
    }

    // 현재 날짜로 적용
    void settingNowDay()
    {
        setNowDayText();
        setDayText();
        nowDayDisplay();
    }

    // 현재 날짜 Text를 현재 날짜로 적용
    void setNowDayText()
    {
        String dayStr = dayManager.getFormatDay(dayManager.nowDay);
        nowDayText.setText(dayStr);
    }

    // 선택된 날짜 표시
    void nowDayDisplay()
    {
        for(int i=0;i<7;++i)
        {
            selectText[i].setBackgroundResource(0);
            selectText[i].setTextColor(Color.WHITE);
        }
        selectText[dayManager.getWeekDay(dayManager.nowDay)-1].setBackgroundResource(R.drawable.magnitude_circle);
        selectText[dayManager.getWeekDay(dayManager.nowDay)-1].setTextColor(Color.rgb(105, 134,165));
    }

    // 현재 날짜 기준으로 일주일 각각 날짜들의 일 적용
    void setDayText()
    {
        Calendar c;
        c = GregorianCalendar.getInstance();
        c.setTime(dayManager.nowDay);
        c.set(c.DAY_OF_WEEK, Calendar.SUNDAY);


        String nowDate = df.format(c.getTime());

        for(int i=0;i<7;++i)
        {
            String nowDay = dayManager.getDay(dayManager.stringToDate(nowDate));
            if(nowDay.charAt(0) == '0'){
                nowDay = Character.toString(nowDay.charAt(1));
                selectText[i].setText(nowDay);
            }else{
                selectText[i].setText(nowDay);
            }
            c.add(Calendar.DATE,1);
            nowDate = df.format(c.getTime());
        }
    }

}