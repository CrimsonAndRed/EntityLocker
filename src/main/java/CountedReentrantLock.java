import java.util.concurrent.locks.ReentrantLock;

/**
 * Reentrant lock with counter for amount of accessed threads.
 */
public class CountedReentrantLock extends ReentrantLock {

    private int count = 0;

    public CountedReentrantLock() {
    }

    public CountedReentrantLock(boolean fair) {
        super(fair);
    }

    /**
     * Increments amount of access times.
     */
    public void inc() {
        count++;
    }

    /**
     * Decrements amount of access times.
     */
    public void dec() {
        count--;
    }

    /**
     * Gets amount of access times.
     */
    public int getCount() {
        return count;
    }
}
