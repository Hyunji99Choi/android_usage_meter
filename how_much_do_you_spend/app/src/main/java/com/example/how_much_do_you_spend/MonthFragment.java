package com.example.how_much_do_you_spend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MonthFragment extends Fragment {
    TextView monthText;
    ImageButton prevButton;
    ImageButton nextButton;
    DayManager dayManager = DayManager.getInstance();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_month,null);

        monthText = (TextView)view.findViewById(R.id.monthText);
        prevButton = (ImageButton)view.findViewById(R.id.leftButton);
        nextButton = (ImageButton)view.findViewById(R.id.rightButton);

        initializeDate();

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayManager.nowDay = dayManager.prevMonth(dayManager.nowDay);
                monthText.setText(dayManager.getMonth(dayManager.nowDay));
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayManager.nowDay = dayManager.nextMonth(dayManager.nowDay);
                monthText.setText(dayManager.getMonth(dayManager.nowDay));
            }
        });
        return view;
    }

    // 오늘 날짜의 월로 적용
    void initializeDate(){
        DayManager.getInstance().nowDay = DayManager.getInstance().getToday();
        monthText.setText(DayManager.getInstance().getMonth(DayManager.getInstance().nowDay));

        DayManager.getInstance().option = "MONTH";
    }
}