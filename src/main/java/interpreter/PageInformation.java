package interpreter;

public class PageInformation {
    public int physicalPageFrameMask; //contain the mask (first 6bits page frame)
    public boolean isPresent;
    public boolean isReferenced;

    public PageInformation(int pageFrameNumber, boolean isPresent, boolean isReferenced){
        this.physicalPageFrameMask = pageFrameNumber << 10; //10 is length of offset bits
        this.isPresent = isPresent;
        this.isReferenced = isReferenced;
    }
}

