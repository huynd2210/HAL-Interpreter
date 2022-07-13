package interpreter;

import java.util.ArrayList;
import java.util.List;

public class PageInformation {
    public int physicalPageFrameMask; //contain the mask (first 6bits page frame)
    public boolean isPresent;
    public boolean isReferenced;
    public List<Double> data;

    public PageInformation(int pageFrameNumber, boolean isPresent, boolean isReferenced){
        this.physicalPageFrameMask = pageFrameNumber << 10; //10 is length of offset bits
        this.isPresent = isPresent;
        this.isReferenced = isReferenced;
        this.data = new ArrayList<>();
        for (int i = 0; i <= 1024; i++) {
            this.data.add(0d);
        }
    }

    public PageInformation(int physicalPageFrameMask, boolean isPresent, boolean isReferenced, boolean isPhysical){
        this.physicalPageFrameMask = physicalPageFrameMask; //10 is length of offset bits unless it is already computed
        this.isPresent = isPresent;
        this.isReferenced = isReferenced;
        this.data = new ArrayList<>();
        for (int i = 0; i <= 1024; i++) {
            this.data.add(0d);
        }
    }
}

