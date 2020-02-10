package net.cicerosantos.myshoppinglist.model;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import net.cicerosantos.myshoppinglist.settings.SettingsFirebase;

public class Item {
    private static DatabaseReference databaseReference = SettingsFirebase.getDatabaseReference();
    private static FirebaseAuth firebaseAuth = SettingsFirebase.getFirebaseAuth();
    private String id, description, priority, position;

    public Item() {
    }

    public static void save(Item item) {
        databaseReference.child("shopping_list")
                .child(item.getId())
                .push()
                .setValue(item);
    }

    public static boolean delete(String id){
        try {
            databaseReference.child("shopping_list")
                    .child( firebaseAuth.getCurrentUser().getUid())
                    .child( id ).removeValue();
            return true;
        }catch (Exception e){

            return false;
        }
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
