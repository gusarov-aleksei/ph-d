package concurrency.philos;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Class represents Chopstick used by Philosophers. In other words this is resource shared among threads.
 */
public class Chopstick {

    private int id;
    //mutual exclusion
    private Semaphore semaphore;

    public Chopstick(int id) {
        this.id = id;
        this.semaphore = new Semaphore(1);
    }

    public Chopstick(int id, Semaphore semaphore) {
        this.id = id;
        this.semaphore = semaphore;
    }

    public boolean tryAcquire(long timeout) throws InterruptedException {
        return semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
    }

    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }

    public void release() throws InterruptedException {
        semaphore.release();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chopstick chopstick = (Chopstick) o;

        return id == chopstick.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Chopstick{" +
                "id=" + id +
                '}';
    }
}
