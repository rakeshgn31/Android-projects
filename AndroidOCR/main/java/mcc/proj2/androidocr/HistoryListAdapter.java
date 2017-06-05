package mcc.proj2.androidocr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Rakesh on 11/16/2016
 */

class ProcessedItemInfo {

    Bitmap thumbnail_image;
    String strText;
}

public class HistoryListAdapter extends BaseAdapter {

    private Activity parentActivity;
    private ArrayList<ProcessedItemInfo> processedHistoryData;
    private static LayoutInflater inflater = null;

    public HistoryListAdapter(Activity parent, ArrayList<ProcessedItemInfo> data) {

        parentActivity = parent;
        processedHistoryData = data;
        inflater = (LayoutInflater)parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {

        return processedHistoryData.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {

        return position;
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {

        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if(convertView == null) {

            view = inflater.inflate(R.layout.history_list_item, null);
        }

        ImageView img_thumbnail = (ImageView)view.findViewById(R.id.img_ocr_item_preview);    // Thumbnail image of the processed text
        TextView text = (TextView)view.findViewById(R.id.tv_processed_text_preview);          // Concise display text
        ImageView img_arrow = (ImageView)view.findViewById(R.id.img_arrow); // thumb image

        ProcessedItemInfo item = processedHistoryData.get(position);

        // Set all the values into the list itemS
        Drawable image = new BitmapDrawable(parentActivity.getResources(), item.thumbnail_image);
        img_thumbnail.setBackground(image);
        text.setText(item.strText);
        img_arrow.setImageResource(R.mipmap.ic_arrow);

        return view;
    }
}
