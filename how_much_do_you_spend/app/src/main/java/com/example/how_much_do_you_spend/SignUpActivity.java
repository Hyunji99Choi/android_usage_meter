package com.example.how_much_do_you_spend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

//회원가입 페이지
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{
    ImageButton backButton; // 뒤로가기 버튼
    TextView phoneNumCreateMsgText; // 아이디 생성 가능 여부 메세지 텍스트뷰
    EditText nameEdit; // 이름 입력 텍스트 박스
    EditText phoneNumEdit; // 휴대폰번호 입력 텍스트 박스
    Button signUpButton; // 회원가입 버튼
    Button phoneNumExistCheckButton; // 아이디 중복 확인 버튼
    String existPhoneNumMsg = "이미 전화번호가 등록되어 있습니다";
    String notExistPhoneNumMsg = "사용가능한 전화번호입니다";
    String unUsePhoneNumMsg = "올바르지 않은 전화번호입니다";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // xml과 연결 설정
        backButton = (ImageButton)findViewById(R.id.backButton);
        phoneNumCreateMsgText = (TextView)findViewById(R.id.phoneNumCreateMsgText);

        nameEdit = (EditText)findViewById(R.id.nameEdit);
        phoneNumEdit = (EditText)findViewById(R.id.phoneNumEdit);
        signUpButton = (Button)findViewById(R.id.signUpButton);
        phoneNumExistCheckButton = (Button)findViewById(R.id.phoneNumExistCheckButton);

        // 각 버튼 클릭 이벤트 등록
        backButton.setOnClickListener(this);
        phoneNumExistCheckButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
    }

    // 휴대폰 뒤로가기 버튼
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class); //로그인페이지 호출
        startActivity(intent);
        finish();
    }

    // 클릭 이벤트
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.backButton: //뒤로 버튼 클릭
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.phoneNumExistCheckButton: // 전화번호 중복 확인 버튼
                // ID 생성 가능한지 확인
                checkID();
                break;
            case R.id.signUpButton: //회원가입 버튼 클릭
                if(!checkID()){
                    nameEdit.requestFocus();
                    break;
                }
                // 회원가입 가능하면 성공
                if(requestSignUp()){
                    saveLoginInfo(nameEdit.getText().toString(), phoneNumEdit.getText().toString());
                    intent = new Intent(getApplicationContext(), MainActivity.class); //메인 페이지 진입
                    startActivity(intent);
                    finish();
                } else {
                    showSuccessOrFailMsg(phoneNumCreateMsgText,existPhoneNumMsg+"\n다시 중복체크 해주세요",false);
                }
                break;
        }
    }

    // 아이디 생성 가능한지 확인
    boolean checkID(){
        if(11 != phoneNumEdit.getText().toString().length()){ //길이 체크
            showSuccessOrFailMsg(phoneNumCreateMsgText,unUsePhoneNumMsg,false);
            return false;
        }

        if(!checkExistPhoneNum()){ //중복 체크
            showSuccessOrFailMsg(phoneNumCreateMsgText,existPhoneNumMsg,false);
            return false;
        }

        showSuccessOrFailMsg(phoneNumCreateMsgText,notExistPhoneNumMsg,true);
        return true;
    }

    // 아이디 존재 여부 확인
    boolean checkExistPhoneNum(){
        JSONObject res_obj; // 응답 json
        RequestToServer reqToServer = new RequestToServer(); // 서버 요청 클래스
        JSONObject req_json = new JSONObject(); // 요청 json
        boolean responseMsg = false; // 요청 결과 값
        try {
            req_json.put("phoneNum" , phoneNumEdit.getText().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (req_json.length() > 0) {
            try {
                //reqToserver execute / params 0 = GET OR POST / 1 = call function / 2 = request json
                res_obj = new JSONObject(reqToServer.execute("POST", "users/phoneNum",String.valueOf(req_json)).get());
                try {
                    responseMsg = res_obj.getBoolean("result");

                } catch (JSONException e) {
                    System.out.println(e.toString());
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return responseMsg;
    }

    // 회원 가입 요청
    boolean requestSignUp(){
        JSONObject res_obj; // 응답 json
        RequestToServer reqToServer = new RequestToServer(); // 서버 요청 클래스
        JSONObject req_json = new JSONObject(); // 요청 json
        boolean responseMsg = false; // 요청 결과 값

        try {
            req_json.put("name" , nameEdit.getText().toString());
            req_json.put("phoneNum" , phoneNumEdit.getText().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (req_json.length() > 0) {
            try {
                //reqToserver execute / params 0 = GET OR POST / 1 = call function / 2 = request json
                res_obj = new JSONObject(reqToServer.execute("POST", "users/join",String.valueOf(req_json)).get());
                try {
                    responseMsg = res_obj.getBoolean("result");
                    System.out.println(res_obj);

                } catch (JSONException e) {
                    System.out.println(e.toString());
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return responseMsg;
    }

    // 성공 or 실패 메세지 textview 출력
    void showSuccessOrFailMsg(TextView messageText, String message, boolean success) {

        if(success){ // 성공 메세지
            messageText.setVisibility(View.VISIBLE);
            messageText.setTextColor(Color.parseColor("#8ec96d"));
            messageText.setText(message);
        } else { // 실패 메세지
            messageText.setText(message);
            messageText.setTextColor(Color.parseColor("#ff0000"));
            messageText.setVisibility(View.VISIBLE);
            Animation anim = new AlphaAnimation(0.0f, 1.0f); // 생성자 : 애니메이션 duration 간격 설정
            anim.setDuration(50); // 깜빡임 동작 시간 milliseconds
            anim.setStartOffset(50); // 시작 전 시간 간격 milliseconds
            anim.setRepeatCount(1); // 반복 횟수
            messageText.startAnimation(anim); // 깜빡임 시작
        }
    }

    // 로그인 정보 저장
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
