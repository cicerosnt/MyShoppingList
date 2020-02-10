package net.cicerosantos.myshoppinglist.model;

import com.google.firebase.database.DatabaseReference;

public class ShoppingList {
    private DatabaseReference databaseReference;
    private String id, description, priority;

    public ShoppingList() {
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
