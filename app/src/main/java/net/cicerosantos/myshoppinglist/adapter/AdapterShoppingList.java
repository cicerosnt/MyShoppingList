package net.cicerosantos.myshoppinglist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import net.cicerosantos.myshoppinglist.R;
import net.cicerosantos.myshoppinglist.model.Item;

import java.util.List;

public class AdapterShoppingList extends RecyclerView.Adapter<AdapterShoppingList.MyViewHolder> {

    List<Item> items;
    Context context;

    public AdapterShoppingList(List<Item> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_shopping_list, parent, false);
        return new MyViewHolder(itemLista);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Item item = items.get(position);

        holder.id.setText(String.valueOf(position + 1));
        holder.description.setText(item.getDescription());

    }


    @Override
    public int getItemCount() {
        return items.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView id, description, priority;

        public MyViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.txvId);
            description = itemView.findViewById(R.id.txvDescription);
            //priority = itemView.findViewById(id.txvPriority);
        }

    }

}
