package concurrency.philos;

import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * It creates Philosophers and Chopsticks, arrange them and starts party(performing threads).
 */
public class PhilosophersParty {

    protected List<Chopstick> createChopsticks(final int amountOfChopsticks) {
        return IntStream.range(0, amountOfChopsticks)
                .mapToObj(i -> new Chopstick(i))
                .collect(Collectors.toList());
    }

    protected List<Philosopher> createPhilosophers(final List<Chopstick> chopstickList) {
        //create Philosopher with related Chopstick shared among Philosophers
        IntFunction<Philosopher> createPhilosopher = (id) ->
            new Philosopher.Builder().
                    leftChopstick(chopstickList.get(id)).
                    rightChopstick(chopstickList.get((id + 1) % chopstickList.size())).build();
        return IntStream.range(0, chopstickList.size())
                .mapToObj(createPhilosopher)
                .collect(Collectors.toList());
    }

    public List<Philosopher> initParty(final int amountOfPhilosophers) {
        if (amountOfPhilosophers < 2) {
            throw new IllegalArgumentException("amountOfPhilosophers must be more than 1");
        }
        return createPhilosophers(createChopsticks(amountOfPhilosophers));
    }

    public List performParty(List<Philosopher> philosophers) {
        List<Thread> threads = philosophers.stream().
                map(this::createAndStart).
                collect(Collectors.toList());
        threads.forEach(this::joinUnchecked);
        return threads;
    }

    protected Thread createAndStart(Philosopher ph) {
        Thread th = new Thread(ph);
        th.start();
        return th;
    }

    protected void joinUnchecked(Thread th) {
        try {
            th.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
