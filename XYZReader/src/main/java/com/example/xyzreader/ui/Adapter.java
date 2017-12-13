package com.example.xyzreader.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.example.xyzreader.ui.MyApplication.picassoWithCache;
import static com.example.xyzreader.util.Util.parsePublishedDate;
class Adapter extends RecyclerView.Adapter<ArticleListActivity.ViewHolder> {
    private Cursor cursor;
    private Context context;
    private LayoutInflater inflater;
    private boolean itemsEnabled = true;

    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    Adapter(Context context, Cursor cursor) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.cursor = cursor;
    }

    void disableAllItems() {
        itemsEnabled = false;
        notifyDataSetChanged();
    }

    void enableAllItems() {
        itemsEnabled = true;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        cursor.moveToPosition(position);
        return cursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ArticleListActivity.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_article, parent, false);
        final ArticleListActivity.ViewHolder vh = new ArticleListActivity.ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()))));
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(ArticleListActivity.ViewHolder holder, int position) {
        holder.itemView.setEnabled(itemsEnabled);
        cursor.moveToPosition(position);
        holder.titleView.setText(cursor.getString(ArticleLoader.Query.TITLE));
        Date publishedDate = parsePublishedDate(cursor);
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {

            holder.subtitleView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + "<br/>" + " by "
                            + cursor.getString(ArticleLoader.Query.AUTHOR)));
        } else {
            holder.subtitleView.setText(Html.fromHtml(
                    outputFormat.format(publishedDate)
                            + "<br/>" + " by "
                            + cursor.getString(ArticleLoader.Query.AUTHOR)));
        }
        picassoWithCache.load(cursor.getString(ArticleLoader.Query.THUMB_URL)).into(holder.thumbnailView);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }
}
