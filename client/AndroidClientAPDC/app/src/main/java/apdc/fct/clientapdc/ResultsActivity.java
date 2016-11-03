package apdc.fct.clientapdc;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;

import java.io.File;

public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        ImageView imageViewLoad1 = (ImageView) findViewById(R.id.photo1);
        ImageView imageViewLoad2 = (ImageView) findViewById(R.id.photo2);
        ImageView imageViewLoad3 = (ImageView) findViewById(R.id.photo3);
        ImageView imageViewLoad4 = (ImageView) findViewById(R.id.photo4);

        byte[] img1 = Base64.decode(PhotoActivity.p1, Base64.NO_WRAP);
        imageViewLoad1.setImageBitmap(BitmapFactory.decodeByteArray(img1, 0, img1.length));

        byte[] img2 = Base64.decode(PhotoActivity.p2, Base64.NO_WRAP);
        imageViewLoad2.setImageBitmap(BitmapFactory.decodeByteArray(img2, 0, img2.length));

        byte[] img3 = Base64.decode(PhotoActivity.p3, Base64.NO_WRAP);
        imageViewLoad3.setImageBitmap(BitmapFactory.decodeByteArray(img3, 0, img3.length));

        byte[] img4 = Base64.decode(PhotoActivity.p4, Base64.NO_WRAP);
        imageViewLoad4.setImageBitmap(BitmapFactory.decodeByteArray(img4, 0, img4.length));
    }
}
