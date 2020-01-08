package statki.battleship;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import statki.battleship.R;


public class MainMenu extends Activity {
    Button single;
    Button multi;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        //Buttons
        single = (Button) findViewById(R.id.single);
        multi = (Button) findViewById(R.id.multi);

        single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainMenu.this, PlaceShipsActivity.class); //Intent i = new Intent(getApplicationContext(), PlaceShipsActivity.class);
                startActivity(i);
            }
        });

        multi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainMenu.this, ConnectionActivity.class);
                startActivity(i);
            }
        });
    }
}
