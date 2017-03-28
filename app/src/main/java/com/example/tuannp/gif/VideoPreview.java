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
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.ShareToMessengerParams;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareButton;

import java.io.File;

public class VideoPreview extends AppCompatActivity {
    final int REQUEST_CODE_SHARE_TO_MESSENGER = 789;
    final String VIDEO_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/video.mp4";
    final Uri videoUri = Uri.fromFile(new File(VIDEO_PATH));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);

        //SET UP TOOLBAR
        Toolbar myToolbar = (Toolbar) findViewById(R.id.video_preview_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.video_preview_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        VideoView mVideoView  = (VideoView) findViewById(R.id.videoView);
        //set controller: play-pause-seek for VideoView
        MediaController mc = new MediaController(this){
            @Override
            public void show(int timeout) {
                super.show(0);
            }
        };
        mc.setAnchorView(mVideoView);
        mc.setMediaPlayer(mVideoView);
        mVideoView.setMediaController(mc);
        //set video uri
        mVideoView.setVideoURI(videoUri);
        //play video
        mVideoView.start();

        //handle share button
        shareAction();
    }

    private void shareAction() {

        //share facebook
        ShareVideo shareVideo = new ShareVideo.Builder()
                .setLocalUrl(videoUri)
                .build();
        ShareVideoContent content = new ShareVideoContent.Builder()
                .setVideo(shareVideo)
                .build();
        ShareButton shareButton = (ShareButton)findViewById(R.id.video_share_facebook);
        shareButton.setShareContent(content);


        //share Messenger
        Button btnShareMessenger = (Button) findViewById(R.id.video_share_messenger);
        btnShareMessenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mimeType = "video/mp4";
                ShareToMessengerParams shareToMessengerParams =
                        ShareToMessengerParams.newBuilder(videoUri, mimeType)
                                .build();
                MessengerUtils.shareToMessenger(
                        VideoPreview.this,
                        REQUEST_CODE_SHARE_TO_MESSENGER,
                        shareToMessengerParams);
            }
        });


        //share Instagram
        Button btnShareInstagram = (Button) findViewById(R.id.video_share_instagram);
        btnShareInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareInstagramIntent = new Intent(Intent.ACTION_SEND);
                shareInstagramIntent.setType("video/mp4");
                shareInstagramIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
                shareInstagramIntent.setPackage("com.instagram.android");
                startActivity(Intent.createChooser(shareInstagramIntent, "Share video using"));
            }
        });


        //share Snapchat
        Button btnShareSnapchat = (Button) findViewById(R.id.video_share_snapchat);
        btnShareSnapchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareSnapchat = new Intent(Intent.ACTION_SEND);
                shareSnapchat.setType("video/mp4");
                shareSnapchat.putExtra(Intent.EXTRA_STREAM, videoUri);
                shareSnapchat.setPackage("com.snapchat.android");
                try {
                    startActivity(Intent.createChooser(shareSnapchat, "Share video using"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(VideoPreview.this,"Can not share to Snapchat.",Toast.LENGTH_SHORT).show();
                }

            }
        });


        //share Twitter
        Button btnShareTwitter = (Button) findViewById(R.id.video_share_twitter);
        btnShareTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareTwitterIntent = new Intent(Intent.ACTION_SEND);
                shareTwitterIntent.setType("video/mp4");
                shareTwitterIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
                shareTwitterIntent.setPackage("com.twitter.android");
                startActivity(Intent.createChooser(shareTwitterIntent, "Share video using"));
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_video_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //share other
            case R.id.preview_video_action_share:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("video/mp4");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
                startActivity(Intent.createChooser(sharingIntent, "Share video using"));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
