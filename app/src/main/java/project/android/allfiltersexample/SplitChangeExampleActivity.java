package project.android.allfiltersexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
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
import project.android.allfiltersexample.extfilter.SplitChangeFilterV2;
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

    private List<LookupFilter> filters;
    private List<String> lookupPath = new ArrayList<>();

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
                Log.e(TAG, "onFling absDy = " + absDy);
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

        filters = new ArrayList<LookupFilter>();

        screen = new ScreenEndpoint(pipeline);

        // init filters
        /** ------------------------------------- */
        filters = getAllFilters();
        input.addTarget(filters.get(0));
        pipeline.addRootRenderer(input);
        pipeline.startRendering();

        curFilter = filters.get(0);
        curIndex = 0;
        /** ------------------------------------- */
//        filterA = filters.get(5);
//        filterB = filters.get(6);
//        input.addTarget(filterA);
//        input.addTarget(filterB);
//        pipeline.addRootRenderer(input);
//        changeFilter = new SplitChangeFilter(filterA, filterB);
//        changeFilter.addTarget(screen);
//        pipeline.startRendering();

//        SplitChangeFilterV2 splitChangeFilterV2 = new SplitChangeFilterV2();
//        splitChangeFilterV2.changeFilter(lookupPath.get(5), lookupPath.get(6));
//        input.addTarget(splitChangeFilterV2);
//        splitChangeFilterV2.addTarget(screen);
//        pipeline.addRootRenderer(input);
//        pipeline.startRendering();
//        fastImageProcessingView.requestRender();

    }

    private void addFilter(LookupFilter filter) {
        filters.add(filter);
        filter.addTarget(screen);
        filter.registerFilterLocation(input);
    }


    private List<LookupFilter> getAllFilters() {
        List<LookupFilter> filters = new ArrayList<>();
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
                lookupPath.add(filePath);
                LookupFilter filter = new LookupFilter(filePath);
                filter.setId(id);
                filter.setName(firstName);
                filters.add(filter);
                addFilter(filter);
            }
        }
        return filters;
    }


//    private MultiInputFilter filterA;
//    private MultiInputFilter filterB;
//    private SplitChangeFilter changeFilter;
    private SplitChangeFilterV2 splitChangeFilterV2;

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

//        if (filterA == null) {
//            filterA = filters.get(curIndex);
//            input.addTarget(filterA);
//            Log.e(TAG, "filterA: = " + ((LookupFilter) filterA).getName() + "; Index = " + curIndex);
//        }
//        if (filterB == null) {
//            filterB = filters.get(targetIndex);
//            input.addTarget(filterB);
//            Log.e(TAG, "filterB = " + ((LookupFilter) filterB).getName() + "; Index = " + targetIndex);
//        }
//        if (changeFilter == null && filterA != null && filterB != null) {
//            changeFilter = new SplitChangeFilter(filterA, filterB);
//            pipeline.pauseRendering();
//            if (targetIndex > curIndex) {
//                changeFilter.changeFilter(filterA, filterB);
//            } else {
//                changeFilter.changeFilter(filterB, filterA);
//            }
//            changeFilter.addTarget(screen);
//            pipeline.startRendering();
//        }
//        if (changeFilter != null) {
//            WindowManager wm = getWindowManager();
//            int width = wm.getDefaultDisplay().getWidth();
//            float ration = Math.abs(offset) / width;
//            changeFilter.changeSplitPoint(next ? 1.0f - ration : ration);
//            fastImageProcessingView.requestRender();
//        }

        if (splitChangeFilterV2 == null) {
            splitChangeFilterV2 = new SplitChangeFilterV2();
            pipeline.pauseRendering();
            input.removeTarget(curFilter);
            input.addTarget(splitChangeFilterV2);
            if (targetIndex > curIndex) {
                splitChangeFilterV2.changeFilter(filters.get(curIndex).getLookupBitmap(), filters.get(targetIndex).getLookupBitmap());
            } else {
                splitChangeFilterV2.changeFilter(filters.get(targetIndex).getLookupBitmap(), filters.get(curIndex).getLookupBitmap());
            }
            splitChangeFilterV2.addTarget(screen);
            pipeline.startRendering();
        }
        if (splitChangeFilterV2 != null) {
            WindowManager wm = getWindowManager();
            int width = wm.getDefaultDisplay().getWidth();
            float ration = Math.abs(offset) / width;
            splitChangeFilterV2.setSplitPoint(next ? 1.0f - ration : ration);
            Log.e(TAG, "switchFilter: requestRender");
            fastImageProcessingView.requestRender();
        }

        /** ------------------------- **/
//        if (changeFilter != null) {
//            changeFilter.changeFilter(filterA, filterB);
//        }
//        WindowManager wm = getWindowManager();
//        int width = wm.getDefaultDisplay().getWidth();
//        float ration = Math.abs(offset) / width;
//        Log.e(TAG, "ration: " + ration);
//        changeFilter.changeSplitPoint(next ? 1.0f - ration : ration);
//        fastImageProcessingView.requestRender();
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
        // 渲染当前滤镜
        loadFilter();

//        if (changeFilter != null) {
//            input.removeTarget(filterA);
//            input.removeTarget(filterB);
//            changeFilter.destroy();
//            changeFilter = null;
//            filterB = null;
//            filterA = null;
//        }

        if (splitChangeFilterV2 != null) {
            input.removeTarget(splitChangeFilterV2);
            splitChangeFilterV2.removeTarget(screen);
            splitChangeFilterV2.destroy();
            splitChangeFilterV2 = null;
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
        filterNameView.setText(((LookupFilter) filters.get(curIndex)).getName());
        pipeline.startRendering();
        fastImageProcessingView.requestRender();
    }
}