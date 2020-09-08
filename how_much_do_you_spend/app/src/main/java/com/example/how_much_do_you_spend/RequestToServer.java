package com.example.how_much_do_you_spend;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestToServer extends AsyncTask<String, String, String> {
    String ip = "http://---.---.---.---:----/";
    @Override
    protected String doInBackground(String... params) {
        String method = params[0].toUpperCase(); // GET or POST
        String func = params[1]; // rest api 호출 경로

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        if(method=="" || func == "")
        {
            System.out.println("Check params method or func");
            return null;
        }
        try {
            URL serverUrl = new URL(ip + func);
            System.out.println(serverUrl);
            urlConnection = (HttpURLConnection)serverUrl.openConnection();
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);
            urlConnection.setRequestProperty("content-type","application/json");
            urlConnection.setRequestMethod(method);
            if(method=="POST"){ // method가 POST일 때
                urlConnection.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
            }
            urlConnection.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
            urlConnection.connect();

            if(method=="POST")
            {
                String req_json = params[2]; // 요청 json
                //서버로 보내기위해서 스트림 만듬
                OutputStream outStream = urlConnection.getOutputStream();
                //버퍼를 생성하고 넣음
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                writer.write(req_json);
                writer.flush();
                writer.close();//버퍼를 받아줌
            }

            //서버로 부터 데이터를 받음
            InputStream stream = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();

            String line = "";
            while((line = reader.readLine()) != null){
                buffer.append(line);
            }
            reader.close();
            System.out.println("dafd "+buffer.toString());
            return buffer.toString();//서버로 부터 받은 값을 리턴해줌
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            try {
                if(reader != null){
                    reader.close();//버퍼를 닫아줌
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
