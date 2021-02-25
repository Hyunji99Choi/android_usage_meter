package com.example.how_much_do_you_spend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

// 초기화면, 대표화면, 로딩화면 class
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Thread.sleep(2000); //(초기화면, 대표화면, 로딩화면) 대기 시간
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Intent intent;
        if(checkAutoLogin()){ //자동 로그인 확인
            Log.d("debug","asdfd");
            intent = new Intent(getApplicationContext(),MainActivity.class); //메인 페이지
        }else{ //자동 로그인 실패
            intent = new Intent(getApplicationContext(),LoginActivity.class); //로그인 페이지
        }
        startActivity(intent);
        finish();
    }

    //자동로그인 확인
    boolean checkAutoLogin(){
        // 저장된 값 호출
        SharedPreferences loginInfo = getSharedPreferences("loginInfo",MODE_PRIVATE);
        String name = loginInfo.getString("name",""); // 이메일 호출, default ""반환
        String phoneNum = loginInfo.getString("phoneNum","");// 비밀번호 호출, default ""반환
        // 이메일, 패스워드가 저장되어 있고 로그인 성공하면 true 반환
        try {
            if(!name.equals("") && !phoneNum.equals("") && logInCheck(name,phoneNum).getBoolean("result")){
                AppInfo.getInstance().setName(name);
                AppInfo.getInstance().setPhoneNum(phoneNum);
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    public JSONObject logInCheck(String name, String phoneNum){
        JSONObject res_obj=null;
        RequestToServer reqToServer = new RequestToServer(); // 서버 요청 클래스
        JSONObject req_json = new JSONObject(); // req body json
        JSONObject responseMsg = null; // res message

        try { //json 형태 데이터 만들기
            req_json.put("name" ,name);
            req_json.put("phoneNum", phoneNum);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (req_json.length() > 0) {
            try {
                //동기 통신
                //reqToserver execute / params 0 = GET OR POST / 1 = call function / 2 = request json
                res_obj = new JSONObject(reqToServer.execute("POST", "users/login",String.valueOf(req_json)).get());
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return res_obj;
    }
}
