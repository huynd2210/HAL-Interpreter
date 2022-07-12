package interpreter;

public class PageInformation {
    public int physicalPageFrameMask; //contain the mask (first 6bits page frame)
    public boolean isPresent;
    public boolean isReferenced;

    public PageInformation(int physicalPageFrameMask, boolean isPresent, boolean isReferenced){
        this.physicalPageFrameMask = physicalPageFrameMask;
        this.isPresent = isPresent;
        this.isReferenced = isReferenced;

    }
}

