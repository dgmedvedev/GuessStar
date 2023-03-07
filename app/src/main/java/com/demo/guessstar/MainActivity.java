package com.demo.guessstar;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private Button buttonOptionOne;
    private ImageView imageViewPhotoStar;
    private int count = 0;

    ArrayList<String> urlPhotos;
    ArrayList<String> names;
    ArrayList<Bitmap> photos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonOptionOne = findViewById(R.id.buttonOptionOne);
        imageViewPhotoStar = findViewById(R.id.imageViewPhotoStar);

        urlPhotos = new ArrayList<>();
        names = new ArrayList<>();
        photos = new ArrayList<>();
        String url = "https://bodysize.org/ru/top100/";
        DownloadWebContent dc = new DownloadWebContent();
        try {
            String data = dc.execute(url).get();
            Pattern patternPhotos = Pattern.compile("style=\"background-image: url(.*?);");
            Pattern patternNames = Pattern.compile("target=\"_blank\">(.*?)</a>");
            if (data != null) {
                Matcher matcherPhotos = patternPhotos.matcher(data);
                Matcher matcherNames = patternNames.matcher(data);
                while (matcherPhotos.find()) {
                    String subResult = null;
                    String result = matcherPhotos.group(1);
                    if (result != null) {
                        subResult = result.substring(1, result.length() - 1);
                    }
                    urlPhotos.add(subResult);
                }
                while (matcherNames.find()) {
                    names.add(matcherNames.group(1));
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        for (String urls : urlPhotos) {
            System.out.println(urls);
        }
        for (String name : names) {
            System.out.println(name);
        }

//        for (String str : urlPhotos) {
//            try {
//                Bitmap bitmap = dit.execute(str).get();
//                photos.add(bitmap);
//            } catch (ExecutionException | InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
        buttonOptionOne.setOnClickListener(view -> {
            DownloadImageTask dit = new DownloadImageTask();
            Bitmap bitmap;
            try {
                String urlPhoto = urlPhotos.get(0);
                Log.i("URLPhoto", urlPhoto);
                bitmap = dit.execute(urlPhoto).get();
                //bitmap = dit.execute(urlPhotos.get(0)).get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (bitmap != null) {
                photos.add(bitmap);
                imageViewPhotoStar.setImageBitmap(bitmap);
                Toast.makeText(getApplicationContext(), bitmap.toString(), Toast.LENGTH_SHORT).show();
                //imageViewPhotoStar.setImageBitmap(photos.get(count));
                //count++;
            } else {
                Toast.makeText(getApplicationContext(), "bitmap = " + bitmap, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void choiceStar() {

    }

    private static class DownloadWebContent extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder result = new StringBuilder();
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    result.append(line);
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return result.toString();
        }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                return BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }
    }
}