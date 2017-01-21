package apdc.fct.clientapdc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {
    public final static String IP_ADDR = "fct.apdc.IP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    public void toQuery(View view){
        Intent intent = new Intent(this, MainActivity.class);

        EditText editText = (EditText) findViewById(R.id.editText);
        String ip = editText.getText().toString();

        intent.putExtra(IP_ADDR, ip);
        startActivity(intent);
    }

    public void toPhoto(View view){
        Intent intent = new Intent(this, PhotoActivity.class);

        EditText editText = (EditText) findViewById(R.id.editText);
        String ip = editText.getText().toString();

        intent.putExtra(IP_ADDR, ip);
        startActivity(intent);
    }
}