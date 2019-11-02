# Lightning Fast Java
Performance Tests to show Mechanical Sympathy principles in Java


To build the tests:
1. Make sure you have Java and Maven installed.
2. Clone the repo
3. cd into the directory
4. Run mvn clean install

## JMH Benchmarks
JMH runs benchmarks over and over again to get good statistical sample.  If you are in a hurry, you may lower the number of Forks to 1.  The different options for running JMH benchmarks [can be found here](https://github.com/guozheng/jmh-tutorial/blob/master/README.md).  Otherwise, I recommend running them over night.

### To run the Array Vs LinkedList benchmark run:
java -jar target/benchmarks.jar net.askren.mechanicalsympathy.jmh.ArrayVsLinkedList


### To run the Memory Access Patterns test:
java -jar target/benchmarks.jar net.askren.mechanicalsympathy.jmh.TestMemoryAccessPatterns

## Non JMH Benchmarks
### To run the SequentialIOPerformance Benchmark run:
Run java -jar target/java-performance-0.0.1-SNAPSHOT.jar net.askren.mechanicalsympathy.TestSequentialoPerformance

