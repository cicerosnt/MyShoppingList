package net.cicerosantos.myshoppinglist.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import net.cicerosantos.myshoppinglist.R;
import net.cicerosantos.myshoppinglist.model.AlertDefault;
import net.cicerosantos.myshoppinglist.model.User;
import net.cicerosantos.myshoppinglist.settings.SettingsFirebase;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    EditText edtMail, edtPass;
    Button btnLogIn, btnNewRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initComponets();
    }

    private void initComponets() {
        firebaseAuth = SettingsFirebase.getFirebaseAuth();
        edtMail = findViewById(R.id.edtMail);
        edtPass = findViewById(R.id.edtPass);
        btnLogIn = findViewById(R.id.btnLogIn);
        btnNewRegister = findViewById(R.id.btnNewRegister);
    }

    public void onClickLogin(View view){
        if (validateField(edtMail)){
            if (validateField(edtPass)){
                User user = new User();
                user.setMail(edtMail.getText().toString().trim());
                user.setPsss(edtPass.getText().toString());
                AlertDefault.getHideKeyBoard(this, edtPass);
                loginUser(user);

            }else{
                edtPass.requestFocus();
                edtPass.setError("Password invalid!");
            }
        }else{
            edtMail.requestFocus();
            edtMail.setError("Mail invalid!");
        }
    }

    public void loginUser(User user){
        AlertDefault.getProgress("Signing", "Please wait", this);
        firebaseAuth.signInWithEmailAndPassword(
                user.getMail(), user.getPsss()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    AlertDefault.progressDialog.dismiss();
                    AlertDefault.getToast("You just log in!", LoginActivity.this);
                    main();
                }else {
                    AlertDefault.progressDialog.dismiss();
                    String  execao = "";
                    try {
                        throw task.getException();
                    }catch ( FirebaseAuthInvalidCredentialsException e){
                        execao = "Usuario e senha não corresponde!";
                    }catch (FirebaseAuthInvalidUserException e){
                        execao = "Usuário não cadastrado!";
                    }catch ( Exception e){
                        AlertDefault.progressDialog.dismiss();
                        execao = "Error: " + e.getMessage();
                    }
                    AlertDefault.getToast( execao, LoginActivity.this);
                }
            }
        });

    }

    private void main() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private boolean validateField(EditText editText){
        String field = editText.getText().toString();
        if (!field.isEmpty() && !field.equals("") && field.length() > 5){
            return true;
        }
        return false;
    }

    public void onClickNewRegister(View view){
        startActivity(new Intent(this, RegisterActivity.class));
    }
}
