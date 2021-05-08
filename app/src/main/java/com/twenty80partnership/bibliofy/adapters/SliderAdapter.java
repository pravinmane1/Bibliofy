package com.twenty80partnership.bibliofy.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofy.AvlCategoryActivity;
import com.twenty80partnership.bibliofy.CommonActivity;
import com.twenty80partnership.bibliofy.R;
import com.twenty80partnership.bibliofy.StationaryItemsActivity;
import com.twenty80partnership.bibliofy.models.SliderItem;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {


    private List<SliderItem> sliderItems;
    private ViewPager2 viewPager2;
    private Context context;

    public SliderAdapter(List<SliderItem> sliderItems, ViewPager2 viewPager2, Context context) {
        this.sliderItems = sliderItems;
        this.viewPager2 = viewPager2;
        this.context = context;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SliderViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.slide_item_container,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        holder.setImage(sliderItems.get(position));
        holder.setListener(sliderItems.get(position));

        if (position == sliderItems.size() - 2) {
            viewPager2.post(runnable);
        }
    }

    @Override
    public int getItemCount() {
        return sliderItems.size();
    }

    class SliderViewHolder extends RecyclerView.ViewHolder {

        private RoundedImageView imageView;

        SliderViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_slide);
        }

        void setImage(SliderItem sliderItem) {
            if (sliderItem.getImg() != null && !sliderItem.getImg().equals(""))
                Picasso.get().load(sliderItem.getImg()).placeholder(R.color.black).into(imageView);
        }

        void setListener(final SliderItem sliderItem) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String type = sliderItem.getType();
                    String course = sliderItem.getCourse();
                    String category = sliderItem.getCategory();

                    if (type!=null && type.equals("books")) {
                        Intent bookIntent = new Intent(context, AvlCategoryActivity.class);

                        if (course!=null && !course.isEmpty() && category!=null && !category.isEmpty()){
                            bookIntent.putExtra("course", course);
                            bookIntent.putExtra("bookType", category);
                            context.startActivity(bookIntent);
                        }

                    } else if (type!=null && type.equals("stationary")) {
                        Intent stationaryIntent = new Intent(context, StationaryItemsActivity.class);

                        if (category!=null && !category.isEmpty()){
                            stationaryIntent.putExtra("stationaryId", category);
                            stationaryIntent.putExtra("categoryName", "Welcome");
                            context.startActivity(stationaryIntent);
                        }
                    }
                }
            });
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            sliderItems.addAll(sliderItems);
            notifyDataSetChanged();
        }
    };
}
