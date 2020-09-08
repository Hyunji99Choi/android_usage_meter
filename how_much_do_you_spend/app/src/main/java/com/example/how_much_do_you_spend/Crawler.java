package com.example.how_much_do_you_spend;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class Crawler extends AsyncTask<String, String, String> {
    String category;

    // category 받아옴
    public String getCategory(String packageName) throws IOException {
        String url = "https://play.google.com/store/apps/details?id=" + packageName;

        Document documnet = Jsoup.connect(url).get(); // 페이지 html 반환
        Element element = documnet.select("div").select("a[itemprop=genre").first();  // a태그의 itemprop genre 반환

        String elementText = element.text();
        category = elementText;

        return category;
    }

    //스레드
    @Override
    protected String doInBackground(String... urls) {
        String result = null;

        try {
            result = getCategory(urls[0]);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
