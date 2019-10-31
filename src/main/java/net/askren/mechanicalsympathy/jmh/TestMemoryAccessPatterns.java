package net.askren.mechanicalsympathy.jmh;

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

@BenchmarkMode(Mode.Throughput)
@Fork(value=20)
@Warmup(iterations=20)
@Measurement(iterations = 20)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class TestMemoryAccessPatterns {

	private static final int LONG_SIZE = 8;
	private static final int PAGE_SIZE = 2 * 1024 * 1024;
	private static final int ONE_GIG = 1024 * 1024 * 1024;
	private static final long TWO_GIG = 2L * ONE_GIG;

	private static final int ARRAY_SIZE = (int) (TWO_GIG / LONG_SIZE);
	private static final int WORDS_PER_PAGE = PAGE_SIZE / LONG_SIZE;

	private static final int ARRAY_MASK = ARRAY_SIZE - 1;
	private static final int PAGE_MASK = WORDS_PER_PAGE - 1;

	private static final int PRIME_INC = 514229;

	private static final long[] memory = new long[ARRAY_SIZE];
	
	public enum StrideType {
		LINEAR_WALK {
			@Override
			public int next(final int pageOffset, final int wordOffset,
					final int pos) {
				return (pos + 1) & ARRAY_MASK;
			}
		},

		RANDOM_PAGE_WALK {
			@Override
			public int next(final int pageOffset, final int wordOffset,
					final int pos) {
				return pageOffset + ((pos + PRIME_INC) & PAGE_MASK);
			}
		},

		RANDOM_HEAP_WALK {
			@Override
			public int next(final int pageOffset, final int wordOffset,
					final int pos) {
				return (pos + PRIME_INC) & ARRAY_MASK;
			}
		};

		public abstract int next(int pageOffset, int wordOffset, int pos);
	}
	
	@Setup(Level.Trial)
	public void setup() {
		for (int i = 0; i < ARRAY_SIZE; i++) {
			memory[i] = 777;
		}
	}
	
	@Benchmark
	public long linearWalk() {
		return perfTest(StrideType.LINEAR_WALK);
	}
	
	@Benchmark
	public long randomPageWalk() {
		return perfTest(StrideType.RANDOM_PAGE_WALK);
	}
	
	@Benchmark
	public long randomHeapWalk() {
		return perfTest(StrideType.RANDOM_HEAP_WALK);
	}
	
	private static long perfTest(final StrideType strideType) {
		int pos = -1;
		long result = 0;
		for (int pageOffset = 0; pageOffset < ARRAY_SIZE; pageOffset += WORDS_PER_PAGE) {
			for (int wordOffset = pageOffset, limit = pageOffset
					+ WORDS_PER_PAGE; wordOffset < limit; wordOffset++) {
				pos = strideType.next(pageOffset, wordOffset, pos);
				result += memory[pos];
			}
		}
		return result;
	}
	
}
