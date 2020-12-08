package project.android.allfiltersexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import project.android.allfiltersexample.extfilter.LookupFilter;
import project.android.allfiltersexample.extfilter.SplitChangeFilter;
import project.android.allfiltersexample.extfilter.view.FilterScrollViewPager;
import project.android.allfiltersexample.utils.FileUtil;
import project.android.imageprocessing.FastImageProcessingPipeline;
import project.android.imageprocessing.FastImageProcessingView;
import project.android.imageprocessing.filter.MultiInputFilter;
import project.android.imageprocessing.input.GLTextureOutputRenderer;
import project.android.imageprocessing.input.ImageResourceInput;
import project.android.imageprocessing.output.ScreenEndpoint;

public class SplitChangeExampleActivity extends AppCompatActivity {

    private static final String TAG = "SplitChangeExample";

    private FilterScrollViewPager filterScrollViewPager;
    private FastImageProcessingView fastImageProcessingView;
    private TextView filterNameView;
    private FastImageProcessingPipeline pipeline;

    private GLTextureOutputRenderer input;

    private List<MultiInputFilter> filters;

    private ScreenEndpoint screen;

    private MultiInputFilter curFilter;

    private int curIndex;
    private float curOffset;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_split_change_example);
        init();
    }

    private void init() {
        filterNameView = findViewById(R.id.cur_filter_name);
        filterScrollViewPager = findViewById(R.id.filter_scroll_viewpager);
        filterScrollViewPager.setListener(new FilterScrollViewPager.OnFlingListener() {

            boolean next = false;

            @Override
            public void onFling(boolean up, float absDy) {
            }

            @Override
            public void onMoving(float offset) {
                next = offset < 0;
                switchFilter(next, offset);
            }

            @Override
            public void onUp(float offset) {
                if (curOffset != 0) {
                    switchFilter(offset > 0, true, false);
                }
            }

            @Override
            public void onCancel() {
                if (curOffset != 0) {
                    switchFilter(!next, true, true);
                }
            }
        });

        fastImageProcessingView = findViewById(R.id.fast_image_processing_view);
        pipeline = new FastImageProcessingPipeline();
        fastImageProcessingView.setPipeline(pipeline);

        input = new ImageResourceInput(fastImageProcessingView, this, R.drawable.kukulkan);

        filters = new ArrayList<MultiInputFilter>();

        screen = new ScreenEndpoint(pipeline);

        // init filters
        /** ------------------------------------- */
        filters = getAllFilters();
//        input.addTarget(filters.get(0));
//        pipeline.addRootRenderer(input);
//        pipeline.startRendering();
//
//        curFilter = filters.get(0);
//        curIndex = 0;
        /** ------------------------------------- */
        filterA = filters.get(5);
        filterB = filters.get(6);
        input.addTarget(filterA);
        input.addTarget(filterB);
        pipeline.addRootRenderer(input);
        changeFilter = new SplitChangeFilter(filterA, filterB);
        changeFilter.addTarget(screen);
        pipeline.startRendering();

    }

    private void addFilter(MultiInputFilter filter) {
        filters.add(filter);
        filter.addTarget(screen);
        filter.registerFilterLocation(input);
    }


    private List<MultiInputFilter> getAllFilters() {
        List<MultiInputFilter> filters = new ArrayList<>();
        File dir = new File(FileUtil.getCacheDirectory(this), "filterData");
        File filtersImageHomeDir = new File(dir.getPath(), "filterImg");
        File[] folders = filtersImageHomeDir.listFiles();
        Arrays.sort(folders);
        for (int i = 0, length = folders.length; i < length; i++) {

            File folder = folders[i];
            Log.e(TAG, "getAllFilters: folder = " + folder.getAbsolutePath());
            String[] names = folder.getName().split("_");
            String id = "";
            String firstName = "";
            if (names.length > 2) {
                id = names[1];
                firstName = names[2];
            }
            boolean isMacosx = folder.getPath().toLowerCase().endsWith("__macosx");
            if (!isMacosx) {
                String filePath = folder.getPath() + "/" + "lookup.png";
                // 暂时无解析
                String manifestUrl = folder.getPath() + "/" + "manifest.json";
                if (!new File(filePath).exists()) {
                    Log.e(TAG, "file not exists: " + filePath);
                    continue;
                }
                LookupFilter filter = new LookupFilter(filePath);
                filter.setId(id);
                filter.setName(firstName);
                filters.add(filter);
                addFilter(filter);
            }
        }
        return filters;
    }


    private MultiInputFilter filterA;
    private MultiInputFilter filterB;
    private SplitChangeFilter changeFilter;
    private void switchFilter(boolean next, float offset) {
        int targetIndex = 0;
        if (next) {
            targetIndex = curIndex + 1;
        } else {
            targetIndex = curIndex - 1;
        }
        if (targetIndex < 0) {
            targetIndex = filters.size() - 1;
        }
        if (targetIndex >= filters.size()) {
            targetIndex = 0;
        }
        curOffset = next ? -offset : 1 - offset;
        if (filterA == null) {
            filterA = filters.get(curIndex);
            input.addTarget(filterA);
            Log.e(TAG, "filterA = " + ((LookupFilter)filterA).getName());
        } else {
            input.removeTarget(filterA);
        }

        if (filterB == null) {
            filterB = filters.get(targetIndex);
            input.addTarget(filterB);
            Log.e(TAG, "filterB = " + ((LookupFilter)filterB).getName());
        } else {
            input.removeTarget(filterB);
        }
        if ( changeFilter == null && filterA != null && filterB != null) {
            changeFilter = new SplitChangeFilter(filterA, filterB);
            pipeline.pauseRendering();

            changeFilter.changeFilter(filterA, filterB);

            pipeline.startRendering();
            fastImageProcessingView.requestRender();
            Log.e(TAG, "change at here");
        }
//        if (changeFilter != null) {
//            WindowManager wm = this.getWindowManager();
//            int width = wm.getDefaultDisplay().getWidth();
//            float ration = Math.abs(offset)/width;
//            Log.e(TAG, "ration = " + ration);
//            changeFilter.changeSplitPoint(ration);
//        }
    }

    private void switchFilter(boolean last, boolean smooth, boolean cancel) {
        int size = filters.size();
        int endIndex = curIndex + (last ? -1 : 1);
        int setIndex = curIndex;
        if (cancel) endIndex = curIndex;
        if (endIndex < 0) {
            endIndex = size - 1;
            setIndex = size;
        } else if (endIndex >= size) {
            endIndex = 0;
        }
        // 停止上个滤镜
        stopRendering();
        curIndex = endIndex;
        curOffset = 0;
        Log.e(TAG, "switchFilter: endFilterIndex = " + curIndex);
        // 渲染当前滤镜
        loadFilter();

        if (changeFilter != null) {
            changeFilter.destroy();
            changeFilter = null;
            filterB = null;
            filterA = null;
        }
    }

    private void stopRendering() {
        Log.e(TAG, "stopRendering: last = " + curIndex);
        pipeline.pauseRendering();
        input.removeTarget(filters.get(curIndex));
    }

    private void loadFilter() {
        Log.e(TAG, "loadFilter: new = " + curIndex);
        input.addTarget(filters.get(curIndex));
        filterNameView.setText(((LookupFilter)filters.get(curIndex)).getName());
        pipeline.startRendering();
        fastImageProcessingView.requestRender();
    }
}