package concurrency.philos

import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PhilosopherBuilderSpecification extends Specification {

    def "PhilosopherBuilder instantiates Philosopher object"() {
        given: "Created Philosopher"
            def leftChopstick = new Chopstick(1)
            def rightChopstick = new Chopstick(2)
            def ph = new Philosopher.Builder().
                    leftChopstick(leftChopstick).
                    rightChopstick(rightChopstick).
                    expectedAttempts(20).
                    timeForWaiting(20).
                    unlimited(true).
                    build();
        expect: "Its parameters are initialized properly"
            verifyAll {
                ph.leftChopstick == leftChopstick
                ph.rightChopstick == rightChopstick
                ph.expectedAttempts == 20
                ph.timeForWaiting == 20
                ph.attemptsCounter == 0
                ph.eatingCounter == 0
                ph.unlimited == true
            }
    }

    def "PhilosopherBuilder doesn't create Philosopher object without left Chopstick"() {
        when: "Philosopher is being created"
            new Philosopher.Builder().rightChopstick(new Chopstick(1)).build()
        then: "Exception is expected"
            def e = thrown(IllegalArgumentException)
            e.getMessage() == "Left Chopstick can't be null"
    }

    def "PhilosopherBuilder doesn't create Philosopher object without right Chopstick"() {
        when: "Philosopher is being created"
            new Philosopher.Builder().leftChopstick(new Chopstick(1)).build()
        then: "Exception is expected"
            def e = thrown(IllegalArgumentException)
            e.getMessage() == "Right Chopstick can't be null"
    }

    def "PhilosopherBuilder doesn't create Philosopher object with the same right and left Chopstick"() {
        when: "Philosopher is being created"
            def left = new Chopstick(1)
            new Philosopher.Builder().
                leftChopstick(left).rightChopstick(left).build()
        then: "Builder initializes Philosopher's parameters"
            def e = thrown(IllegalArgumentException)
            e.getMessage() == "Right and Left Chopsticks are expected to be different"
    }

    def "PhilosopherBuilder generates unique id"() {
        when: "Builder call generateId in parallel way"
            def builder = new Philosopher.Builder()
            def idBefore = builder.generateId();
            def executor = Executors.newFixedThreadPool(5)
            def task = { builder.generateId()} as Runnable
            100000.times { executor.execute(task) }
            executor.shutdown()
            executor.awaitTermination(2,TimeUnit.SECONDS)
        then: "It generates correct value according to sequence"
            builder.generateId() == 100001 + idBefore
    }

}
