package apdc.fct.clientapdc;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class PhotoActivity extends AppCompatActivity {
    private static int IMG_RESULT = 1;
    ImageView imageViewLoad;
    Intent intent;
    String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button LoadImage;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        TextView logs2 = ((TextView) findViewById(R.id.logs2));
        logs2.setMovementMethod(new ScrollingMovementMethod());

        Intent intenti = getIntent();
        ip = intenti.getStringExtra(StartActivity.IP_ADDR);

        imageViewLoad = (ImageView) findViewById(R.id.imageView1);
        LoadImage = (Button) findViewById(R.id.button1);

        LoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMG_RESULT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String ImageDecode;

        try {
            if (requestCode == IMG_RESULT && resultCode == RESULT_OK && null != data) {
                Uri URI = data.getData();
                String[] FILE = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(URI, FILE, null, null, null);

                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(FILE[0]);
                ImageDecode = cursor.getString(columnIndex);
                cursor.close();

                imageViewLoad.setImageBitmap(BitmapFactory.decodeFile(ImageDecode));
                concatLogs(ImageDecode);

                String imgString = Base64.encodeToString(Util.fullyReadFileToBytes(new File(ImageDecode)), Base64.NO_WRAP);

                String msg = "SELECT * FROM images WHERE tag='nature' OR IMAGE ~<" + imgString + ">";
                int size = msg.getBytes().length;
                new Communications().execute(String.format("%010d%s", size, msg));
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            concatLogs("ERROR " + e.getMessage());
        }
    }

    class Communications extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... message) {
            String msg = message[0];
            try {
                Socket s = new Socket(ip, MainActivity.DEFAULT_PROXY_LISTENER_PORT);
                s.setSoTimeout(20000);
                concatLogs("Bound to socket at " + s.getRemoteSocketAddress().toString());

                InputStream is = s.getInputStream();
                OutputStream os = s.getOutputStream();

                os.write(msg.getBytes());
                concatLogs("Sent request...");

                byte[] tmp = new byte[10];
                Thread.sleep(100);
                int get = is.read(tmp);

                concatLogs("Started reading answer...");
                int toReceive = Integer.valueOf(new String(tmp));
                String finalAnswer = "";
                int hasRead = 0;

                while (hasRead < toReceive) {
                    tmp = new byte[toReceive];
                    hasRead += is.read(tmp);
                    concatLogs(hasRead);
                    finalAnswer += new String(tmp);
                }

                concatLogs("All read...");
                s.close();

                return finalAnswer;
            } catch (Exception e) {
                concatLogs("Exception in doInBackground");
                concatLogs(e.getMessage());
                return null;
            }
        }

        protected void onPostExecute(String ans) {
            super.onPostExecute(ans);
            concatLogs("Processing answer...");
            try {
                Intent i = new Intent(PhotoActivity.this, ResultsActivity.class);

                JSONObject obj = new JSONObject(ans);
                concatLogs("\nSuccess: " + obj.get("success"));

                p1 = "" + obj.get("row_image_0");
                p2 = "" + obj.get("row_image_1");
                p3 = "" + obj.get("row_image_2");
                p4 = "" + obj.get("row_image_3");

                startActivity(i);
                concatLogs("Going...");
            } catch (JSONException e) {
                concatLogs("Exception in postExecute1");
                concatLogs(e.getMessage());
            } catch (Exception e) {
                concatLogs("Exception in postExecute2");
                concatLogs(e.getMessage());
            }

            concatLogs("\n--------------------\n");
        }
    }

    public static String p1, p2, p3, p4;

    public void toGallery(View view) {
        Intent i = new Intent(PhotoActivity.this, ResultsActivity.class);

        i.putExtra("photo1", p1);
        i.putExtra("photo2", p2);
        i.putExtra("photo3", p3);
        i.putExtra("photo4", p4);

        startActivity(i);
    }

    private void concatLogs(String aa) {
        final String msg = aa;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView logs = ((TextView) findViewById(R.id.logs2));
                logs.setText(logs.getText() + "\n" + msg);
            }
        });

    }

    private void concatLogs(int msg) {
        concatLogs(String.valueOf(msg));
    }
}