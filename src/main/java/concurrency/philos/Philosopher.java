package concurrency.philos;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * Class represents Philosopher participating in party and able to think and eat. Every Philosopher has a shared Chopsticks which can be held by another Philosophers.
 * Philosopher try to acquire left and right chopsticks. If them are free(unlocked) Philosopher acquires(locks) them and eats its food and releases after eat.
 * Philosopher decides don't acquire chopstick after waiting if left or right chopstick is already still held by another participant.
 * Philosopher can be unlimited: he does its activity until he is interrupted externally.
 * limited: he does its activity expectedAttempts times and stops.
 *
 * All that traits are covered in test
 */
public class Philosopher implements Runnable {

    private int id;
    private Chopstick leftChopstick;//shared resource
    private Chopstick rightChopstick;//shared resource
    private int timeForWaiting=10;//milliseconds
    private int expectedAttempts=10;
    private int attemptsCounter=0;//TODO rename to thinkingCounter
    private int eatingCounter=0;//successful attempts
    private boolean unlimited=false;
    // true - Philosopher stops performing when thread is interrupted externally
    // false - Philosopher stops performing when attemptsCounter = expectedAttempts

    public static class Builder {
        private Chopstick leftChopstick;
        private Chopstick rightChopstick;
        private int timeForWaiting=10;
        private int expectedAttempts=10;
        private boolean unlimited=false;
        private static AtomicInteger idGenerator = new AtomicInteger();

        public Builder() {
        }

        public Builder leftChopstick(Chopstick leftChopstick) {
            this.leftChopstick = leftChopstick;
            return this;
        }

        public Builder rightChopstick(Chopstick rightChopstick) {
            this.rightChopstick = rightChopstick;
            return this;
        }

        public Builder timeForWaiting(int timeForWaiting) {
            this.timeForWaiting = timeForWaiting;
            return this;
        }

        public Builder expectedAttempts(int expectedAttempts) {
            this.expectedAttempts = expectedAttempts;
            return this;
        }

        public Builder unlimited(boolean unlimited) {
            this.unlimited = unlimited;
            return this;
        }

        protected int generateId() {
            return idGenerator.incrementAndGet();
        }

        public Philosopher build() {
            if (leftChopstick == null) {
                throw new IllegalArgumentException("Left Chopstick can't be null");
            }
            if (rightChopstick == null) {
                throw new IllegalArgumentException("Right Chopstick can't be null");
            }
            if (leftChopstick.equals(rightChopstick)) {
                throw new IllegalArgumentException("Right and Left Chopsticks are expected to be different");
            }
            Philosopher ph = new Philosopher();
            ph.id = generateId();
            ph.leftChopstick = this.leftChopstick;
            ph.rightChopstick = this.rightChopstick;
            ph.timeForWaiting = this.timeForWaiting;
            ph.expectedAttempts = this.expectedAttempts;
            ph.unlimited = this.unlimited;
            return ph;
        }
    }

    private Philosopher() {}

    protected void think() throws InterruptedException {
         sleep(timeForWaiting);//simulate thinking
    }

    private void eat() throws InterruptedException {
        eatingCounter++;
        //System.out.println(this + " eat "+ eatingCounter);
        sleep(timeForWaiting);//simulate eating
    }


    private void doPerformance() throws InterruptedException {
        while (!isCancelled()) {
            think();
            tryEat(leftChopstick, rightChopstick);
            attemptsCounter++;
        }
    }

    protected int getEatingCounter() {
        return eatingCounter;
    }
    
    protected int getAttemptsCounter() {
        return attemptsCounter;
    }

    protected int getTimeForWaiting() {
        return timeForWaiting;
    }

    protected int getExpectedAttempts() {
        return expectedAttempts;
    }

    private void tryEat(Chopstick leftChopstick, Chopstick rightChopstick) throws InterruptedException  {
        //System.out.println(this + " started trying to eat");
        boolean leftAcquired = false;
        boolean rightAcquired = false;
        try {
            leftAcquired = leftChopstick.tryAcquire(timeForWaiting);
            if (leftAcquired) {
                //System.out.println(this + " " + leftChopstick +" is acquired");
                rightAcquired = rightChopstick.tryAcquire(timeForWaiting);
                if (rightAcquired) {
                    //System.out.println(this + " " + rightChopstick +" is acquired");
                    eat();
                } else {
                    //System.out.println(this + " " + rightChopstick +" isn't acquired");
                }
            } else {
                //System.out.println(this + " " + leftChopstick +" isn't acquired");
            }
        } finally {
            if (leftAcquired) {
                //System.out.println(this + " release " + leftChopstick );
                leftChopstick.release();
            }
            if (rightAcquired) {
                //System.out.println(this + " release " + rightChopstick);
                rightChopstick.release();
            }
        }
    }

    /*private void tryToEatDeadlock(Chopstick leftChopstick, Chopstick rightChopstick) throws InterruptedException  {
        System.out.println(this + " started trying to eat");
        boolean leftAcquired = false;
        boolean rightAcquired = false;
        try {
            leftChopstick.acquire();
            System.out.println(this + " " + leftChopstick +" is acquired");
            rightChopstick.acquire();
            System.out.println(this + " " + rightChopstick +" is acquired");
            eat();
        } finally {
            leftChopstick.release();
            rightChopstick.release();
            System.out.println(this + " release chopsticks");
        }
    }*/


    protected boolean isCancelled() {
        return unlimited ? isInterrupted() : isLimitReached();
    }

    protected boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    protected boolean isLimitReached() {
        return attemptsCounter >= expectedAttempts;
    }

    protected Chopstick getLeftChopstick() {
        return leftChopstick;
    }

    protected Chopstick getRightChopstick() {
        return rightChopstick;
    }

    protected boolean isUnlimited() {
        return unlimited;
    }

    @Override
    public void run() {
        try {
            doPerformance();
        } catch (InterruptedException e) {
            //if thread was in Object.wait, Thread.sleep, or Thread.join
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return /*LocalTime.now() + " " +Thread.currentThread().generateId() +*/ " Philosopher{" +
                "id=" + id +
                '}';
    }

}
