package net.cicerosantos.myshoppinglist.model;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import net.cicerosantos.myshoppinglist.settings.Settings;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class User {
    private static FirebaseAuth firebaseAuth = Settings.getFirebaseAuth();
    private static DatabaseReference databaseReference = Settings.getDatabaseReference();
    private String id, name, mail, psss, photo;

    public User() {
    }

    public static boolean saveFirebaseAuth(final User user, final Activity activity){

        try {
            firebaseAuth.createUserWithEmailAndPassword(user.getMail(), user.getPsss())
                    .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                updateNameInFirebaseAuth(user.getName());
                                saveInFirebasDataBase(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(activity, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                //saveInFirebasDataBase(null);
                            }

                            // ...
                        }
                    });
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public static boolean updateNameInFirebaseAuth(final String name){

        try {
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName( name )
                    .build();
            Settings.getFirebaseAuth().getCurrentUser().updateProfile( profile ).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if ( task.isSuccessful() ){
                        User user = new User();
                        user.setName(firebaseAuth.getCurrentUser().getDisplayName());
                        user.setMail(firebaseAuth.getCurrentUser().getEmail());
                        user.update(firebaseAuth.getCurrentUser().getUid());
                    }
                }
            });
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updatePhotoUser(Uri url){
        try {
            UserProfileChangeRequest profile = new UserProfileChangeRequest
                    .Builder()
                    .setPhotoUri( url )
                    .build();
            Settings.getFirebaseAuth().getCurrentUser().updateProfile( profile ).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){

                    }
                }
            });
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private static void saveInFirebasDataBase(User user) {
        String iduser = firebaseAuth.getCurrentUser().getUid();
        user.setId(iduser);
        databaseReference.child("user").child(iduser).setValue(user);

    }

    public void update(String idUsuario){

        DatabaseReference firebase = Settings.getDatabaseReference()
                .child( "user" )
                .child( idUsuario );

        Map<String, Object> valoresUsuario = converterParaMap();
        firebase.updateChildren(valoresUsuario);
    }

    public Map<String, Object> converterParaMap(){

        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("name", getName());
        usuarioMap.put("mail", getMail());

        return usuarioMap;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getId() {
        return id;
    }
    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
    @Exclude
    public String getPsss() {
        return psss;
    }

    public void setPsss(String psss) {
        this.psss = psss;
    }
}
