package interpreter;

import java.util.*;

public class PageTable {
    public Map<Integer, PageInformation> pageNumberAndPageInformationMap;
    public Deque<Integer> freePages;
    public Deque<List<Object>> fifoQueueForReplacement;
    public StringBuilder logs;
    private final int numberOfPhysicalPages = 4;

    public PageTable() {
        this.pageNumberAndPageInformationMap = new HashMap<>();
        this.fifoQueueForReplacement = new LinkedList<>();
        this.initEmptyPageMap();
        this.freePages = new LinkedList<>();
        for (int i = 0; i < numberOfPhysicalPages; i++) {
            this.freePages.add(i);
        }
        this.logs = new StringBuilder();
    }

    private void initEmptyPageMap() {
        int numberOfVirtualAddressPages = 64; //2^6 = 64
        for (int i = 0; i < numberOfVirtualAddressPages; i++) {
            pageNumberAndPageInformationMap.put(i, null);
        }
    }

    //Resolve reference query from Interpreter: load,store...etc returns the physical address
    public short resolveQuery(short virtualAddress) throws Exception {
        int virtualPageNumber = this.getPageNumber(virtualAddress);
        if (!this.pageNumberAndPageInformationMap.containsKey(virtualPageNumber)) {
            throw new Exception("Page Number: " + virtualPageNumber + " doesnt exists in table");
        }

        if (this.pageNumberAndPageInformationMap.get(virtualPageNumber) == null) {
            //resolve empty case
            return resolveEmptySlot(virtualPageNumber, virtualAddress);
        } else {
            return resolveTableAccess(virtualPageNumber, virtualAddress);
        }
    }

    private short resolveEmptySlot(int pageNumber, short virtualAddress) throws Exception {
        //still remaining free physical memory pages
        if (this.fifoQueueForReplacement.size() < this.numberOfPhysicalPages) {
            this.insertPageMapEntry(pageNumber);
        } else {
            resolveTableAccessOnPageAbsentWithFifoReference(virtualAddress);
        }
        return this.virtualToPhysicalAddress(virtualAddress);
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

                if (this.pageNumberAndPageInformationMap.get(queriedPageNumber) == null){
                    this.pageNumberAndPageInformationMap.put(queriedPageNumber, new PageInformation(replaced.physicalPageFrameMask, true, true));
                    this.fifoQueueForReplacement.add(List.of(queriedPageNumber, new PageInformation(replaced.physicalPageFrameMask, true, true)));
                    logs.append("Page fault due to empty page frame while accessing page number: ")
                            .append(queriedPageNumber).append(". ")
                            .append("page number ")
                            .append(poppedPageNumber)
                            .append(" with page frame number ")
                            .append(replaced.physicalPageFrameMask)
                            .append(" will be replaced")
                            .append("\n");

                }else{
                    PageInformation queriedPageInformation = this.pageNumberAndPageInformationMap.get(queriedPageNumber);
                    queriedPageInformation.isPresent = true;
                    queriedPageInformation.isReferenced = true;
                    queriedPageInformation.physicalPageFrameMask = replaced.physicalPageFrameMask;
                    logs.append("Page fault while accessing page number: ")
                            .append(queriedPageNumber).append(" ")
                            .append("page number ")
                            .append(poppedPageNumber)
                            .append(" with page frame number ")
                            .append(replaced.physicalPageFrameMask)
                            .append(" will be replaced")
                            .append("\n");
                }
                return;
            } else {
                tmp.isReferenced = false;
                this.fifoQueueForReplacement.add(pop);
            }
        }
    }

    private void insertPageMapEntry(int pageNumber) throws Exception {
        if (this.freePages.isEmpty()) {
            throw new Exception("There are no free pages left");
        }
        int freePage = this.freePages.pop();
        PageInformation pageInformation = new PageInformation(freePage, true, true);
        this.pageNumberAndPageInformationMap.put(pageNumber, pageInformation);
        this.fifoQueueForReplacement.add(List.of(pageNumber, pageInformation));
        this.logs.append("Page fault due to empty page frame number, inserting new page frame number: ")
                .append(freePage).append(" at index ").append(pageNumber).append("\n");
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
