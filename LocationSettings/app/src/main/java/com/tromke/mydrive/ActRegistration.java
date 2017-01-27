package com.tromke.mydrive;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.ServerValue;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tromke.mydrive.Constants.ConstantsSharedPreferences;
import com.tromke.mydrive.Registration.Interfaces.IntRegistrationView;
import com.tromke.mydrive.Registration.Presenters.PresenterRegistration;
import com.tromke.mydrive.util.Config;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Devrath on 10-09-2016.
 */
public class ActRegistration extends AppCompatActivity implements IntRegistrationView {
    ProgressDialog progressDialog;
    EditText Password, Phone;
    @BindView(R.id.btn_login)
    Button btn_login;

    @BindView(R.id.login_button)
    Button login_button;

    @BindView(R.id.edt_email_id)
    EditText edt_email_id;

    @BindView(R.id.edt_password_id2)
    EditText edt_password_id;

    @BindView(R.id.edt_name_id)
    EditText edt_name_id;

    @BindView(R.id.edt_phone_id2)
    EditText edt_phone_id;

    @BindView(R.id.rootId)
    LinearLayout rootId;


    PresenterRegistration presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_registration);
        //Inject views from butter-knife
        ButterKnife.bind(this);
        Firebase.setAndroidContext(this);
        Password = (EditText) findViewById(R.id.edt_password_id2);
        Phone = (EditText) findViewById(R.id.edt_phone_id2);
        presenter = new PresenterRegistration(this);
        presenter.initPresenter(edt_password_id);
    }


    @OnClick(R.id.btn_login)
    public void registration() {
        //SignUp to server

        if (edt_email_id.getText().toString().length() == 0 || edt_name_id.getText().toString().length() == 0) {
            Toast.makeText(this, "Please provide valid information", Toast.LENGTH_LONG).show();
            return;
        }
        if (Password.getText().toString().length() <= 8) {
            Password.setError("At least 8 char");
            return;
        }
        if (Phone.getText().toString().length() <= 8) {
            Phone.setError("Enter valid Phone Number");
            return;
        }


        //showing progress dialog.
        progressDialog = new ProgressDialog(ActRegistration.this, ProgressDialog.THEME_HOLO_LIGHT);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle(getResources().getText(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();


        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(edt_email_id.getText().toString(), edt_password_id.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ActRegistration.this, " Registed Successfully ", Toast.LENGTH_SHORT).show();
                    addDriver();

                } else {
                    Toast.makeText(ActRegistration.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.cancel();
                }
            }
        });


    }

    public void addDriver() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Driver");
        String Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String driverKey = Uid;
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("email", edt_email_id.getText().toString());
        updates.put("name", edt_name_id.getText().toString());
        updates.put("phone", edt_phone_id.getText().toString());
        updates.put("profileImage", "");
        updates.put("DLImage", "");
        updates.put("hypertrack_id", "2");
        updates.put("RCImage", "");
        updates.put("UUID", Uid);
        updates.put("notification_token", FirebaseInstanceId.getInstance().getToken());
        updates.put("taxPaidImage", "");
        updates.put("insuranceImage", "");
        updates.put("timestamp", ServerValue.TIMESTAMP);
        updates.put("customerId", getResources().getString(R.string.pola_customer_id));
        reference.child(Uid).setValue(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ParseApplication.getSharedPreferences().edit().putBoolean(ConstantsSharedPreferences.DRIVER_REGISTERED, true).commit();
                    Intent intent = new Intent(getApplicationContext(), ActHome.class);
                    intent.putExtra(Config.DRIVER_KEY, driverKey);
                    startActivity(intent);
                    progressDialog.cancel();
                } else {
                    Toast.makeText(ActRegistration.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void registrationSuccess() {

    }

    @Override
    public void registrationFailure() {

    }

    @Override
    public void validationEmailFailure() {
        edt_email_id.setError("Please enter correct email");
    }

    @Override
    public void validationPasswordFailure() {
        edt_password_id.setError("Please enter correct password");
    }

    @Override
    public void validationPasswordSuccess() {
        presenter.storeCredentials(edt_email_id.getText().toString(), edt_password_id.getText().toString());
        startActivity(new Intent(getApplicationContext(), ActHome.class));
    }

    @OnClick(R.id.login_button)
    void loginUser() {
        Intent intent = new Intent(ActRegistration.this, ActLogin.class);
        startActivity(intent);
        finish();
    }
}
