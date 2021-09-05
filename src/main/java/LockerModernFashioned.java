import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * Modern API style for critical section execution, protected by entity identifier.
 * @param <ID> identifier type.
 */
public interface LockerModernFashioned<ID> {

    /**
     * Executes protected code for a given entity id.
     * If given id is already locked, then current thread would wait until lock is released.
     *
     * @param id entity identifier.
     * @param code critical section for given entity identifier, gets id as an argument.
     */
    void run(ID id, Consumer<ID> code);

    /**
     * Executes protected code for a given id with a timeout.
     * If given id is already locked, then current thread would wait given amount of time until lock is released.
     * If timeout is reached and lock is not acquired then {@link TimeoutException} is raised.
     *
     * @param id entity identifier.
     * @param timeout the time to wait for the lock.
     * @param unit the time unit of the timeout argument.
     * @param code critical section for given entity identifier.
     * @throws TimeoutException if timeout is reached and lock is not acquired.
     * @throws InterruptedException  if the current thread is interrupted.
     */
    void run(ID id, int timeout, TimeUnit unit, Consumer<ID> code) throws TimeoutException, InterruptedException;
}
