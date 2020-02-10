package net.cicerosantos.myshoppinglist.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.provider.MediaStore;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import net.cicerosantos.myshoppinglist.R;
import net.cicerosantos.myshoppinglist.model.AlertDefault;
import net.cicerosantos.myshoppinglist.model.User;
import net.cicerosantos.myshoppinglist.settings.Permissao;
import net.cicerosantos.myshoppinglist.settings.Settings;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private static final int SELECAO_GALERIA = 200;

    private CircleImageView imageView;
    private EditText edtName, edtMail, edtPass, edtPass2;
    private CheckBox cbxLoggedIn;
    private ConstraintLayout layoutImg;
    private FloatingActionButton fab;
    private TextInputLayout til1, til2;
    private static Bitmap imgBitmap = null;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initComponents();

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for ( int permisaoResultado : grantResults ){
            if ( permisaoResultado == PackageManager.PERMISSION_DENIED){
                alertaPermissaoNegada();
            }
        }
    }

    public void alertaPermissaoNegada(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Permission denied !");
        dialog.setMessage(" You need permission to continue. ");
        dialog.setCancelable(false);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Permissao.validarPermissoes( permissoesNecessarias, RegisterActivity.this, 1 );
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        dialog.create();
        dialog.show();
    }

    public void onClickUpdatePhoto(View view){
        Intent i = new Intent( Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
        if ( i.resolveActivity( getPackageManager() ) != null ) {
            startActivityForResult( i, SELECAO_GALERIA );
//            AlertDefault.getProgress("Processing","Please wait...", this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( resultCode == RESULT_OK ){
            AlertDefault.getProgress("Wait...","Updated you profile picture", this);

            try {

                switch ( requestCode ){
                    case SELECAO_GALERIA:
                        Uri localImgSelected = data.getData();
                        imgBitmap = MediaStore.Images.Media.getBitmap( getContentResolver(), localImgSelected );
                        break;
                }

                if ( imgBitmap != null){

                    ByteArrayOutputStream boas = new ByteArrayOutputStream();
                    imgBitmap.compress(Bitmap.CompressFormat.JPEG, 60, boas);
                    byte[] dataImageBaos = boas.toByteArray();

                    StorageReference imageRef = storageReference.child("images")
                            .child("profile")
                            .child( firebaseAuth.getCurrentUser().getUid() );

                    UploadTask uploadTask = imageRef.putBytes(dataImageBaos);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            AlertDefault.progressDialog.dismiss();
                            AlertDefault.getToast(" It was not possible to update the photo, try again!", RegisterActivity.this);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            AlertDefault.progressDialog.dismiss();

                            Uri uri = taskSnapshot.getDownloadUrl();
                            User user = new User();
                            user.updatePhotoUser( uri );

                            //exibe imagem carregado no perfil
                            imageView.setImageBitmap( imgBitmap );
                            AlertDefault.getToast("Photo changed successfully! ", RegisterActivity.this);

                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            //menssagem de erro
            AlertDefault.progressDialog.dismiss();
            AlertDefault.getToast(" It was not possible to update the photo, try again!", RegisterActivity.this);

        }
    }


    private void requestDataPut() {

        String edit = getIntent().getStringExtra("edit");

        if (edit!= null && !edit.isEmpty() && !edit.equals("")){
            layoutImg.setVisibility(View.VISIBLE);
            edtMail.setEnabled(false);
            edtPass.setEnabled(false);
            edtPass2.setEnabled(false);
            cbxLoggedIn.setVisibility(View.GONE);
            til1.setVisibility(View.GONE);
            til2.setVisibility(View.GONE);

            edtName.setText(firebaseAuth.getCurrentUser().getDisplayName());
            edtMail.setText(firebaseAuth.getCurrentUser().getEmail());
            if (firebaseAuth.getCurrentUser().getPhotoUrl() != null){
                Picasso.with(this).load(firebaseAuth.getCurrentUser().getPhotoUrl().toString()).into(imageView);
            }

            adOnclickUpdate();

            Permissao.validarPermissoes(permissoesNecessarias, this, 1);
        }else{
            addOnclickSave();
        }
    }

    private void adOnclickUpdate() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateField(edtName)){
                    if (User.updateNameInFirebaseAuth(edtName.getText().toString())){
                        AlertDefault.getToast("Successfully updated name!", RegisterActivity.this);
                    }else {
                        AlertDefault.getToast("Error when updating!", RegisterActivity.this);
                    }

                }else{
                    edtName.requestFocus();
                    edtName.setError("Name invalid!");
                }
            }
        });
    }

    private void addOnclickSave() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateField(edtName)){
                    if (validateField(edtMail)){
                        if (validateField(edtPass)){
                            if (!edtPass.getText().equals(edtPass2.getText())){
                                User user = new User();
                                user.setName(edtName.getText().toString());
                                user.setMail(edtMail.getText().toString());
                                user.setPsss(edtPass.getText().toString());
                                if (user.saveFirebaseAuth(user, RegisterActivity.this)){
                                    if (!cbxLoggedIn.isChecked()){
                                        firebaseAuth.signOut();
                                        Snackbar.make(view, "Sucessfully", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                        loggin();
                                    }else {
                                        Snackbar.make(view, "Sucessfully", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                        main();
                                    }
                                }else {
                                    Snackbar.make(view, "Error :-(", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            }else {
                                edtPass2.requestFocus();
                                edtPass2.setError("Password not match!");
                            }
                        }else {
                            edtPass.requestFocus();
                            edtPass.setError("Password invalid!");
                        }
                    }else {
                        edtMail.requestFocus();
                        edtMail.setError("Mail invalid!");
                    }
                }else {
                    edtName.requestFocus();
                    edtName.setError("Name invalid!");
                }
            }
        });
    }

    private void main() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void loggin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void initComponents() {

        firebaseAuth = Settings.getFirebaseAuth();
        storageReference = Settings.getStorageReference();

        imageView =findViewById(R.id.imgProfile);
        edtName = findViewById(R.id.edtName);
        edtMail = findViewById(R.id.edtMail);
        edtPass = findViewById(R.id.edtPass);
        edtPass2 = findViewById(R.id.edtPass2);
        cbxLoggedIn = findViewById(R.id.cbxLoggedIn);
        layoutImg = findViewById(R.id.layoutImg);
        fab = findViewById(R.id.fabSave);
        til1 = findViewById(R.id.til1);
        til2 = findViewById(R.id.til2);

        requestDataPut();

    }




    private boolean validateField(EditText editText){
        String field = editText.getText().toString();
        if (!field.isEmpty() && !field.equals("") && field.length() > 5){
            return true;
        }
        return false;
    }

}
