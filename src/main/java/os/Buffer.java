package os;

public class Buffer {
    private boolean available = false;
    private double data;

    public synchronized void put(double x) {
        while (available) {
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }
        data = x;
        available = true;
        notifyAll();
    }

    public synchronized double get(){
        while(!available){
            try{
                wait();
            }catch (InterruptedException ignored){}
        }
        available = false;
        notifyAll();
        return data;
    }
}
