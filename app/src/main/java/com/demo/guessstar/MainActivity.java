package com.demo.guessstar;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
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
    private Button buttonOptionTwo;
    private Button buttonOptionThree;
    private Button buttonOptionFour;

    private ImageView imageViewPhotoStar;
    private int count = 1;

    ArrayList<String> urlPhotos;
    ArrayList<Bitmap> photos;
    ArrayList<String> names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonOptionOne = findViewById(R.id.buttonOptionOne);
        buttonOptionTwo = findViewById(R.id.buttonOptionTwo);
        buttonOptionThree = findViewById(R.id.buttonOptionThree);
        buttonOptionFour = findViewById(R.id.buttonOptionFour);
        imageViewPhotoStar = findViewById(R.id.imageViewPhotoStar);

        urlPhotos = new ArrayList<>();
        names = new ArrayList<>();
        photos = new ArrayList<>();

        String url = getString(R.string.url);

        DownloadWebContent dwc = new DownloadWebContent();
        try {
            String data = dwc.execute(url).get();
            if (data != null) {
                Pattern patternPhotos = Pattern.compile("style=\"background-image: url(.*?);");
                Pattern patternNames = Pattern.compile("target=\"_blank\">(.*?)</a></");
                Matcher matcherPhotos = patternPhotos.matcher(data);
                Matcher matcherNames = patternNames.matcher(data);
                while (matcherPhotos.find()) {
                    String subResult;
                    String result = matcherPhotos.group(1);
                    if (result != null) {
                        subResult = result.substring(1, result.length() - 1);
                        urlPhotos.add(subResult);
                    }
                }
                while (matcherNames.find()) {
                    names.add(matcherNames.group(1));
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        for (String urlPhoto : urlPhotos) {
            try {
                DownloadImageTask dit = new DownloadImageTask();
                Bitmap bitmap = dit.execute(urlPhoto).get();
                photos.add(bitmap);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        downloadActivity();

        buttonOptionOne.setOnClickListener(view -> {
            if (count >= photos.size()) {
                count = 0;
            }
            imageViewPhotoStar.setImageBitmap(photos.get(count));
            count++;
        });
    }

    private void downloadActivity() {
        if (urlPhotos != null && photos != null && names != null) {
            imageViewPhotoStar.setImageBitmap(photos.get(0));
            buttonOptionOne.setText(names.get(3));
            buttonOptionTwo.setText(names.get(1));
            buttonOptionThree.setText(names.get(0));
            buttonOptionFour.setText(names.get(2));
        } else {
            Toast.makeText(getApplicationContext(), "Error data", Toast.LENGTH_SHORT).show();
        }
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