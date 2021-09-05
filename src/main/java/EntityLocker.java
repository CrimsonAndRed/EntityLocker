import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * Locker, that provides synchronization mechanism similar to row-level DB locking
 *
 * @param <ID> row identifier type.
 */
public class EntityLocker<ID> implements LockerModernFashioned<ID>, LockerOldFashioned<ID> {

    // No need for SynchronizedMap since we do several operations per synchronized block anyway.
    private final Map<ID, CountedReentrantLock> ids = new HashMap<>();

    public EntityLocker() {
    }

    @Override
    public Lock lock(ID id) {
        CountedReentrantLock monitor = this.acquireLock(id);
        monitor.lock();
        return new EntityLock(id);
    }

    @Override
    public Lock lock(ID id, int timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        CountedReentrantLock monitor = this.acquireLock(id);
        boolean lockSucceeded = monitor.tryLock(timeout, unit);

        if (!lockSucceeded) {
            synchronized (ids) {
                monitor.dec();
            }
            throw new TimeoutException("Could not acquire lock in a given time frame");
        }
        return new EntityLock(id);
    }

    @Override
    public void run(ID id, Consumer<ID> code) {
        CountedReentrantLock monitor = this.acquireLock(id);
        monitor.lock();
        try {
            code.accept(id);
        } finally {
            this.release(id);
        }
    }

    @Override
    public void run(ID id, int timeout, TimeUnit unit, Consumer<ID> code) throws TimeoutException, InterruptedException {
        CountedReentrantLock monitor = this.acquireLock(id);
        boolean lockSucceeded = monitor.tryLock(timeout, unit);
        if (!lockSucceeded) {
            synchronized (ids) {
                monitor.dec();
            }
            throw new TimeoutException("Could not acquire lock in a given time frame");
        }
        try {
            code.accept(id);
        } finally {
            this.release(id);
        }
    }

    /**
     * Releases a lock for a given entity identifier.
     * If lock is not requested by any other thread then it is removed from locks collection {@link #ids}.
     * @param id entity identifier.
     */
    private void release(ID id) {
        CountedReentrantLock monitor;
        synchronized (ids) {
            monitor = ids.get(id);
            monitor.dec();
            if (monitor.getCount() == 0) {
                ids.remove(id);
            }
        }

        monitor.unlock();
    }

    /**
     * Acquires a lock for a given entity identifier.
     * @param id entity identifier.
     * @return lock.
     */
    private CountedReentrantLock acquireLock(ID id) {
        CountedReentrantLock monitor;
        synchronized (ids) {
            monitor = ids.computeIfAbsent(id, (k) -> new CountedReentrantLock());
            monitor.inc();
        }
        return monitor;
    }

    /**
     * Lock primitive that has to be closed when protected code is finished.
     */
    public class EntityLock implements Lock {

        private final ID id;

        private boolean closed;

        private EntityLock(ID id) {
            this.id = id;
            this.closed = false;
        }

        @Override
        public void close() {
            if (!closed) {
                this.closed = true;
                release(this.id);
            }
        }
    }
}
