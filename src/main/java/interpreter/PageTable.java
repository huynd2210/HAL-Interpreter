package interpreter;

import java.util.*;

public class PageTable {
    public Map<Integer, PageInformation> pageNumberAndPageInformationMap;
    public Deque<Integer> freePages;
    public Deque<List<Object>> fifoQueueForReplacement;
    private final int numberOfPhysicalPages = 4;

    public PageTable() {
        this.pageNumberAndPageInformationMap = new HashMap<>();
        this.fifoQueueForReplacement = new LinkedList<>();
        this.initEmptyPageMap();
        this.freePages = new LinkedList<>();
        for (int i = 0; i < numberOfPhysicalPages; i++) {
            this.freePages.add(i);
        }
    }

    public void initEmptyPageMap() {
        int numberOfVirtualAddressPages = 64; //2^6 = 64
        for (int i = 0; i < numberOfVirtualAddressPages; i++) {
            pageNumberAndPageInformationMap.put(i, null);
        }
    }

    //Resolve reference query from Interpreter: load,store...etc
    public void resolveQuery(short virtualAddress) throws Exception {
        int virtualPageNumber = this.getPageNumber(virtualAddress);
        if (!this.pageNumberAndPageInformationMap.containsKey(virtualPageNumber)) {
            throw new Exception("Page Number: " + virtualPageNumber + " doesnt exists in table");
        }

        if (this.pageNumberAndPageInformationMap.get(virtualPageNumber) == null) {
            //resolve empty case

        } else {
            resolveTableAccess(virtualPageNumber, virtualAddress);
        }

//
//        if (this.pageNumberAndPageInformationMap.get(pageNumber) == null) {
//            this.insertPageMapEntry(pageNumber);
//        } else {
//            resolveTableAccess(pageNumber, virtualAddress);
//        }

    }

    private void resolveEmptySlot(int pageNumber, short virtualAddress) throws Exception {
        //still remaining free physical memory pages
        if (this.fifoQueueForReplacement.size() < this.numberOfPhysicalPages) {
            this.insertPageMapEntry(pageNumber);
        } else {
//            resolveTableAccessOnPageAbsent(pageNumber, virtualAddress);
        }
    }

    private short resolveTableAccess(int virtualPageNumber, short virtualAddress) {
        PageInformation pageInformation = this.pageNumberAndPageInformationMap.get(virtualPageNumber);
        if (pageInformation.isPresent) {
            pageInformation.isReferenced = true;
        } else {
            resolveTableAccessOnPageAbsentWithFifoReference(virtualAddress);
        }
        return this.virtualToPhysicalAddress(virtualAddress);
    }

    private void resolveTableAccessOnPageAbsentWithFifoReference(short virtualAddress) {
        //resolve seitenersetzung
        while (true) {
            List<Object> pop = this.fifoQueueForReplacement.pop();
            PageInformation tmp = (PageInformation) pop.get(1);
            if (!tmp.isReferenced) {
                //not referenced, therefore it is picked to be replaced i.e find it in the map and set present bit to false.
                int poppedPageNumber = (int) pop.get(0);
                PageInformation replaced = this.pageNumberAndPageInformationMap.get(poppedPageNumber);
                replaced.isPresent = false;

                int queriedPageNumber = this.getPageNumber(virtualAddress);
                PageInformation queriedPageInformation = this.pageNumberAndPageInformationMap.get(queriedPageNumber);
                queriedPageInformation.isPresent = true;
                queriedPageInformation.isReferenced = true;
                queriedPageInformation.physicalPageFrameMask = replaced.physicalPageFrameMask;

                return;
            } else {
                tmp.isReferenced = false;
                this.fifoQueueForReplacement.add(pop);
            }
        }
    }

    private void replacePageRandom() {

    }


    private void insertPageMapEntry(int pageNumber) throws Exception {
        if (this.freePages.isEmpty()) {
            throw new Exception("There are no free pages left");
        }
        int freePage = this.freePages.pop();
        PageInformation pageInformation = new PageInformation(freePage, true, true);
        this.pageNumberAndPageInformationMap.put(pageNumber, pageInformation);
        this.fifoQueueForReplacement.add(List.of(pageNumber, pageInformation));
    }

    public int getPageNumber(short virtualAddress) {
        short virtualPageNumberMask = (short) 64512;
        short maskedAddress = (short) (virtualAddress & virtualPageNumberMask);
        return (maskedAddress >> 10);
    }

    public short virtualToPhysicalAddress(short virtualAddress) {
        //first 6bit, note: 6bits for the entirety of virtual address, but physical address wont fully use 6bits during translation
        int pageNumber = getPageNumber(virtualAddress);
        int offsetMask = 1023;
        int maskedOffset = virtualAddress & offsetMask; //10 offset bits

        int physicalAddress = pageNumberAndPageInformationMap.get(pageNumber).physicalPageFrameMask | maskedOffset;
        return (short) physicalAddress;
    }
}
