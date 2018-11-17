package concurrency.philos

import spock.lang.Specification
import spock.lang.Timeout

class PhilosophersSpecification extends Specification {

    def "Philosopher has initial parameters"() {
        given:
            def left = new Chopstick(1);
            def right = new Chopstick(2);
            def ph = new Philosopher.Builder().
                leftChopstick (left).rightChopstick(right).build()
        expect: "Philosopher default values after object has been created"
            verifyAll {
                ph.eatingCounter == 0
                ph.attemptsCounter == 0
                ph.unlimited == false
                ph.timeForWaiting == 10
                ph.expectedAttempts == 10
                ph.leftChopstick == left;
                ph.rightChopstick == right;
            }
    }

    def "Philosopher defines cancellation of unlimited activity"() {
        given: "Philosopher with unlimited=true flag"
            def ph = new Philosopher.Builder().
                    leftChopstick (new Chopstick(1)).rightChopstick(new Chopstick(2)).
                    unlimited(true).build()
            def phSpy = Spy(ph)
        when:"Flag is being checked"
            phSpy.isCancelled()
        then: "call of isInterrupted method"
            verifyAll {
                1 * phSpy.isInterrupted()
                0 * phSpy.isLimitReached()
            }
    }

    def "Philosopher defines cancellation of limited activity"() {
        given: "Philosopher with unlimited=false flag"
            def ph = new Philosopher.Builder().
                leftChopstick (new Chopstick(1)).rightChopstick(new Chopstick(2)).
                unlimited(false).build()
            def phSpy = Spy(ph)
        when:"Flag is being checked"
            phSpy.isCancelled()
        then: "call of isLimitReached"
            verifyAll {
                0 * phSpy.isInterrupted()
                1 * phSpy.isLimitReached()
            }
    }

    @Timeout(1)
    def "Philosopher can be interrupted externally"() {
        given: "Philosopher with unlimited attempts of thinking"
            def ph = new Philosopher.Builder().
                    leftChopstick (new Chopstick(1)).rightChopstick(new Chopstick(2)).
                    unlimited(true).build()
        when:"Active Philosopher is interrupted"
            Thread t = new Thread(ph);
            t.start();
            sleep(300);
            t.interrupt()
            t.join()
            //println "${ph} ${t.state}"
        then: "Philosopher stops its activity"
            verifyAll {
                ph.unlimited == true
                ph.attemptsCounter > 0
                t.state == Thread.State.TERMINATED
            }
    }

    def "Philosopher doesn't eat if its left Chopstick is locked by another thread"() {
        given: "Philosopher, left and right Chopsticks"
            def leftChopstick = new Chopstick(1);
            def rightChopstick = new Chopstick(2);
            def ph = new Philosopher.Builder().
                    leftChopstick (leftChopstick).
                    rightChopstick(rightChopstick).build()
        when: "Left Chopstick is acquired by another external thread (thread of current test)"
            leftChopstick.acquire();
            Thread t = new Thread(ph);
            t.start();
            t.join();
        then: "Philosopher does attempts but doesn't eat"
            ph.attemptsCounter == 10;
            ph.eatingCounter == 0;
    }

    def "Philosopher doesn't eat if its right Chopstick is locked by another thread"() {
        given: "Philosopher, left and right Chopsticks"
            def leftChopstick = new Chopstick(1);
            def rightChopstick = new Chopstick(2);
            def ph = new Philosopher.Builder().
                leftChopstick (leftChopstick).
                rightChopstick(rightChopstick).build()
        when: "Right Chopstick is acquired by another external thread (thread of current test)"
            rightChopstick.acquire();
            Thread t = new Thread(ph);
            t.start();
            t.join();
        then: "Philosopher does attempts but doesn't eat"
            ph.attemptsCounter == 10;
            ph.eatingCounter == 0;
    }

    def "Philosopher doesn't eat if its all Chopsticks are locked by another thread"() {
        given: "Philosopher, left and right Chopsticks"
            def leftChopstick = new Chopstick(1);
            def rightChopstick = new Chopstick(2);
            def ph = new Philosopher.Builder().
                leftChopstick (leftChopstick).
                rightChopstick(rightChopstick).build()
        when: "Chopsticks are acquired by another external thread (thread of current test)"
            rightChopstick.acquire();
            leftChopstick.acquire();
            Thread t = new Thread(ph);
            t.start();
            t.join();
        then: "Philosopher does attempts but doesn't eat"
            ph.attemptsCounter == 10;
            ph.eatingCounter == 0;
    }

    def "Each Philosopher tries to make a progress if one Chopstick is locked by neighbor Philosopher"() {
        given: "Philosophers and its left and right Chopsticks"
            def ch1 = new Chopstick(1)
            def ch2 = new Chopstick(2)
            def ch3 = new Chopstick(3)
            def chopsticks = [] << ch1 << ch2 << ch3
            def philosophers = []
            philosophers << new Philosopher.Builder().leftChopstick(ch1).rightChopstick(ch2).build()
            philosophers << new Philosopher.Builder().leftChopstick(ch2).rightChopstick(ch3).build()
            philosophers << new Philosopher.Builder().leftChopstick(ch3).rightChopstick(ch1).build()
        when: "All Chopsticks are acquired by another external thread (it can be current or philosopher thread)"
            chopsticks.each {ch -> ch.acquire()}
            philosophers.collect { ph -> new Thread(ph) }.
                    each { th -> th.start() }.each { th -> th.join() }
        then: "Each Philosopher does attempts but doesn't eat"
            assert philosophers.every { it.attemptsCounter == 10}
            assert philosophers.every { it.eatingCounter == 0 }
            assert philosophers.every { it.eatingCounter <= 10 }
    }

    def "Philosopher makes a progress with unlocked right and left Chopsticks"() {
        given: "Philosopher, left and right Chopsticks"
            def ph = new Philosopher.Builder().
                    leftChopstick(new Chopstick(1)).
                    rightChopstick(new Chopstick(2)).build();
        when: "Chopsticks are not acquired by another external thread (thread of current test)"
            def t = new Thread(ph);
            t.start();
            t.join();
        then: "Philosopher does attempts and eats expected times"
            ph.attemptsCounter == 10;
            ph.eatingCounter == 10;
    }

    def "Philosopher allows another Philosopher make a progress"() {
        given: "Three Philosophers and its left and right Chopsticks"
            def c1 = new Chopstick(1)
            def c2 = new Chopstick(2)
            def c3 = new Chopstick(3)
            def philosophers = []
            philosophers << new Philosopher.Builder().leftChopstick(c1).rightChopstick(c2).timeForWaiting(8).build();
            philosophers << new Philosopher.Builder().leftChopstick(c2).rightChopstick(c3).timeForWaiting(12).build();
            philosophers << new Philosopher.Builder().leftChopstick(c3).rightChopstick(c1).timeForWaiting(17).build();
        when: "Philosophers start its activity simultaneously"
            def threads = philosophers.collect { ph -> new Thread(ph) }
            threads.each { th -> th.start() }.each { th -> th.join() }
        then: "Every Philosopher does attempts and eats some times"
            assert philosophers.every { it.attemptsCounter == 10}
            assert philosophers.every { it.eatingCounter > 0 }
            assert philosophers.every { it.eatingCounter <= 10 }
            //philosophers.each {ph -> println "${ph} ${ph.eatingCounter}"}
    }

    def "Philosopher with non-shared Chopsticks has property eatingCounter = attemptsCounter"() {
        given: "Philosopher with non-shared Chopsticks"
            def c1 = new Chopstick(1)
            def c2 = new Chopstick(2)
            def philosophers = []
            philosophers << new Philosopher.Builder().leftChopstick(c1).rightChopstick(c2).timeForWaiting(8).build();
        when: "Chopsticks are using in one thread (Philosopher) only"
            def threads = philosophers.collect { ph -> new Thread(ph) }
            threads.each { th -> th.start() }.each { th -> th.join() }
        then: "Number of attempts equals expected times"
            assert philosophers.every { it.attemptsCounter == 10}
            assert philosophers.every { it.eatingCounter == 10 }
            //philosophers.each {ph -> println "${ph} ${ph.eatingCounter}"}
    }

}
