package interpreter;

import java.util.*;

public class PageTable {
    public Map<Integer, PageInformation> pageNumberAndPageInformationMap;
    public Deque<Integer> freePages;
    public Deque<List<Object>> fifoQueueForReplacement;
    public StringBuilder logs;
    public Interpreter interpreter;
    private final int numberOfPhysicalPages = 4;

    public PageTable(Interpreter interpreter) {
        this.interpreter = interpreter;
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
    public int resolveQuery(int virtualAddress, boolean isRandomReplacement) throws Exception {
        int virtualPageNumber = this.getPageNumber(virtualAddress);
        if (!this.pageNumberAndPageInformationMap.containsKey(virtualPageNumber)) {
            throw new Exception("Page Number: " + virtualPageNumber + " doesnt exists in table");
        }
        if (this.pageNumberAndPageInformationMap.get(virtualPageNumber) == null) {
            //resolve empty case
            return resolveEmptySlot(virtualPageNumber, virtualAddress, isRandomReplacement);
        } else {
            return resolveTableAccess(virtualPageNumber, virtualAddress, isRandomReplacement);
        }
    }

    private int resolveEmptySlot(int pageNumber, int virtualAddress, boolean isRandomReplacement) throws Exception {
        //still remaining free physical memory pages
        if (this.fifoQueueForReplacement.size() < this.numberOfPhysicalPages) {
            this.insertPageMapEntry(pageNumber);
        } else {

            if (isRandomReplacement) {
                resolveTableAccessOnPageAbsentWithRandomness(virtualAddress);
            } else {
                resolveTableAccessOnPageAbsentWithFifoReference(virtualAddress);
            }
        }
        return this.virtualToPhysicalAddress(virtualAddress);
    }

    private int resolveTableAccess(int virtualPageNumber, int virtualAddress, boolean isRandomReplacement) {
        PageInformation pageInformation = this.pageNumberAndPageInformationMap.get(virtualPageNumber);
        if (pageInformation.isPresent) {
            pageInformation.isReferenced = true;
        } else {
            if (isRandomReplacement) {
                resolveTableAccessOnPageAbsentWithRandomness(virtualAddress);
            } else {
                resolveTableAccessOnPageAbsentWithFifoReference(virtualAddress);
            }
        }
        return this.virtualToPhysicalAddress(virtualAddress);
    }

    private void resolveTableAccessOnPageAbsentWithRandomness(int virtualAddress) {
        //resolve seitenersetzung random
        List<Integer> activePageNumber = new ArrayList<>();
        for (Map.Entry<Integer, PageInformation> entry : this.pageNumberAndPageInformationMap.entrySet()) {
            if (entry.getValue() != null) {
                activePageNumber.add(entry.getKey());
            }
        }

        Collections.shuffle(activePageNumber);
        int pageNumberToBeReplaced = activePageNumber.get(0);
        PageInformation replaced = this.pageNumberAndPageInformationMap.get(pageNumberToBeReplaced);
        replaced.isPresent = false;
        for (int i = replaced.physicalPageFrameMask; i < 1000 + replaced.physicalPageFrameMask; i++) {
            replaced.data.set(i, this.interpreter.register.get(i));
        }
        int queriedPageNumber = this.getPageNumber(virtualAddress);

        if (this.pageNumberAndPageInformationMap.get(queriedPageNumber) == null) {
            PageInformation pageNew = new PageInformation(replaced.physicalPageFrameMask, true, true, true);

            if (this.pageNumberAndPageInformationMap.get(queriedPageNumber) != null){
                pageNew.data = this.pageNumberAndPageInformationMap.get(queriedPageNumber).data;
            }

            this.pageNumberAndPageInformationMap.put(queriedPageNumber, pageNew);

//            this.pageNumberAndPageInformationMap.put(queriedPageNumber, new PageInformation(replaced.physicalPageFrameMask, true, true, true));
            logs.append("Page fault due to empty page frame while accessing page number: ")
                    .append(queriedPageNumber).append(". ")
                    .append("page number ")
                    .append(pageNumberToBeReplaced)
                    .append(" with page frame number ")
                    .append(replaced.physicalPageFrameMask)
                    .append(" will be replaced")
                    .append("\n");

        } else {
            PageInformation queriedPageInformation = this.pageNumberAndPageInformationMap.get(queriedPageNumber);
            queriedPageInformation.isPresent = true;
            queriedPageInformation.isReferenced = true;
            queriedPageInformation.physicalPageFrameMask = replaced.physicalPageFrameMask;
            logs.append("Page fault while accessing page number: ")
                    .append(queriedPageNumber).append(" ")
                    .append("page number ")
                    .append(pageNumberToBeReplaced)
                    .append(" with page frame number ")
                    .append(replaced.physicalPageFrameMask)
                    .append(" will be replaced")
                    .append("\n");
        }
    }

    private int calculateEndRange(int physicalPageFrameMask) {
        return (physicalPageFrameMask * 2) - 1;
    }

    private void resolveTableAccessOnPageAbsentWithFifoReference(int virtualAddress) {
        //resolve seitenersetzung
        while (true) {
            List<Object> pop = this.fifoQueueForReplacement.pop();
            PageInformation tmp = (PageInformation) pop.get(1);
            if (!tmp.isReferenced) {
                //not referenced, therefore it is picked to be replaced i.e find it in the map and set present bit to false.
                int poppedPageNumber = (int) pop.get(0);
                PageInformation replaced = this.pageNumberAndPageInformationMap.get(poppedPageNumber);
                replaced.isPresent = false;
                for (int i = replaced.physicalPageFrameMask; i <= 1024 + replaced.physicalPageFrameMask; i++) {
//                    replaced.data.set(i, this.interpreter.register.get(i));
                    replaced.data.add(this.interpreter.register.get(i));
                }
                int queriedPageNumber = this.getPageNumber(virtualAddress);

                if (this.pageNumberAndPageInformationMap.get(queriedPageNumber) == null) {
                    PageInformation pageNew = new PageInformation(replaced.physicalPageFrameMask, true, true, true);

                    if (this.pageNumberAndPageInformationMap.get(queriedPageNumber) != null){
                        pageNew.data = this.pageNumberAndPageInformationMap.get(queriedPageNumber).data;
                    }

                    this.pageNumberAndPageInformationMap.put(queriedPageNumber, pageNew);
//                    this.pageNumberAndPageInformationMap.put(queriedPageNumber, new PageInformation(replaced.physicalPageFrameMask, true, true, true));
                    this.fifoQueueForReplacement.add(List.of(queriedPageNumber, pageNew));
                    logs.append("Page fault due to empty page frame while accessing page number: ")
                            .append(queriedPageNumber).append(". ")
                            .append("page number ")
                            .append(poppedPageNumber)
                            .append(" with page frame number ")
                            .append(replaced.physicalPageFrameMask)
                            .append(" will be replaced")
                            .append("\n");
                } else {
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

    public int getPageNumber(int virtualAddress) {
        int virtualPageNumberMask = 64512;
        int maskedAddress = virtualAddress & virtualPageNumberMask;
        return (maskedAddress >> 10);
    }

    public int virtualToPhysicalAddress(int virtualAddress) {
        //first 6bit, note: 6bits for the entirety of virtual address, but physical address wont fully use 6bits during translation
        int pageNumber = getPageNumber(virtualAddress);
        int offsetMask = 1023;
        int maskedOffset = virtualAddress & offsetMask; //10 offset bits
        PageInformation pageInformation = this.pageNumberAndPageInformationMap.get(pageNumber);

        int physicalAddress = pageInformation.physicalPageFrameMask | maskedOffset;
        return physicalAddress;
    }
}
