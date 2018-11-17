package concurrency.philos

import spock.lang.Specification

class PhilosophersPartySpecification extends Specification {

    def party = new PhilosophersParty();

    def "PhilosophersParty creates Chopsticks"() {
        given: "Chopstick creation method is called"
            def chopsticks = party.createChopsticks(3)
            def expectedChopsticks = [] << new Chopstick(0) << new Chopstick(1) <<  new Chopstick(2)
        expect: "List of Chopsticks is created"
            chopsticks == expectedChopsticks
    }

    def "PhilosophersParty creates Philosophers"() {
        given: "Philosopher creation method is called with passed Chopsticks"
            def chopsticks = party.createChopsticks(5)
            def philosophers = party.createPhilosophers(chopsticks)
        expect: "List of Philosophers is created with the same size"
            verifyAll {
                philosophers.size() == 5
                philosophers.size() == chopsticks.size()
            }
    }

    def "PhilosophersParty arranges Philosophers and Chopsticks "() {
        given: "Philosopher creation method is called with passed Chopsticks"
            def chopsticks = party.createChopsticks(3)
            List<Philosopher> philosophers = party.createPhilosophers(chopsticks)
        expect: "Right Chopstick of current Philosopher is left Chopstick of the next Philosopher"
            verifyAll {
                philosophers.first().getRightChopstick() == philosophers.get(1).getLeftChopstick()  //first philosopher
                philosophers.get(1).getRightChopstick() == philosophers.get(2).getLeftChopstick()   //second philosopher
                philosophers.last().getRightChopstick() == philosophers.first().getLeftChopstick()  //third philosopher
            }
    }

    def "PhilosophersParty with only one Philosopher is impossible"() {
        when: "PhilosophersParty with 1 Philosopher creation is called"
            party.initParty(1)
        then: "IllegalArgumentException is thrown"
            thrown(IllegalArgumentException)
    }

    def "PhilosophersParty with several(not one) Philosophers is possible"() {
        when: "PhilosophersParty with 2 Philosophers creation is called"
            party.initParty(2)
        then: "IllegalArgumentException isn't thrown"
            notThrown(IllegalArgumentException)
    }

    def "It creates and starts thread for Philosopher"() {
        when:
            def ph = new Philosopher.Builder().
                    leftChopstick ( new Chopstick(1)).rightChopstick(new Chopstick(2)).
                    timeForWaiting(1).expectedAttempts(1).build()
            def state = party.createAndStart(ph).state
        then: "Thread State isn't NEW because it is started"
            state != Thread.State.NEW
    }

    def "It creates and starts threads for every Philosopher"() {
        when: "PhilosophersParty creation is called"
            def philosophers = [] << new Philosopher.Builder().
                    leftChopstick ( new Chopstick(1)).rightChopstick(new Chopstick(2)).
                    timeForWaiting(1).expectedAttempts(1).build()
            List<Thread> threads = party.performParty(philosophers)
        then: "Threads are created and has no NEW(after starting) state"
            threads.size() == philosophers.size()
            assert threads.every { it.state != Thread.State.NEW}
    }

}