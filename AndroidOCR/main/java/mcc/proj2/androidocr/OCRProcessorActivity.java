package mcc.proj2.androidocr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by Rakesh on 11/10/2016
 */

enum OCRProcessingMode {

    LOCAL,
    REMOTE,
    BENCHMARK
}

public class OCRProcessorActivity extends AppCompatActivity {

    private static String TAG = "OCRProcessorActivity:  ";

    private RadioGroup rg_operating_mode;
    private RadioGroup rg_image_source;
    private ListView list_remote_processed_history;

    // Stores the operating mode
    OCRProcessingMode m_processingMode;

    // Image gallery related variables
    private ArrayList<String> imagesPathList;
    private final int PICK_IMAGE_MULTIPLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_operations);

        rg_operating_mode = (RadioGroup) findViewById(R.id.rg_operating_mode);
        rg_image_source = (RadioGroup) findViewById(R.id.rg_imgSource);
        list_remote_processed_history = (ListView) findViewById(R.id.lv_remote_history);

        // Attach listeners to the control elements
        attachListeners();

        // Set defaults in the control elements
        setDefaults();

        // Populate history list
        populateHistory();
    }

    private void attachListeners() {

        rg_operating_mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                handle_operating_mode_change(checkedId);
            }
        });

        rg_image_source.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                handle_image_source_selection_change(checkedId);
            }
        });

        list_remote_processed_history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Use position to determine the selected item
                handle_selected_list_item(position);
            }
        });
    }

    private void setDefaults() {

        // By default, check remote operating mode
        rg_operating_mode.check(R.id.rb_mode_remote);
        m_processingMode = OCRProcessingMode.REMOTE;
    }

    private void populateHistory() {

        // Invoke backend and fetch the list
    }

    private void handle_operating_mode_change(int checkedID) {

        switch (checkedID) {

            case R.id.rb_mode_local:
                m_processingMode = OCRProcessingMode.LOCAL;
                break;

            case R.id.rb_mode_remote:
                m_processingMode = OCRProcessingMode.REMOTE;
                break;

            case R.id.rb_mode_benchmark:
                m_processingMode = OCRProcessingMode.BENCHMARK;
                break;

            default:
                break;
        }
    }

    private void handle_image_source_selection_change(int checkedID) {

        switch (checkedID) {

            case R.id.rb_camera:
                // Check and launch Camera to capture the image
                launchCamera();
                break;

            case R.id.rb_img_gallery:
                // Launch intent to access the gallery
                showGallery();
                break;

            default:
                break;
        }
    }

    private void handle_selected_list_item(int position) {


    }

    private void launchCamera() {
    }

    private void showGallery() {


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_MULTIPLE) {

                imagesPathList = new ArrayList<>();
                String[] imagesPath = data.getStringExtra("data").split("\\|");
                Collections.addAll(imagesPathList, imagesPath);
            }
        }
    }
}
