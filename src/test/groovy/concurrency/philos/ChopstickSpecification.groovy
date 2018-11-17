package concurrency.philos

import spock.lang.Specification

import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class ChopstickSpecification extends Specification {

    def "Chopstick can be acquired by some thread (current thread of test)"() {
        given:
            def c = new Chopstick(1)
        expect:
            c.tryAcquire(1) == true
        cleanup:
            c.release()
    }

    def "Chopstick can't be acquired twice without release"() {
        given:
            def c = new Chopstick(1)
        when:
            c.tryAcquire(1)
        then:
            c.tryAcquire(1) == false
        cleanup:
            c.release()
    }

    def "Acquired Chopstick can be acquired only after its release happened"() {
        given:
            def c = new Chopstick(1)
        when:
            c.tryAcquire(1)
        and:
            c.release()
        then:
            c.tryAcquire(1)
        cleanup:
            c.release()
    }

    def "Chopstick delegates tryAcquire and release call to the semaphore"() {
        given:
            def timeout = 1
            def semaphore = Mock(Semaphore)
            def chopstick = new Chopstick(timeout,semaphore)
        when:
            chopstick.tryAcquire timeout
        and:
            chopstick.release()
        then: "call is delegated to semaphore"
            1 * semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)
        and: "call is delegated to semaphore"
            1 * semaphore.release()
    }

    def "Chopstick hash code function is based on id"() {
        expect:
            chopstick.hashCode() == hashCode
        where:
            chopstick               | hashCode
            new Chopstick(5)    | 5
            new Chopstick(15)   | 15
    }

    def "Chopstick has toString representation"() {
        expect:
            chopstick.toString() == str
        where:
            chopstick            | str
            new Chopstick(1) | "Chopstick{id=1}"
            new Chopstick(10)| "Chopstick{id=10}"
    }



}
