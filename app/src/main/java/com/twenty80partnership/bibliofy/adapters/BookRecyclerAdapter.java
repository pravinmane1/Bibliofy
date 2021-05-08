package com.twenty80partnership.bibliofy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.R;
import com.twenty80partnership.bibliofy.models.Book;

import java.util.ArrayList;

public class BookRecyclerAdapter extends RecyclerView.Adapter<BookRecyclerAdapter.ViewHolder> {


    ArrayList<Book> bookArrayList = new ArrayList<>();

    public void loadData(ArrayList<Book> bookArrayList){

        this.bookArrayList = bookArrayList;

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView itemImage;
        public TextView itemTitle;
        public TextView itemPublication;
        public TextView itemPrice;


        public ViewHolder(View itemView) {
            super(itemView);
            itemImage =
                    (ImageView) itemView.findViewById(R.id.img);
            itemTitle =
                    (TextView) itemView.findViewById(R.id.title);
            itemPublication =
                    (TextView) itemView.findViewById(R.id.publication);
            itemPrice =
                    itemView.findViewById(R.id.price);

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.order_data_book_row, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        String name = bookArrayList.get(i).getName();
        String publication = bookArrayList.get(i).getPublication();
        String price = bookArrayList.get(i).getDiscountedPrice().toString();
        String img = bookArrayList.get(i).getImg();

        if (name !=null && !name.isEmpty())
        viewHolder.itemTitle.setText(name);

        if (publication!=null && !publication.isEmpty())
        viewHolder.itemPublication.setText(publication);

        if (!price.isEmpty())
        viewHolder.itemPrice.setText("₹ " +price);


        if (img!=null && !img.isEmpty()){
            Picasso.get().load(bookArrayList.get(i).getImg()).into(viewHolder.itemImage);
        }
        else{
            Picasso.get().load(R.drawable.sample_book).into(viewHolder.itemImage);
        }

    }

    @Override
    public int getItemCount() {
        return bookArrayList.size();
    }

}