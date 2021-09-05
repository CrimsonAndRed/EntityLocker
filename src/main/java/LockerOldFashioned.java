import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * "Oldschool" API for critical section execution, protected by entity identifier.
 * @param <ID> identifier type.
 */
public interface LockerOldFashioned<ID> {

    /**
     * Acquires the lock for a given id.
     * If given id is already locked, then current thread would wait until lock is released.
     *
     * Special Lock object is returned. It is mandatory to call {@link Lock#close} manually or using this method in try-with-resources block.
     * @param id entity identifier.
     */
    Lock lock(ID id);

    /**
     * Acquires the lock for a given id with a timeout.
     * If given id is already locked, then current thread would wait given amount of time until lock is released.
     * If timeout is reached and lock is not acquired then {@link TimeoutException} is raised.
     *
     * Special Lock object is returned. It is mandatory to call {@link Lock#close} manually or using this method in try-with-resources block.
     * @param id entity identifier.
     * @param timeout the time to wait for the lock.
     * @param unit the time unit of the timeout argument.
     * @throws TimeoutException if timeout is reached and lock is not acquired.
     * @throws InterruptedException  if the current thread is interrupted.
     */
    Lock lock(ID id, int timeout, TimeUnit unit) throws TimeoutException, InterruptedException;
}
