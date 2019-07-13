package net.askren.mechanicalsympathy.performance;

import java.util.ArrayList;
import java.util.LinkedList;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.Throughput)
@Fork(value=20)
@Warmup(iterations=20)
@Measurement(iterations = 20)
@State(Scope.Benchmark)
public class ArrayVsLinkedList {
	private static final int NUM_VALUES = 1000000;
	private LinkedList<String> linkedList;
	private ArrayList<String> arrayList;
	private String[] array;
	
	
	@Setup(Level.Trial)
	public void setup() {
		linkedList = new LinkedList<>();
		arrayList = new ArrayList<>();
		array = new String[NUM_VALUES];
		
		for (int i=0; i < NUM_VALUES; i++) {
			linkedList.add("Test_" + i);
			arrayList.add("Test_" + i);
			array[i] = "Test_" + i;
		}
	}
	
	@Benchmark
	public int testArrayList() {
		int val = 0;
		for (String item : arrayList) {
			val += item.hashCode();
		}
		return val;
	}
	
	@Benchmark
	public int testLinkedList() {
		int val = 0;
		for (String item : linkedList) {
			val += item.hashCode();
		}
		return val;
	}
	
	@Benchmark
	public int testArray() {
		int val = 0;
		for (String item : array) {
			val += item.hashCode();
		}
		return val;
	}

}
