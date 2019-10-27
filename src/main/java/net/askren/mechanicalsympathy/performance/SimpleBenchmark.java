package net.askren.mechanicalsympathy.performance;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;


@BenchmarkMode(Mode.SingleShotTime)
@Fork(value=1)
@Warmup(iterations=20)
@Measurement(iterations = 20)
@State(Scope.Benchmark)
public class SimpleBenchmark {

	static final int size = 200000000;
	
//	public static void main(String[] args) {
//		sumValues(size);
//		long begin = System.currentTimeMillis();
//		sumValues(size);
//		System.out.println(System.currentTimeMillis() - begin);
//		
//		
//		createValues(size) ;
//		begin = System.currentTimeMillis();
//		createValues(size);
//		System.out.println(System.currentTimeMillis() - begin);
//	}
	
	@Benchmark
	public static void sumValues() {
		int[] values = createValues();
		int result = 0;
		for (int value : values) {
			result += value;
		}
	}

	@Benchmark
	public static int[] createValues() {
		int[] values = new int[size];
		for (int i = 0; i < values.length; i++) {
			values[i] = i;
		}
		return values;
	}
}
