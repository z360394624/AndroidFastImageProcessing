package project.android.allfiltersexample.extfilter;

import java.util.ArrayList;

import project.android.imageprocessing.filter.BasicFilter;
import project.android.imageprocessing.filter.GroupFilter;

public class SplitChangeFilter extends GroupFilter {

    private SplitFilter splitFilter = new SplitFilter();
    private BasicFilter curFilterA;
    private BasicFilter curFilterB;
    private boolean isStashed;
    private BasicFilter mStashedA;
    private BasicFilter mStashedB;

    public SplitChangeFilter(BasicFilter filterA, BasicFilter filterB) {
        this.curFilterA = filterA;
        this.curFilterB = filterB;
        // 两个lookup滤镜的输出目标为SplitFilter
        filterA.addTarget(this.splitFilter);
        filterB.addTarget(this.splitFilter);
        //
        this.splitFilter.registerFilterLocation(filterA, 0);
        this.splitFilter.registerFilterLocation(filterB, 1);
        // SplitFilter的输出目标为当前滤镜
        this.splitFilter.addTarget(this);

        // curFilterA和curFilterB为当前滤镜的输入
        this.registerInitialFilter(this.curFilterA);
        this.registerInitialFilter(this.curFilterB);

        // SplitFilter为此滤镜内置管线的输出节点
        this.registerTerminalFilter(this.splitFilter);

    }

    public ArrayList<BasicFilter> changeFilter(BasicFilter filterA, BasicFilter filterB) {
        synchronized (this.getLockObject()) {
            ArrayList<BasicFilter> result = new ArrayList<>();
            if (this.curFilterA == filterA && this.curFilterB == filterB) {
                return result;
            } else {
                if (this.curFilterA != null) {
                    result.add(this.curFilterA);
                }

                if (this.curFilterB != null) {
                    result.add(this.curFilterB);
                }

                if (this.isStashed) {
                    this.mStashedA = filterA;
                    this.mStashedB = filterB;
                }

                this.removeInitialFilter(this.curFilterA);
                this.removeInitialFilter(this.curFilterB);
                this.removeTerminalFilter(this.splitFilter);
                this.curFilterA.removeTarget(this.splitFilter);
                this.curFilterB.removeTarget(this.splitFilter);
                filterA.addTarget(this.splitFilter);
                filterB.addTarget(this.splitFilter);
                this.splitFilter.registerFilterLocation(filterA, 0);
                this.splitFilter.registerFilterLocation(filterB, 1);
                this.registerInitialFilter(filterA);
                this.registerInitialFilter(filterB);
                this.registerTerminalFilter(this.splitFilter);
                this.curFilterA = filterA;
                this.curFilterB = filterB;
                return result;
            }
        }
    }

    public void changeSplitPoint(float splitPoint) {
        synchronized (this.getLockObject()) {
            splitFilter.setSplitPoint(splitPoint);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (curFilterA != null) {
            curFilterA.destroy();
            curFilterA = null;
        }
        if (curFilterB != null) {
            curFilterB.destroy();
            curFilterB = null;
        }
        if (splitFilter != null) {
            splitFilter.destroy();
            splitFilter = null;
        }
    }
}
