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

public class WeekFragment extends Fragment {
    TextView weekText;
    ImageButton prevButton;
    ImageButton nextButton;
    DayManager dayManager = DayManager.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week,null);
        weekText = (TextView)view.findViewById(R.id.weekText);
        prevButton = (ImageButton)view.findViewById(R.id.leftButton);
        nextButton = (ImageButton)view.findViewById(R.id.rightButton);

        initializeDate();

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayManager.nowDay = dayManager.prevWeek(dayManager.nowDay);
                weekText.setText(dayManager.getWeek(dayManager.nowDay));
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayManager.nowDay = dayManager.nextWeek(dayManager.nowDay);
                weekText.setText(dayManager.getWeek(dayManager.nowDay));
            }
        });
        return view;//inflater.inflate(R.layout.fragment_week, container, false);
    }

    // 오늘 날짜의 한 주로 적용
    void initializeDate(){

        dayManager.nowDay = dayManager.getToday();
        weekText.setText(dayManager.getWeek(dayManager.nowDay));

        dayManager.option = "WEEK";
    }
}