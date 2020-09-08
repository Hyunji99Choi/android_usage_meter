package com.example.how_much_do_you_spend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener  {
    TextView findIdPwdText; // 비밀번호 찾기 버튼
    EditText nameEdit; //
    EditText phoneNumEdit;
    TextView logInFailMessageText;
    Button logInButton;
    Button signUpButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        nameEdit = (EditText)findViewById(R.id.nameEdit);
        phoneNumEdit = (EditText)findViewById(R.id.phoneNumEdit);
        logInFailMessageText = (TextView) findViewById(R.id.logInFailMessageText);
        logInButton = (Button) findViewById(R.id.logInButton);
        signUpButton = (Button) findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(this);
        logInButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.signUpButton:
                intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.logInButton:
                // 로그인 성공시 true, 실패 false
                String name = nameEdit.getText().toString();
                String phoneNum = phoneNumEdit.getText().toString();

                JSONObject result = logInCheck(name, phoneNum);
                System.out.println(result);
                try {
                    if(result.getBoolean("result")){

                        saveLoginInfo(name, phoneNum);

                        //메인 엑티비티로 전환
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        loginFailError();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
    public JSONObject logInCheck(String name, String phoneNum){
        JSONObject res_obj=null;
        RequestToServer reqToServer = new RequestToServer(); // 서버 요청 클래스
        JSONObject req_json = new JSONObject(); // req body json
        JSONObject responseMsg = null; // res message

        try {
            req_json.put("name" ,name);
            req_json.put("phoneNum", phoneNum);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (req_json.length() > 0) {
            try {
                //reqToserver execute / params 0 = GET OR POST / 1 = call function / 2 = request json
                res_obj = new JSONObject(reqToServer.execute("POST", "users/login",String.valueOf(req_json)).get());
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return res_obj;
    }

    //login 실패시 실패 메세지 깜빡임 출력
    void loginFailError(){
        logInFailMessageText.setVisibility(View.VISIBLE);
        Animation anim = new AlphaAnimation(0.0f, 1.0f);  // 생성자 : 애니메이션 duration 간격 설정
        anim.setDuration(50); // 깜빡임 동작 시간 milliseconds
        anim.setStartOffset(50);  // 반복 횟수
        anim.setRepeatCount(1); // 시작 전 시간 간격 milliseconds
        logInFailMessageText.startAnimation(anim); // 깜빡임 시작
    }
    void saveLoginInfo(String name, String phoneNum){
        SharedPreferences loginInfo = getSharedPreferences("loginInfo",MODE_PRIVATE);
        SharedPreferences.Editor editor = loginInfo.edit();
        editor.putString("name",name);
        editor.putString("phoneNum",phoneNum);

        AppInfo.getInstance().setName(name);
        AppInfo.getInstance().setPhoneNum(phoneNum);

        editor.commit();
    }
}
