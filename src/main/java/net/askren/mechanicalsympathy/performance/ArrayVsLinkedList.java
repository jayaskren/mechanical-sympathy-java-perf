package net.askren.mechanicalsympathy.performance;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.SingleShotTime)
@Fork(value=1)
@Warmup(iterations=20)
@Measurement(iterations = 20)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ArrayVsLinkedList {
	private static final int NUM_VALUES = 10000000;
	private LinkedList<String> linkedList;
	private ArrayList<String> arrayList;
	private String[] array;
	private int[] intArray;
	private Integer[] integerArray;
	
	@Setup(Level.Trial)
	public void setup() {
		linkedList = new LinkedList<>();
		arrayList = new ArrayList<>();
		array = new String[NUM_VALUES];
		intArray = new int[NUM_VALUES];
		integerArray = new Integer[NUM_VALUES];
		
		for (int i=0; i < NUM_VALUES; i++) {
			linkedList.add(Integer.toString(i%10));
			arrayList.add(Integer.toString(i%10));
			array[i] = Integer.toString(i%10);
			intArray[i]=i%10;
			integerArray[i] = i%10;
		}
	}
	
	@Benchmark
	public int testStringArrayList() {
		int val = 0;
		for (String item : arrayList) {
			val += item.charAt(0);
		}
		return val;
	}
	
	@Benchmark
	public int testStringLinkedList() {
		int val = 0;
		for (String item : linkedList) {
			val += item.charAt(0);
		}
		return val;
	}
	
	@Benchmark
	public int testStringArray() {
		int val = 0;
		for (String item : array) {
			val += item.charAt(0);
		}
		return val;
	}
	
	@Benchmark
	public int testIntArray() {
		int val = 0;
		for (int item : intArray) {
			val += item;
		}
		return val;
	}
	
	@Benchmark
	public int testIntegerArray() {
		int val = 0;
		for (Integer item : integerArray) {
			val += item.intValue();
		}
		return val;
	}

}
