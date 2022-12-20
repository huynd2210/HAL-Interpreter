package interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//pageId Represent
public class Page {
  public Integer pageId;
  public Map<Integer, Double> virtualAddressRegisters;
  public boolean isReferenced;
  public final Integer pageSize = 1024;

  public Page(Integer pageId){
    this.pageId = pageId;
    this.virtualAddressRegisters = new HashMap<>();
    this.initPageRegisters();
  }

  private void initPageRegisters(){
    int offset = pageId * 1024;
    for (int i = offset; i < offset + pageSize; i++) {
      virtualAddressRegisters.put(i, 0d);
    }
  }

  //zero-based index
  public Double get(int physicalAddress){
    int offset = pageId * 1024;
    return virtualAddressRegisters.get(offset + physicalAddress);
  }

  public void store(int virtualAddress, double value){
    virtualAddressRegisters.computeIfPresent(virtualAddress, (k, v) -> value);
  }

  public double load(int virtualAddress){
    if (virtualAddressRegisters.get(virtualAddress) == null){
      System.out.println("sdsd");
    }
    return virtualAddressRegisters.get(virtualAddress);
  }

  public void dumpPage(){
    for (int i = 0; i < pageSize; i++) {
      System.out.println("Page number: " + pageId + " register number: " + i + " value: " + get(i));
    }
  }

  public int getPageId() {
    return pageId;
  }

  @Override
  public String toString() {
    return "Page{" +
            "pageId=" + pageId +
            ", virtualAddressRegisters=" + virtualAddressRegisters +
            '}';
  }
}
