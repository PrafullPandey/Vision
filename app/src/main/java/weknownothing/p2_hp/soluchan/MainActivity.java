package weknownothing.p2_hp.soluchan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener , TextToSpeech.OnInitListener {
    private Button upload,choose;
    private EditText name;
    private ImageView imgView;
    private final int IMG_REQUEST = 1;
    private Bitmap bitmap;
//    private String Uploadurl = "http://caff2a32.ngrok.io/vihaan/image_save.php" ;
    private String Uploadurl = "http://192.168.43.18/image_save.php" ;

    private static final String TAG = "MainActivity";
    private TextToSpeech tts ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this , this);

        upload = (Button)findViewById(R.id.upload);
        choose = (Button)findViewById(R.id.choose);
        name = (EditText)findViewById(R.id.name);
        imgView=(ImageView)findViewById(R.id.imageView);

        name.setText("Welcome");

        upload.setOnClickListener(this);
        choose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.upload:
                //uploadImage();
                UploadImage upld = new UploadImage();
                upld.execute();
                break;
            case R.id.choose:
                selectImage();
                break;
        }
    }
    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMG_REQUEST);
    }
/*
    private void uploadImage()
    {
        String.format(Uploadurl,ImagetoString(bitmap));
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Uploadurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "*******************onResponse: "+response);
                        name.setText(response.toString());
                        speakOut();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: "+ error);

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("name",name.getText().toString().trim());
                params.put("image",ImagetoString(bitmap));

                return params;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);

        MySingelton.getInstance(MainActivity.this).addToRequestQue(stringRequest);
    }
    */
    private String ImagetoString(Bitmap bitmap)
    {
        Log.d(TAG, "ImagetoString: in");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        Log.d(TAG, "ImagetoString: out");
        return Base64.encodeToString(imgBytes,Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: in");
        if(requestCode==IMG_REQUEST && resultCode==RESULT_OK && data!=null){

            Uri path = data.getData();
           // setPic(path.toString());
            try {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),path);
                imgView.setImageBitmap(bitmap);
                imgView.setVisibility(View.VISIBLE);
                name.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        Log.d(TAG, "onActivityResult: out");
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result =tts.setLanguage(Locale.US);
            tts.setSpeechRate((float) 0.8);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                //btnSpeak.setEnabled(true);
                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    private void speakOut() {

        String text = name.getText().toString();

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void setPic(String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = imgView.getWidth();
        int targetH = imgView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        imgView.setImageBitmap(bitmap);
    }

    private class UploadImage extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
//            String.format(Uploadurl,ImagetoString(bitmap));
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Uploadurl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "*******************onResponse: "+response);

                            name.setText(response.toString());
                            speakOut();

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "onErrorResponse: "+ error);

                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params =new HashMap<>();
                    params.put("name",name.getText().toString().trim());
                    params.put("image",ImagetoString(bitmap));

                    return params;
                }
            };
            RetryPolicy policy = new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            stringRequest.setRetryPolicy(policy);

            MySingelton.getInstance(MainActivity.this).addToRequestQue(stringRequest);


            return null;
        }
    }
}
