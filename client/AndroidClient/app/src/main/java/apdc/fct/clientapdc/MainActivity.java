package apdc.fct.clientapdc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "fct.apdc.MESSAGE";
    public static final int DEFAULT_PROXY_LISTENER_PORT = 5482;
    public String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        ip = intent.getStringExtra(StartActivity.IP_ADDR);
    }

    public void sendMessage(View view) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            concatLogs((activeNetworkInfo != null) + " " + activeNetworkInfo.isConnected());

            EditText editText = (EditText) findViewById(R.id.edit_message);
            TextView logs = ((TextView) findViewById(R.id.logs));
            logs.setMovementMethod(new ScrollingMovementMethod());

            String msg = editText.getText().toString();
            int size = msg.getBytes().length;
            new Communications().execute(String.format("%010d%s", size, msg));
        } catch(Exception e) {
            concatLogs(e.toString());
            concatLogs(e.getMessage() + " " + e.getLocalizedMessage());
        }
    }

    class Communications extends AsyncTask<String, Void, String> {
        protected String doInBackground(String ... message) {
            String msg = message[0];
            try {
                Socket s = new Socket(ip, DEFAULT_PROXY_LISTENER_PORT);
                s.setSoTimeout(10000);
                concatLogs("Bound to socket at " + s.getRemoteSocketAddress().toString());

                InputStream is = s.getInputStream();
                OutputStream os = s.getOutputStream();

                os.write(msg.getBytes());

                byte [] tmp = new byte[10];
                Thread.sleep(100);
                int get = is.read(tmp);

                int toReceive = Integer.valueOf(new String(tmp));
                String finalAnswer = "";
                int hasRead = 0;

                while(hasRead < toReceive){
                    tmp = new byte[toReceive];
                    hasRead += is.read(tmp);
                    concatLogs(hasRead);
                    finalAnswer += new String(tmp);
                }
                concatLogs("Finished receiving...");
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
            concatLogs(ans);

            try {
                JSONObject obj = new JSONObject(ans);
                concatLogs("\nSuccess: " + obj.get("success"));
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

    private void concatLogs(String aa){
        final String msg = aa;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView logs = ((TextView) findViewById(R.id.logs));
                logs.setText(logs.getText() + "\n" + msg);
            }
        });

    }

    private void concatLogs(int msg){
        concatLogs("" + msg);
    }
}