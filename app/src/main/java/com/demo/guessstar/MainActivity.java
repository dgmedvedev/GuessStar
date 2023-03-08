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

    private Button button0;
    private Button button1;
    private Button button2;
    private Button button3;
    private ImageView imageViewStar;

    String url;

    ArrayList<String> urlImages;
    ArrayList<Bitmap> images;
    ArrayList<String> names;
    ArrayList<Button> buttons;

    private int numberOfQuestion;
    private int numberOfRightAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        buttons = new ArrayList<>();
        buttons.add(button0);
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);

        imageViewStar = findViewById(R.id.imageViewStar);
        url = getString(R.string.url);

        urlImages = new ArrayList<>();
        names = new ArrayList<>();
        images = new ArrayList<>();

        getContent();

        playGame();

        button0.setOnClickListener(view -> {
            Button button = (Button) view;
            String tag = button.getTag().toString();
            if (Integer.parseInt(tag) == numberOfRightAnswer) {
                Toast.makeText(this, "Верно", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Неверно, правильный ответ: " +
                        names.get(numberOfQuestion), Toast.LENGTH_SHORT).show();
            }
            playGame();
        });
        button1.setOnClickListener(view -> {
            Button button = (Button) view;
            String tag = button.getTag().toString();
            if (Integer.parseInt(tag) == numberOfRightAnswer) {
                Toast.makeText(this, "Верно", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Неверно, правильный ответ: " +
                        names.get(numberOfQuestion), Toast.LENGTH_SHORT).show();
            }
            playGame();
        });
        button2.setOnClickListener(view -> {
            Button button = (Button) view;
            String tag = button.getTag().toString();
            if (Integer.parseInt(tag) == numberOfRightAnswer) {
                Toast.makeText(this, "Верно", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Неверно, правильный ответ: " +
                        names.get(numberOfQuestion), Toast.LENGTH_SHORT).show();
            }
            playGame();
        });
        button3.setOnClickListener(view -> {
            Button button = (Button) view;
            String tag = button.getTag().toString();
            if (Integer.parseInt(tag) == numberOfRightAnswer) {
                Toast.makeText(this, "Верно", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Неверно, правильный ответ: " +
                        names.get(numberOfQuestion), Toast.LENGTH_SHORT).show();
            }
            playGame();
        });
    }

    private void playGame() {
        generateQuestion();
        imageViewStar.setImageBitmap(images.get(numberOfQuestion));
        for (int i = 0; i < buttons.size(); i++) {
            if (numberOfRightAnswer == i) {
                buttons.get(i).setText(names.get(numberOfQuestion));
            } else {
                int wrongAnswer = generateWrongAnswer();
                buttons.get(i).setText(names.get(wrongAnswer));
            }
        }
    }

    private void generateQuestion() {
        numberOfQuestion = (int) (Math.random() * names.size());
        numberOfRightAnswer = (int) (Math.random() * buttons.size());
    }

    private int generateWrongAnswer() {
        return (int) (Math.random() * names.size());
    }

    private void getContent() {
        DownloadContentTask task = new DownloadContentTask();
        try {
            String content = task.execute(url).get();

            if (content != null) {
                String start = "<div class=\"top-position\">#1</div>";
                String finish = "function top100_get_next_page\\( btn, page, lang \\) \\{";
                Pattern pattern = Pattern.compile(start + "(.*?)" + finish);
                Matcher matcher = pattern.matcher(content);
                String splitContent = "";
                while (matcher.find()) {
                    splitContent = matcher.group(1);
                }
                if (splitContent != null) {
                    Pattern patternImages = Pattern.compile("style=\"background-image: url\\((.*?)\\);");
                    Pattern patternNames = Pattern.compile("target=\"_blank\">(.*?)</a></");
                    Matcher matcherImages = patternImages.matcher(splitContent);
                    Matcher matcherNames = patternNames.matcher(splitContent);
                    while (matcherImages.find()) {
                        urlImages.add(matcherImages.group(1));
                    }
                    while (matcherNames.find()) {
                        names.add(matcherNames.group(1));
                    }
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        for (String urlPhoto : urlImages) {
            try {
                DownloadImageTask dit = new DownloadImageTask();
                Bitmap bitmap = dit.execute(urlPhoto).get();
                if (bitmap != null) {
                    images.add(bitmap);
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class DownloadContentTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();

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
                return result.toString();
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