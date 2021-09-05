import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConcurrencyTest {

    private EntityLocker<Long> locker;

    private final static Long TIMEOUT_MS = 5_000L;

    @Before
    public void initLocker() {
        this.locker = new EntityLocker<>();
    }

    @Test
    public void oldApi() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try (Lock lock = locker.lock(0L)) {
                try (Lock lock2 = locker.lock(0L)) {
                    Thread.sleep(TIMEOUT_MS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        t1.start();

        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Thread t2 = new Thread(() -> {
            try (Lock lock = locker.lock(0L, 1, TimeUnit.SECONDS)) {
                Assert.fail();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        t2.start();

        t1.join();
        t2.join();
    }

    @Test
    public void newApi() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            locker.run(0L, (id) -> {
                locker.run(0L, (id2) -> {
                    try {
                        Thread.sleep(TIMEOUT_MS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        });

        t1.start();

        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Thread t2 = new Thread(() -> {
            try {
                locker.run(0L, 1, TimeUnit.SECONDS, (id) -> {
                    Assert.fail();
                });
            } catch (TimeoutException | InterruptedException e) {
                e.printStackTrace();
            }
        });


        t2.start();

        t1.join();
        t2.join();
    }
}
