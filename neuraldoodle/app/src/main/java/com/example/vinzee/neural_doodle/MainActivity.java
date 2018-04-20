package com.example.vinzee.neural_doodle;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //custom drawing view
    private DrawingView drawView;
    //buttons
    private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn, opacityBtn, magicBtn;
    //sizes
    private float smallBrush, mediumBrush, largeBrush;
    private RequestQueue queue;
    private final String BASE_URL = "http://130.85.94.233:5000";
    private String imgURL = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(this);

        //get drawing view
        drawView = findViewById(R.id.drawing);

        //get the palette and first color button
        LinearLayout paintLayout = findViewById(R.id.paint_colors);
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        //sizes from dimensions
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        //draw button
        drawBtn = findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        //set initial size
        drawView.setBrushSize(mediumBrush);
        drawView.setDrawingCacheEnabled(true);
        drawView.setDrawingCacheBackgroundColor(0xfffafafa);
        drawView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);


        //erase button
        eraseBtn = findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        //new button
        newBtn = findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        //save button
        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        //opacity
        opacityBtn = findViewById(R.id.opacity_btn);
        opacityBtn.setOnClickListener(this);

        //opacity
        magicBtn = findViewById(R.id.magic_btn);
        magicBtn.setOnClickListener(this);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "already Not granted");
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.INTERNET}, 0);

                Log.d("Permission", "Requesting !");
            }
        } else {
            Log.d("Permission", "already Granted");
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    //user clicked paint
    public void paintClicked(View view){
        //use chosen color

        //set erase false
        drawView.setErase(false);
        drawView.setPaintAlpha(100);
        drawView.setBrushSize(drawView.getLastBrushSize());

        if(view != currPaint){
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            drawView.setColor(color);
            // update ui
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint = (ImageButton) view;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View view){
        final Dialog brushDialog;
        ImageButton smallBtn, mediumBtn, largeBtn;

        switch(view.getId()) {
            case R.id.draw_btn:
                //draw button clicked
                brushDialog = new Dialog(this);
                brushDialog.setTitle("Brush size:");
                brushDialog.setContentView(R.layout.brush_chooser);
                //listen for clicks on size buttons
                smallBtn = brushDialog.findViewById(R.id.small_brush);
                smallBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setErase(false);
                        drawView.setBrushSize(smallBrush);
                        drawView.setLastBrushSize(smallBrush);
                        brushDialog.dismiss();
                    }
                });
                mediumBtn = brushDialog.findViewById(R.id.medium_brush);
                mediumBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setErase(false);
                        drawView.setBrushSize(mediumBrush);
                        drawView.setLastBrushSize(mediumBrush);
                        brushDialog.dismiss();
                    }
                });
                largeBtn = brushDialog.findViewById(R.id.large_brush);
                largeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setErase(false);
                        drawView.setBrushSize(largeBrush);
                        drawView.setLastBrushSize(largeBrush);
                        brushDialog.dismiss();
                    }
                });
                //show and wait for user interaction
                brushDialog.show();
                break;

            case R.id.erase_btn:
                //switch to erase - choose size
                brushDialog = new Dialog(this);
                brushDialog.setTitle("Eraser size:");
                brushDialog.setContentView(R.layout.brush_chooser);
                //size buttons
                smallBtn = brushDialog.findViewById(R.id.small_brush);
                smallBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setErase(true);
                        drawView.setBrushSize(smallBrush);
                        brushDialog.dismiss();
                    }
                });
                mediumBtn = brushDialog.findViewById(R.id.medium_brush);
                mediumBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setErase(true);
                        drawView.setBrushSize(mediumBrush);
                        brushDialog.dismiss();
                    }
                });
                largeBtn = brushDialog.findViewById(R.id.large_brush);
                largeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setErase(true);
                        drawView.setBrushSize(largeBrush);
                        brushDialog.dismiss();
                    }
                });
                brushDialog.show();
                break;

            case R.id.new_btn:
                //new button
                AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
                newDialog.setTitle("New drawing");
                newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
                newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        drawView.startNew();
                        dialog.dismiss();
                    }
                });
                newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                newDialog.show();
                break;

            case R.id.opacity_btn:
                //launch opacity chooser
                final Dialog seekDialog = new Dialog(this);
                seekDialog.setTitle("Opacity level:");
                seekDialog.setContentView(R.layout.opacity_chooser);
                //get ui elements
                final TextView seekTxt = seekDialog.findViewById(R.id.opq_txt);
                final SeekBar seekOpq = seekDialog.findViewById(R.id.opacity_seek);
                //set max
                seekOpq.setMax(100);
                //show current level
                int currLevel = drawView.getPaintAlpha();
                seekTxt.setText(currLevel + "%");
                seekOpq.setProgress(currLevel);
                //update as user interacts
                seekOpq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        seekTxt.setText(Integer.toString(progress) + "%");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                });
                //listen for clicks on ok
                Button opqBtn = seekDialog.findViewById(R.id.opq_ok);
                opqBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setPaintAlpha(seekOpq.getProgress());
                        seekDialog.dismiss();
                    }
                });
                //show dialog
                seekDialog.show();
                break;

            case R.id.save_btn:
                //save drawing
                AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
                saveDialog.setTitle("Save drawing");
                saveDialog.setMessage("Save drawing to device Gallery?");
                saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //save drawing
//                        drawView.setDrawingCacheEnabled(true);
//                        drawView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

                        //attempt to save
                        imgURL = MediaStore.Images.Media.insertImage(
                                getContentResolver(), drawView.getDrawingCache(),
                                UUID.randomUUID().toString() + ".png", "drawing");
                        //feedback
                        if (imgURL != null) {
                            Toast.makeText(getApplicationContext(),
                                    "Drawing saved to Gallery: " + imgURL, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Oops! Image could not be saved.", Toast.LENGTH_SHORT).show();
                        }
                        drawView.destroyDrawingCache();
                    }
                });
                saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                saveDialog.show();
                break;

            case R.id.magic_btn:
//                if (imgURL == null) {
//                    Log.d("Volley", "imgURL empty");
//                    Toast.makeText(getApplicationContext(), "imgURL empty!", Toast.LENGTH_SHORT).show();
//                    return;
//                }

                StringRequest stringRequest = new StringRequest(Request.Method.POST, BASE_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                Log.d("Volley", "Request sent");
                                Toast.makeText(getApplicationContext(), "Volley Response is: "+ response.substring(0,500), Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;

                        Toast.makeText(getApplicationContext(), "Volley request error: " + networkResponse.statusCode, Toast.LENGTH_SHORT).show();

                        Log.d("Volley", "Request error: " + networkResponse.statusCode);
                        error.printStackTrace();
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String,String> param = new HashMap<>();

                        Bitmap bitmap;
//                        try {
//                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(imgURL));
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                        bitmap = drawView.getDrawingCache();

                        String images = getStringImage(bitmap);
                        Log.i("String Image :: ",""+images);
                        param.put("files", images);

                        return param;
                    }
                };;
                queue.add(stringRequest);

                break;
        }
    }

    public String getStringImage(Bitmap bitmap){
        Log.i("getStringImage","Bitmap:: " + bitmap);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("Permission", "Granted");
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("Permission", "Not granted");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}

