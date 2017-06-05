package mcc.proj2.androidocr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by Rakesh on 11/18/2016
 */

public class ImageGalleryManagerActivity extends AppCompatActivity {

    private static final String TAG = "ImageGalleryManager";

    private GridView m_imageGrid;
    private Button m_btnSelect;
    private ImageAdapter m_imageAdapter;

    private int ids[];
    private int m_imageCount;
    private String[] m_arrImagesPath;
    private boolean[] m_thumbnailsSelection;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Get reference to required controls
        m_imageGrid = (GridView) findViewById(R.id.imagesGrid);
        m_btnSelect = (Button) findViewById(R.id.btnSelect);
        m_imageAdapter = new ImageAdapter();

        setGridAdaptor();

        attachListeners();
    }

    @Override
    public void onBackPressed() {

        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    private void setGridAdaptor() {

        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media._ID;

        @SuppressWarnings("deprecation")
        Cursor imagecursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
        int image_column_index = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
        this.m_imageCount = imagecursor.getCount();
        this.m_arrImagesPath = new String[this.m_imageCount];
        ids = new int[m_imageCount];
        this.m_thumbnailsSelection = new boolean[this.m_imageCount];
        for (int i = 0; i < this.m_imageCount; i++) {

            imagecursor.moveToPosition(i);
            ids[i] = imagecursor.getInt(image_column_index);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
            m_arrImagesPath[i] = imagecursor.getString(dataColumnIndex);
        }

        m_imageGrid.setAdapter(m_imageAdapter);
        imagecursor.close();
    }

    private void attachListeners() {

        m_btnSelect.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                final int len = m_thumbnailsSelection.length;
                int cnt = 0;
                String selectImages = "";
                for (int i = 0; i < len; i++) {
                    if (m_thumbnailsSelection[i]) {
                        cnt++;
                        selectImages = selectImages + m_arrImagesPath[i] + "|";
                    }
                }
                if (cnt == 0) {

                    Toast.makeText(getApplicationContext(), "Please select at least one image", Toast.LENGTH_LONG).show();
                } else {

                    Log.d("SelectedImages", selectImages);
                    Intent i = new Intent();
                    i.putExtra("data", selectImages);
                    setResult(Activity.RESULT_OK, i);
                    finish();
                }
            }
        });
    }

    /**
     * This method used to set bitmap.
     * @param imgView represents ImageView
     * @param id represents id
     */
    private void setBitmapToImageView(final ImageView imgView, final int id) {

        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {

                return MediaStore.Images.Thumbnails.getThumbnail(getApplicationContext().getContentResolver(),
                                    id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
            }

            @Override
            protected void onPostExecute(Bitmap result) {

                super.onPostExecute(result);
                imgView.setImageBitmap(result);
            }
        }.execute();
    }

    /**
     *  This class holds the members of each grid
     *  item in the image grid
     */
    class ViewHolder {

        int id;
        ImageView imageInGalleryGrid;
        CheckBox imageSelectCheckBox;
    }

    /**
     *  This class is the adapter to the image grid in the gallery
     */
    class ImageAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        ImageAdapter() {

            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {

            return m_imageCount;
        }

        public Object getItem(int position) {

            return position;
        }

        public long getItemId(int position) {

            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.fragment_gallery_item, null);
                holder.imageInGalleryGrid = (ImageView) convertView.findViewById(R.id.imgViewGalleryItem);
                holder.imageSelectCheckBox = (CheckBox) convertView.findViewById(R.id.cbGalleryImage);
                convertView.setTag(holder);
            } else {

                holder = (ViewHolder) convertView.getTag();
            }

            holder.imageSelectCheckBox.setId(position);
            holder.imageInGalleryGrid.setId(position);
            holder.imageSelectCheckBox.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    CheckBox cb = (CheckBox) v;
                    int id = cb.getId();
                    if (m_thumbnailsSelection[id]) {

                        cb.setChecked(false);
                        m_thumbnailsSelection[id] = false;
                    } else {

                        cb.setChecked(true);
                        m_thumbnailsSelection[id] = true;
                    }
                }
            });

            holder.imageInGalleryGrid.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    int id = holder.imageSelectCheckBox.getId();
                    if (m_thumbnailsSelection[id]) {

                        holder.imageSelectCheckBox.setChecked(false);
                        m_thumbnailsSelection[id] = false;
                    } else {

                        holder.imageSelectCheckBox.setChecked(true);
                        m_thumbnailsSelection[id] = true;
                    }
                }
            });

            try {

                setBitmapToImageView(holder.imageInGalleryGrid, ids[position]);
            } catch (Throwable e) {

                Log.e(TAG, "Exception in setting bitmap to the image gallery grid");
            }

            holder.imageSelectCheckBox.setChecked(m_thumbnailsSelection[position]);
            holder.id = position;

            return convertView;
        }
    }
}