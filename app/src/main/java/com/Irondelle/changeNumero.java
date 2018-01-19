package com.Irondelle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


/**
 * Created by Pierre Boucher on 09/01/2018.
 */

public class changeNumero extends Activity {
    String numero;
    EditText mEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_numero);

        Button valider = (Button) findViewById(R.id.button4);
        Button retour = (Button) findViewById(R.id.button3);

        valider.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mEdit = (EditText) findViewById(R.id.editText);
                        numero = mEdit.getText().toString();

                        BdAdapter clientBdd = new BdAdapter(changeNumero.this.getApplicationContext());
                        clientBdd.open();
                        clientBdd.updateNumero(numero);
                        clientBdd.close();
                        finish();
                        Intent intent = new Intent(changeNumero.this, MainActivity.class);
                        startActivity(intent);
                    }});
        retour.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        Intent intent = new Intent(changeNumero.this, MainActivity.class);
                        startActivity(intent);
                    }});
    }
}
