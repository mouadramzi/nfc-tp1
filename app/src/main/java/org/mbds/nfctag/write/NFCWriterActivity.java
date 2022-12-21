package org.mbds.nfctag.write;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.mbds.nfctag.R;
import org.mbds.nfctag.model.TagType;

public class NFCWriterActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private NfcTagViewModel viewModel;

    Button ok;
    EditText data;
    View formulaire;
    View cardReader;
RadioButton rdu , rdt  , rdn ;

    // TODO Analyser le code et comprendre ce qui est fait // done
    // TODO Ajouter un formulaire permettant à un utilisateur d'entrer le texte à mettre dans le tag // done
    // TODO Le texte peut être 1) une URL 2) un numéro de téléphone 3) un plain texte
    // TODO Utiliser le view binding
    // TODO L'app ne doit pas crasher si les tags sont mal formattés

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_tag_layout);

        ok = findViewById(R.id.submit);
        data = findViewById(R.id.data);
        formulaire = findViewById(R.id.formulaire);
        cardReader = findViewById(R.id.card_reader);
        rdu =findViewById(R.id.idurl) ;
        rdt =findViewById(R.id.idtext) ;
        rdn =findViewById(R.id.idtele) ;
        // init ViewModel
        viewModel = new ViewModelProvider(this).get(NfcTagViewModel.class);


        //Get default NfcAdapter and PendingIntent instances
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // check NFC feature:
        if (nfcAdapter == null) {
            // process error device not NFC-capable…

        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // single top flag avoids activity multiple instances launching

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Toast.makeText(NFCWriterActivity.this, data.getText(), Toast.LENGTH_SHORT).show();
                formulaire.setVisibility(View.GONE);
                cardReader.setVisibility(View.VISIBLE);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Enable NFC foreground detection
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled()) {
                // TODO afficher un message d'erreur à l'utilisateur si le NFC n'est pas activé
                // TODO rediriger l'utilisateur vers les paramètres du téléphone pour activer le NFC
            } else {
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
            }
        } else {
            // TODO afficher un message d'erreur à l'utilisateur si le téléphone n'est pas NFC-capable
            // TODO Fermer l'activité ou rediriger l'utilisateur vers une autre activité
        }

        viewModel.getTagWritten().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                Toast.makeText(NFCWriterActivity.this, "Tag written", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getWrittenFailed().observe(this, new Observer<Exception>() {
            @Override
            public void onChanged(Exception e) {
                Toast.makeText(NFCWriterActivity.this, "Tag writing failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Disable NFC foreground detection
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }


    /**
     * This method is called when a new intent is detected by the system, for instance when a new NFC tag is detected.
     *
     * @param intent The new intent that was started for the activity.
     */
    @Override
    public void onNewIntent(Intent intent) {
        if (formulaire.getVisibility()==View.GONE) {
            super.onNewIntent(intent);
            String action = intent.getAction();
            // check the event was triggered by the tag discovery
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                    || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                    || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
                // get the tag object from the received intent
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if(rdn.isChecked()==true)
        {

    viewModel.writeTag("tel://"+data.getText().toString(), tag, TagType.TEXT);

        }
        else if (rdu.isChecked()==true)
        {
    viewModel.writeTag("http://"+data.getText().toString(), tag, TagType.TEXT);
        }
        else if (rdt.isChecked()==true)
        {
    viewModel.writeTag(data.getText().toString(), tag, TagType.TEXT);
        }



            } else {
                // TODO Indiquer à l'utilisateur que ce type de tag n'est pas supporté
                Toast myToast = Toast.makeText(this, "Svp! choisir un type", Toast.LENGTH_LONG);
                myToast.show();

            }
        }
    }
}
