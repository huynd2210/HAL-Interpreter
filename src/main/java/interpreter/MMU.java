package interpreter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MMU {
  //contains pageId -> Page
  public Map<Integer, Page> storage;
  public StringBuilder logger;
  private final int pageSize = 1024;
  private Interpreter interpreter;
  private boolean isRandomReplacement;

  public MMU(int virtualAddressSize, Interpreter interpreter, boolean isRandomReplacement) {
    initMMU(virtualAddressSize);
    this.interpreter = interpreter;
    this.isRandomReplacement = isRandomReplacement;
    this.logger = new StringBuilder();
  }

  private void initMMU(int virtualAddressSize) {
    this.storage = new HashMap<>();
    final int numberOfPages = virtualAddressSize / pageSize; //65536 / 1024 = 64
    for (int i = 0; i < numberOfPages; i++) {
      this.storage.put(i, new Page(i));
    }
  }

  public void resolveStore(int virtualAddress, double value) throws Exception {
    //floor division

    int pageId = virtualAddress / pageSize;
    //page is present
    for (int i = 0; i < interpreter.pages.size(); i++) {
      if (interpreter.pages.get(i).pageId == pageId) {
        interpreter.pages.get(i).isReferenced = true;
        interpreter.pages.get(i).store(virtualAddress, value);
        return;
      }
    }
    int pageIdToStore;
    //page is not present
    if (this.isRandomReplacement){
      pageIdToStore = replacePageRandomly(virtualAddress);
    }else{
      pageIdToStore = replacePageWithFifoReferencedBit(virtualAddress);
    }

    interpreter.pages.stream().filter(page -> page.pageId == pageIdToStore).findFirst().get().store(virtualAddress, value);
//    interpreter.pages.get(0).store(virtualAddress, value);
//    interpreter.pages.get(pageToStore).store(virtualAddress, value);
  }

  public Double resolveLoad(int virtualAddress) throws Exception {
    //floor division
    int pageId = virtualAddress / pageSize;
    //page is present
    for (int i = 0; i < interpreter.pages.size(); i++) {
      if (interpreter.pages.get(i).pageId == pageId) {
        interpreter.pages.get(i).isReferenced = true;
        return interpreter.pages.get(i).load(virtualAddress);
      }
    }
    int pageIdToGet;
    //page is not present
    if (this.isRandomReplacement){
      pageIdToGet = replacePageRandomly(virtualAddress);
    }else{
      pageIdToGet = replacePageWithFifoReferencedBit(virtualAddress);
    }


    return interpreter.pages.stream().filter(page -> page.pageId == pageIdToGet).findFirst().get().load(virtualAddress);
    //    return interpreter.pages.get(0).load(virtualAddress);

  }

  private int replacePageWithFifoReferencedBit(int virtualAddress) throws Exception {
//    Thread.sleep(50);
    int pageId = virtualAddress / pageSize;
    if (!this.isPageActive(pageId) && this.interpreter.pages.size() < 4) {
      return insertPage(pageId);
    }
    //find and replace page without referenced bit
    while (true) {
      if (!interpreter.pages.get(0).isReferenced) {
        //replace page
        Page replacing = this.storage.get(pageId);
        replacing.isReferenced = true;
        this.storage.put(interpreter.pages.get(0).pageId, interpreter.pages.get(0));
        this.logger.append("Page fault at page: " + interpreter.pages.get(0).pageId + " replacing with page: " + replacing.pageId + "\n");

        interpreter.pages.add(replacing);
        interpreter.pages.remove(0);
        return replacing.pageId;
      } else {
        //set referenced bit to false and push to last position
        interpreter.pages.get(0).isReferenced = false;
        Collections.rotate(interpreter.pages, -1);
      }
    }
  }

  private int replacePageRandomly(int virtualAddress) throws Exception {
//    Thread.sleep(50);
    int pageId = virtualAddress / pageSize;
    if (!this.isPageActive(pageId) && this.interpreter.pages.size() < 4) {
      return insertPage(pageId);
    }
//    Collections.shuffle(this.interpreter.pages);
//    Page replaced = this.interpreter.pages.get(0);

    //random int between 0 and 3
    int randomIndex = (int) (Math.random() * 4);
    Page replaced = this.interpreter.pages.get(randomIndex);

//    this.interpreter.pages.remove(0);
    this.interpreter.pages.remove(randomIndex);
    Page replacing = this.storage.get(pageId);
    String log = "Page fault at page: " + replaced.pageId + " replacing with page: " + replacing.pageId + "\n";
    this.logger.append(log);
    this.interpreter.pages.add(replacing);
    return replacing.pageId;
  }

  private int insertPage(int pageId) throws Exception {
    if (this.interpreter.pages.size() < this.interpreter.maxPages) {
      this.interpreter.pages.add(this.storage.get(pageId));
      this.storage.get(pageId).isReferenced = true;
//      this.logger.append("Page fault while accessing: " + pageId + " due to initialization, loading page into register \n ");
      return pageId;
    }
    throw new Exception("Should not have happened, insert page while max pages reached");
  }

  private boolean isPageActive(int pageId) {
    for (int i = 0; i < interpreter.pages.size(); i++) {
      if (interpreter.pages.get(i).pageId == pageId) {
        return true;
      }
    }
    return false;
  }

  public void printLog(){
    System.out.println(this.logger.toString());
  }
}

