package com.example.yolov5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private String displayedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OkHttpClient client = new OkHttpClient();
        ImageView imageView = findViewById(R.id.main_image_view);
        TextView textView = findViewById(R.id.image_descr_view);
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8000/api_root/Post/")
                .build();
        Timer timer = new Timer();
        ObjectMapper objectMapper = new ObjectMapper();
        MainActivity activity = this;
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()){
                            JsonNode data = objectMapper.readTree(response.body().string());
                            if (data != null){
                                if (data.size()>0){
                                    JsonNode last = data.get(data.size()-1);
                                    Result result = objectMapper.treeToValue(last, Result.class);
                                    String imageUrl = result.getImageUrl();
                                    if (imageUrl != null){
                                        if (!imageUrl.equals(displayedImage)){
                                            displayedImage = imageUrl;
                                            String finalImageUrl = imageUrl;
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Glide.with(imageView.getContext())
                                                            .load(finalImageUrl)
                                                            .error(androidx.appcompat.R.drawable.test_level_drawable)
                                                            .placeholder(androidx.appcompat.R.drawable.test_level_drawable)
                                                            .into(imageView);
                                                    String message = result.getText();
                                                    String pattern = "\\b\\d+\\s+person(s)?,?\\s*";

                                                    // 정규 표현식 패턴을 컴파일
                                                    Pattern regex = Pattern.compile(pattern);

                                                    // Matcher를 사용하여 문자열에서 일치 항목 찾기
                                                    Matcher matcher = regex.matcher(message);

                                                    // 일치 항목을 공백으로 대체하여 삭제
                                                    String finalStr = matcher.replaceAll("");
                                                    if (finalStr.length()>2){
                                                        if (finalStr.endsWith(",")){
                                                            finalStr = finalStr.replace(",", "");
                                                        }
                                                        String resultText = finalStr
                                                                + "가 감지되었습니다.";

                                                        textView.setText(resultText);
                                                    }
                                                    else {
                                                        textView.setText("물체가 감지되지 않았습니다");
                                                    }



                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            Log.e("dsad ", response.body().string());
                        }
                    }
                });
            }
        };
        timer.schedule(timerTask, 3000, 3000);
;

    }
}