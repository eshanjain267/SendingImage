package step.first.sendingimage;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.*;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText et1,et2,et3;
    TextView tv1;
    Button bt ;
    ImageView bt2;

  private static final int RLI=1;
  private static final int MYPRM=100;
  private File imagePath;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MYPRM:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission to read external storage granted", Toast.LENGTH_LONG).show();
                }
                else
                    {
                        Toast.makeText(this,"Permission to read external storage denied",Toast.LENGTH_LONG).show();
                        finish();
                    }


        }
    }

    public void loadImage(View view) {
        Intent i = new Intent( Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RLI);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode) {
            case RLI:
                if (resultCode == RESULT_OK)
                {
                    Uri selectd_img = data.getData();
                    String[] projection = {android.provider.MediaStore.Images.Media.TITLE, android.provider.MediaStore.Images.Media.DATA};
                    Cursor cursr = getContentResolver().query(selectd_img, projection, null, null, null);
                    cursr.moveToFirst();
                    int titl_index = cursr.getColumnIndex(projection[0]);
                    int data_index = cursr.getColumnIndex(projection[1]);
                    String title = cursr.getString(titl_index);
                    String img_data = cursr.getString(data_index);
                    imagePath=new File(img_data);
                    Uri uri = Uri.fromFile(imagePath);
                    bt2.setImageURI(uri);
                    cursr.close();
                }
        }
    }
    private String encodeToBase64(String absolutePath)
    {
        Bitmap bm = BitmapFactory.decodeFile(absolutePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] b = baos.toByteArray();
        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        et1 = (EditText)findViewById(R.id.et1);
        et2 =(EditText)findViewById(R.id.eT2);
        et3 = (EditText)findViewById(R.id.eT3);
        bt = (Button)findViewById(R.id.bt);
        bt2 = (ImageView) findViewById(R.id.bt1);
        tv1 =(TextView)findViewById(R.id.tV5);


          bt2.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                 if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                 {
                     Toast.makeText(MainActivity.this,"No permission given for reading external storage.Requesting the user. . .",Toast.LENGTH_LONG).show();

                     ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MYPRM);

                     return;

                 }
                 loadImage(view);










              }


              });

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if((!et1.getText().toString().isEmpty()) ||(!et2.getText().toString().isEmpty()) || (!et3.getText().toString().isEmpty()))
                {
                    try {

                        person p = new person();
                        p.setName(et1.getText().toString().trim());
                        p.setAge(Integer.parseInt(et2.getText().toString().trim()));
                        p.setSal(Integer.parseInt(et3.getText().toString().trim()));
                        p.setImg(encodeToBase64(imagePath.getAbsolutePath()));
                        System.out.println(p.getName()+" "+ p.getSal()+" "+p.getAge());
                        Gson js = new Gson();
                        String json = js.toJson(p, person.class);

                        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo ni = cm.getActiveNetworkInfo();
                        if (ni != null && cm != null) {


                            mytask mt = new mytask();
                            mt.execute(json);
                            System.out.println(json);

                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Please check Your Connection", Toast.LENGTH_LONG).show();

                        }
                    }
                    catch(Throwable ex)
                    {
                        Toast.makeText(MainActivity.this, "error" + ex, Toast.LENGTH_LONG).show();
                    }


                }
                else
                {
                    Toast.makeText(MainActivity.this,"Please Enter Right Data", Toast.LENGTH_LONG).show();

                }







            }
        });







    }

    class mytask extends AsyncTask<String,Void,String>
    {


        @Override
        protected String doInBackground(String... strings) {
            Looper.prepare();
            String ans ="Sorry we Can't Connect at Moment \n Try Again After Some Time!!!";
            try {
                String jsonstr = strings[0];
                URL url = new URL("http://192.168.1.214:2030/Mywhatsapp/ReciveImage.jsp");
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type","application/json");
                con.setRequestProperty("Accept","application/json");
                PrintWriter pw = new PrintWriter(con.getOutputStream());
                pw.write(jsonstr);
                System.out.println("2" +jsonstr + "2");
                pw.close();
                InputStream in = con.getInputStream();

                BufferedReader bff = new BufferedReader(new InputStreamReader(in));
                StringBuffer buf = new StringBuffer();
                String line ;
                while((line =bff.readLine())!=null)
                    buf.append(line+"\n");

                ans = buf.toString();






            }
            catch(Throwable th)
            {
                Toast.makeText(MainActivity.this,"error"+th, Toast.LENGTH_LONG).show();
            }



            return ans;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            tv1.setText(s.trim());
        }
    }


}
