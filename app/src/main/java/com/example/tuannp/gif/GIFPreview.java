package com.example.tuannp.gif;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.ShareToMessengerParams;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;

import java.io.File;

public class GIFPreview extends AppCompatActivity {
    final int REQUEST_CODE_SHARE_TO_MESSENGER = 789;
    final String GIF_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/test.gif";
    final Uri gifUri = Uri.fromFile(new File(GIF_PATH));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        //set up toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.gif_preview_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.gif_preview_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //display gif
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Glide.with(this)
                .load(GIF_PATH)
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView);

        //handle share button
        shareAction();
    }

    private void shareAction() {

        //share facebook
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(gifUri)
                .build();
        //btn share FB
        ShareButton shareButton = (ShareButton)findViewById(R.id.gif_share_facebook);
        shareButton.setShareContent(content);


        //share Messenger
        Button btnShareMessenger = (Button) findViewById(R.id.gif_share_messenger);
        btnShareMessenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mimeType = "video/mp4";
                ShareToMessengerParams shareToMessengerParams =
                        ShareToMessengerParams.newBuilder(gifUri, mimeType)
                                .build();
                MessengerUtils.shareToMessenger(
                        GIFPreview.this,
                        REQUEST_CODE_SHARE_TO_MESSENGER,
                        shareToMessengerParams);
            }
        });


        //share Instagram
        Button btnShareInstagram = (Button) findViewById(R.id.gif_share_instagram);
        btnShareInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareInstagramIntent = new Intent(Intent.ACTION_SEND);
                shareInstagramIntent.setType("image/gif");
                shareInstagramIntent.putExtra(Intent.EXTRA_STREAM, gifUri);
                shareInstagramIntent.setPackage("com.instagram.android");
                startActivity(Intent.createChooser(shareInstagramIntent, "Share GIF using"));
            }
        });


        //share Snapchat
        Button btnShareSnapchat = (Button) findViewById(R.id.gif_share_snapchat);
        btnShareSnapchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareSnapchatIntent = new Intent(Intent.ACTION_SEND);
                shareSnapchatIntent.setType("image/gif");
                shareSnapchatIntent.putExtra(Intent.EXTRA_STREAM, gifUri);
                shareSnapchatIntent.setPackage("com.snapchat.android");
                startActivity(Intent.createChooser(shareSnapchatIntent, "Share GIF using"));
            }
        });


        //share Twitter
        Button btnShareTwitter = (Button) findViewById(R.id.gif_share_twitter);
        btnShareTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareTwitterIntent = new Intent(Intent.ACTION_SEND);
                shareTwitterIntent.setType("image/gif");
                shareTwitterIntent.putExtra(Intent.EXTRA_STREAM, gifUri);
                shareTwitterIntent.setPackage("com.twitter.android");
                startActivity(Intent.createChooser(shareTwitterIntent, "Share GIF using"));
            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_gif_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.preview_gif_action_share:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("image/gif");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, gifUri);
                startActivity(Intent.createChooser(sharingIntent, "Share GIF using"));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
