package net.cicerosantos.myshoppinglist.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import net.cicerosantos.myshoppinglist.R;
import net.cicerosantos.myshoppinglist.adapter.AdapterShoppingList;
import net.cicerosantos.myshoppinglist.model.AlertDefault;
import net.cicerosantos.myshoppinglist.model.Item;
import net.cicerosantos.myshoppinglist.settings.Settings;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference, itemRef;
    private AlertDialog alert;
    private RecyclerView recyclerView;
    private AdapterShoppingList adapterShoppingList;
    private Item item;
    private EditText edtDescription;
    private MaterialSearchView searchView;
    private Toolbar toolbar;

    private String strDescription, idUser;
    private List<Item> itemList = new ArrayList<>();
    private List<Item> itemListSearch = new ArrayList<>();
    public boolean search = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar_);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fabSave);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                addNewItem();
            }
        });

        initComponents();
    }

    private void initComponents() {

        databaseReference = itemRef = Settings.getDatabaseReference();
        firebaseAuth = Settings.getFirebaseAuth();
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.search_view);

        verify();

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

                toolbar.setVisibility(View.GONE);
                searchView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onSearchViewClosed() {
                toolbar.setVisibility(View.VISIBLE);
                //searchView.setVisibility(View.GONE);
                settingsRecycler(itemListSearch);
            }
        });

        //litner para efetuar a busca
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if ( query != null && !query.isEmpty()){
                    query( query.toLowerCase() );
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if ( newText != null && !newText.isEmpty()){
                    query( newText.toLowerCase() );
                }

                return true;
            }
        });
    }

    private void query(String text){
        search = true;
        itemListSearch.clear();
        for (Item item : itemList ){
            String description = item.getDescription().toLowerCase();
            if ( description.contains(text)){
                itemListSearch.add(item);
            }
        }
        settingsRecycler(itemListSearch);
        settingsSwaip();
    }

    private void requestItens() {
        AlertDefault.getProgress("Loadding", "Please wait", this);
        final DatabaseReference itensRef = databaseReference.child("shopping_list").child(idUser);
        itensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    item = ds.getValue(Item.class);
                    item.setId(ds.getKey());
                    itemList.add(item);
                }

                settingsRecycler(itemList);
                AlertDefault.progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNewItem(){
        LayoutInflater li =  getLayoutInflater();
        final View view = li.inflate(R.layout.layout_new_item, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //view.setPadding(10,10, 50,100);
        builder.setView(view);
        alert = builder.create();
        alert.setCancelable(false);
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.show();

        edtDescription = view.findViewById(R.id.edtNewItem);

        view.findViewById(R.id.btnSaveItem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                strDescription = edtDescription.getText().toString();

                item = new Item();
                item.setDescription(strDescription);
                item.setPriority("0");
                item.setId(idUser);
                item.save(item);

                alert.dismiss();

                requestItens();
            }
        });
        view.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //adapterShoppingList.notifyDataSetChanged();
                alert.dismiss();
            }
        });

    }

    private void settingsRecycler(List list){
        //configurando o adapter
        adapterShoppingList = new AdapterShoppingList(list, this );

        //configurando o recyclear
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( this );
        recyclerView.setLayoutManager( layoutManager );
        recyclerView.setHasFixedSize( true );
        //capturaScrolled();
        recyclerView.setAdapter( adapterShoppingList );

        settingsSwaip();

    }

    private void verify(){
        if (firebaseAuth.getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }else{
            idUser = firebaseAuth.getCurrentUser().getUid();
            requestItens();
        }
    }

    public void settingsSwaip(){
        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

                int draFlags = ItemTouchHelper.ACTION_STATE_DRAG;
                int swipeFlags = ItemTouchHelper.RIGHT;

                return makeMovementFlags(draFlags, swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                deleteItem( viewHolder );
            }
        };

        new ItemTouchHelper( itemTouch ).attachToRecyclerView( recyclerView );
    }

    public void deleteItem(final RecyclerView.ViewHolder viewHolder){

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Warning...");
        dialog.setMessage("Are you sure you want to delete this item?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int position = viewHolder.getAdapterPosition();
                item = new Item();
                if (search == true){
                    item.setId(itemListSearch.get(position).getId());
                }else {
                    item.setId(itemList.get(position).getId());
                }

                if (item.delete(item.getId())){
                    AlertDefault.getToast("Item deleted succesfully!", MainActivity.this);
                    requestItens();
                }else {
                    AlertDefault.getToast("Error deleted the item!", MainActivity.this);
                    adapterShoppingList.notifyItemRemoved( position );
                }
            }
        });
        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                adapterShoppingList.notifyDataSetChanged();
            }
        });
        dialog.create();
        dialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.search_view);
        searchView.setMenuItem(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.putExtra("edit", "edit");
            startActivity(intent);

        }else if (id == R.id.action_exit){
            firebaseAuth.signOut();
            verify();
        }else if (id == R.id.action_reload){
            search = false;
            settingsRecycler(itemList);
        }

        return super.onOptionsItemSelected(item);
    }

}
