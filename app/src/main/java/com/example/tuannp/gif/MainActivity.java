package com.example.tuannp.gif;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    final int CAMERA = 111;     //CAMERA INTENT
    final int GALLERY = 222;    //GALLERY INTENT
    final int SCALE_SIZE = 500;
    final String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
    ImageAdapter imageAdapter = new ImageAdapter(this);
    ProgressDialog progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set Toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.main_title);

        //Set up GridView to display image list
        GridView gridview = (GridView) findViewById(R.id.gridView);
        gridview.setAdapter(imageAdapter);

        //delete grid item onClick
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.delete_item_title)
                        .setMessage(R.string.delete_item_message)
                        .setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                imageAdapter.deleteImage(position);
                            }
                        })
                        .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });

        //init FFmpeg library
        initFFmpeg();
    }

    private void initFFmpeg() {
        FFmpeg ffmpeg = FFmpeg.getInstance(MainActivity.this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {}
                @Override
                public void onFailure() {}
                @Override
                public void onSuccess() {}
                @Override
                public void onFinish() {}
            });
        } catch (FFmpegNotSupportedException e) {
            Toast.makeText(MainActivity.this,"Device not supported",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //import button
            case R.id.action_add:
                CharSequence from[] = new CharSequence[] {"Camera", "Gallery"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.import_select_title);
                builder.setItems(from, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, CAMERA);
                        }
                        else {
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                            galleryIntent.setType("image/*");
                            startActivityForResult(galleryIntent, GALLERY);
                        }
                    }
                }).show();
                return true;
            //export button
            case R.id.action_export:
                CharSequence to[] = new CharSequence[] {"GIF", "MP4","Cancel"};
                AlertDialog.Builder builderExport = new AlertDialog.Builder(this);
                builderExport.setTitle(R.string.export_select_title);
                builderExport.setItems(to, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            String s = PATH + "/test.gif";
                            if (imageAdapter.exportToGif(s)) {
                                Intent preview = new Intent(MainActivity.this, GIFPreview.class);
                                startActivity(preview);
                                Toast.makeText(MainActivity.this,"Create Success",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(MainActivity.this,"Can not create. Try again !!!",Toast.LENGTH_SHORT).show();
                            }
                        }
                        else if (which == 1) {
                            if (imageAdapter.exportToMP4()) {
                                createMP4();
                                Toast.makeText(MainActivity.this,"Create Success",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(MainActivity.this,"Can not create. Try again !!!",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void addAudio() {
        try {
            copyAudiotoSDCard(R.raw.audio, PATH + "/audio.mp3");
        }
        catch (IOException e) {

        }
        FFmpeg ffmpeg = ffmpeg = FFmpeg.getInstance(MainActivity.this);
        //add audio to video, do not care length
        //-i input.mp4 -i input.mp3 -c copy -map 0:0 -map 1:0 output.mp4
        //add audio to video, care length, priority video length.
        //-i input.mp4 -i input.mp3 -filter_complex [1:0]apad -map 0:0 -map 1:0 -shortest output.mp4
        String[] cmdAddAudio = {
                "-i",
                PATH +"/output.mp4",
                "-i",
                PATH + "/audio.mp3",
//                "-filter_complex",
//                "[1:0]apad",
                "-c",
                "copy",
                "-map",
                "0:v",
                "-map",
                "1:a",
//                "-shortest",
                PATH +"/video.mp4"};
        try {
            ffmpeg.execute(cmdAddAudio, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                    progress = ProgressDialog.show(MainActivity.this, "Progressing", "Adding audio", true);
                }
                @Override
                public void onProgress(String message) {}
                @Override
                public void onFailure(String message) {
                    String s = message;
                    progress.dismiss();
                    Toast.makeText(MainActivity.this,"FFmpeg add audio onFailure",Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onSuccess(String message) {
                    progress.dismiss();
                    clearTempFile();
                    Intent preview = new Intent(MainActivity.this, VideoPreview.class);
                    startActivity(preview);
                }
                @Override
                public void onFinish() {}
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
        }
    }

    private void createMP4() {
        File file = new File(PATH +"/output.mp4");
        file.delete();
        file = new File(PATH +"/video.mp4");
        file.delete();
        FFmpeg ffmpeg = ffmpeg = FFmpeg.getInstance(MainActivity.this);
        String[] cmdCreateMP4 = {
                "-framerate",
                "1",
                "-i",
                PATH +"/video_frame/frame-%00d.jpg",
                "-c:v",
                "libx264",
                "-profile:v",
                "high",
                "-crf",
                "20",
                "-pix_fmt",
                "yuv420p",
                PATH +"/output.mp4"};
        try {
            ffmpeg.execute(cmdCreateMP4, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {progress = ProgressDialog.show(MainActivity.this, "Progressing",
                        "Creating MP4", true);}
                @Override
                public void onProgress(String message) {}
                @Override
                public void onFailure(String message) {
                    String s = message;
                    progress.dismiss();
                    Toast.makeText(MainActivity.this,"FFmpeg create mp4 onFailure",Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onSuccess(String message) {
                    progress.dismiss();
                    addAudio();
                }
                @Override
                public void onFinish() {}
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //get image from gallery
        if (resultCode == RESULT_OK && requestCode == GALLERY) {
            Uri imageUri = data.getData();
            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap bm = BitmapFactory.decodeStream(imageStream);
                imageAdapter.addImage(scaleBitmap(bm));
            } catch (FileNotFoundException e) {
                // Handle the error
            } finally {
                if (imageStream != null) {
                    try {
                        imageStream.close();
                    } catch (IOException e) {
                        // Ignore the exception
                    }
                }
            }
        }
        //get image from camera
        else if (resultCode == RESULT_OK && requestCode == CAMERA) {
            Bitmap bm = (Bitmap) data.getExtras().get("data");
            imageAdapter.addImage(scaleBitmap(bm));
        }
    }
    private Bitmap scaleBitmap(Bitmap bm) {
        return Bitmap.createScaledBitmap(bm, SCALE_SIZE, SCALE_SIZE, false);
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    private void copyAudiotoSDCard(int resourceID, String path) throws IOException {
        InputStream in = getResources().openRawResource(resourceID);
        FileOutputStream out = new FileOutputStream(path);
        byte[] buff = new byte[1024];
        int read = 0;
        try {
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    private void clearTempFile() {
        File directory = new File(PATH + "/video_frame");
        if (directory.isDirectory())
        {
            String[] children = directory.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(directory, children[i]).delete();
            }
        }
        directory.delete();
        new File(PATH + "/audio.mp3").delete();
        new File(PATH + "/output.mp4").delete();
    }
}
