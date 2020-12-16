 package project.android.allfiltersexample.extfilter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class FilterScrollViewPager extends ViewPager {

    private static final String TAG = "FilterScrollViewPager";

    private boolean moving = false;
    private float downX = -1;
    private float downY = -1;
    private float lastx, mx;
    private int touchSlop;
    private int minUpDis;
    private int minFlingDis;

    private OnFlingListener listener;

    public FilterScrollViewPager(@NonNull Context context) {
        this(context, null);
    }

    public FilterScrollViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.minUpDis = context.getResources().getDisplayMetrics().heightPixels / 10;
        this.minFlingDis = touchSlop * 3;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                moving = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (downX == -1) {
                    downX = x;
                    downY = y;
                }
                float dx = getCheckMoveAndDiffY(x, y);
                if (moving && listener != null) {
                    listener.onMoving(dx);
                    lastx = mx;
                    mx = x;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (moving) {
                    checkFling(x, y);
                }
                if (listener != null) {
                    float diffx = getCheckMoveAndDiffY(x, y);
                    // up
                    if (diffx <= -minUpDis && lastx > x) {
                        listener.onUp(diffx);
                    } else if (diffx >= minUpDis && lastx < x) {
                        listener.onUp(diffx);
                    } else {
                        listener.onCancel();
                    }
                }
                moving = false;
                downX = -1;
                downY = -1;
                break;
        }

        return moving;
    }

    public void setListener(OnFlingListener listener) {
        this.listener = listener;
    }

    private float getCheckMoveAndDiffY(float x, float y) {
        final float dx = x - downX;
        final float dy = y - downY;
        final float absDx = Math.abs(dx);
        final float absDy = Math.abs(dy);
        if (absDx > absDy && absDx > touchSlop) moving = true;
        return dx;
    }

    private void checkFling(float x, float y) {
        final float dx = x - downX;
        final float dy = y - downY;
        final float absDx = Math.abs(dx);
        final float absDy = Math.abs(dy);
        if (absDx > absDy && absDx > minFlingDis) {
            if (listener != null) {
                listener.onFling(dx < 0, absDx);
            }
        }
    }

    public interface OnFlingListener {
        void onFling(boolean up, float absDy);

        void onMoving(float dy);

        void onUp(float dy);

        void onCancel();
    }
}
