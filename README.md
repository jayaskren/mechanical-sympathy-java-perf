# Lightning Fast Java
Performance Tests to show Mechanical Sympathy principles in Java


To build the tests:
1. Make sure you have Java and Maven installed.
2. Clone the repo
3. cd into the directory
4. Run mvn clean install

## JMH Benchmarks
### To run the Array Vs LinkedList benchmark run:
java -jar target/benchmarks.jar net.askren.mechanicalsympathy.jmh.ArrayVsLinkedList


### To run the Memory Access Patterns test:
java -jar target/benchmarks.jar net.askren.mechanicalsympathy.jmh.TestMemoryAccessPatterns

## Non JMH Benchmarks
### To run the SequentialIOPerformance Benchmark run:
Run java -jar target/java-performance-0.0.1-SNAPSHOT.jar net.askren.mechanicalsympathy.TestSequentialoPerformance

