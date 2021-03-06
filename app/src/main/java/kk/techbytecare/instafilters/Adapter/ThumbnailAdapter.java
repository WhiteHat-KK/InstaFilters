package kk.techbytecare.instafilters.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zomato.photofilters.utils.ThumbnailItem;

import java.util.List;

import kk.techbytecare.instafilters.Interface.FilterListFragmentListener;
import kk.techbytecare.instafilters.R;
import kk.techbytecare.instafilters.ViewHolder.ThumbnailViewHolder;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailViewHolder> {

    private List<ThumbnailItem> thumbnailItems;
    private FilterListFragmentListener listener;
    private Context context;
    private int selectedIndex;

    public ThumbnailAdapter(List<ThumbnailItem> thumbnailItems, FilterListFragmentListener listener, Context context) {
        this.thumbnailItems = thumbnailItems;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.thumbnail_item,parent,false);

        return new ThumbnailViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, final int position) {
        final ThumbnailItem thumbnailItem = thumbnailItems.get(position);

        holder.thumbnail.setImageBitmap(thumbnailItem.image);
        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFilerSelected(thumbnailItem.filter);
                selectedIndex = position;
                notifyDataSetChanged();
            }
        });

        holder.filter_name.setText(thumbnailItem.filterName);

        if (selectedIndex == position)  {
            holder.filter_name.setTextColor(ContextCompat.getColor(context,R.color.selected_filter));
        }
        else    {
            holder.filter_name.setTextColor(ContextCompat.getColor(context,R.color.normal_filter));
        }
    }

    @Override
    public int getItemCount() {
        return thumbnailItems.size();
    }
}
