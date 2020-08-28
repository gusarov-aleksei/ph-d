# Philosophers dinner party. Practicing in concurrent algorithms design.

Dining philosophers problem is classic problem of computer science initially formulated by E. Dijkstra, T. Hoare
([wiki details](https://en.wikipedia.org/wiki/Dining_philosophers_problem)). It shows shared resource utilization in concurrent environment. 
Another yet implementation of concurrency control is done with **Semaphore** of **Java Concurrency API** (see [`Chopstick`](./src/main/java/concurrency/philos/Chopstick.java) class used by [`Philosopher`](./src/main/java/concurrency/philos/Philosopher.java) class). 

Unit-tests are written in **Groovy** with **Spock** library. They contain examples of concurrent utilization modeled with **Java Thread API**.