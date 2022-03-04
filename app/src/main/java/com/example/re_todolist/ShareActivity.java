package com.example.re_todolist;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.dinuscxj.progressbar.CircleProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShareActivity extends AppCompatActivity implements CircleProgressBar.ProgressFormatter {

    private static final String DEFAULT_PATTERN = "%d%%";
    ImageButton button, exit;
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        button = findViewById(R.id.share);
        layout = findViewById(R.id.layout);
        exit = findViewById(R.id.exitIcon);

        Intent intent = getIntent();
        int achieve = intent.getExtras().getInt("achieve");

        CircleProgressBar circle1 = findViewById(R.id.circlebar_share);
        circle1.setProgress(achieve);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureScreen(layout);
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShareActivity.this, MainActivity.class);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(ShareActivity.this, circle1, "transition");
                startActivity(intent, options.toBundle());
                finish();
            }
        });
    }

    @Override
    public CharSequence format(int progress, int max) {
        return String.format(DEFAULT_PATTERN, (int) ((float) progress / (float) max * 100));
    }

    public void captureScreen(View view) {
        view.setBackgroundColor(Color.parseColor("#ffffff"));
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setBackgroundColor(Color.parseColor("#00000000"));

        if (bitmap == null) {
            Toast.makeText(getApplicationContext(), "변환 오류",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            try {
                File cachePath = new File(getApplicationContext().getCacheDir(), "images");
                cachePath.mkdirs(); // don't forget to make the directory
                FileOutputStream stream = new FileOutputStream(cachePath + "/image.jpeg"); // overwrites this image every time
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                stream.close();

                File newFile = new File(cachePath, "image.jpeg");
                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(),
                        "com.example.re_todolist.fileprovider", newFile);

                Intent Sharing_intent = new Intent(Intent.ACTION_SEND);
                Sharing_intent.setType("image/png");
                Sharing_intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(Sharing_intent, "Share image"));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
