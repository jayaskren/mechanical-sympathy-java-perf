package net.askren.mechanicalsympathy;

import static java.lang.System.out;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TestSequentialoPerformance {
	public static final int PAGE_SIZE = 1024 * 4;
    public static final long FILE_SIZE = PAGE_SIZE * 2000L * 1000L/20L;
	public static final long NUM_PAGES = (FILE_SIZE/PAGE_SIZE);
	public static final String FILE_NAME = "test.dat";
	public static final byte[] BLANK_PAGE = new byte[PAGE_SIZE];

	public static void main(final String[] arg) throws Exception {
		preallocateTestFile(FILE_NAME);

		for (final PerfTestCase testCase : testCases) {
			for (int i = 0; i < 5; i++) {
				System.gc();
				long writeDurationMs = testCase.test(PerfTestCase.Type.WRITE, FILE_NAME);

				System.gc();
				long readDurationMs = testCase.test(PerfTestCase.Type.READ, FILE_NAME);

				long bytesReadPerSec = 0;
				if (readDurationMs >0 ) {
					bytesReadPerSec = (FILE_SIZE / 1000L) / readDurationMs;
				}
				long bytesWrittenPerSec = 0;
				if (writeDurationMs > 0) {
					bytesWrittenPerSec = (FILE_SIZE / 1000L) / writeDurationMs;
				}

				out.format("%s\twrite=%,d MB/sec\tread=%,d MB/sec\n",
                        testCase.getName(), bytesWrittenPerSec, bytesReadPerSec);
			}
		}

		deleteFile(FILE_NAME);
	}

	private static void preallocateTestFile(final String fileName) throws Exception {
		RandomAccessFile file = new RandomAccessFile(fileName, "rw");

		for (long i = 0; i < FILE_SIZE; i += PAGE_SIZE) {
			file.write(BLANK_PAGE, 0, PAGE_SIZE);
		}

		file.close();
	}

	private static void deleteFile(final String testFileName) throws Exception {
		File file = new File(testFileName);
		if (!file.delete()) {
			System.out.println("Failed to delete test file=" + testFileName);
			System.out.println("Windows does not allow mapped files to be deleted.");
		}
	}

	public abstract static class PerfTestCase {
		public enum Type {
			READ, WRITE
		}

		private final String name;
		private int checkSum;

		public PerfTestCase(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public long test(final Type type, final String fileName) {
			long start = System.currentTimeMillis();

			try {
				switch (type) {
				case WRITE: {
					checkSum = testWrite(fileName);
					break;
				}

				case READ: {
					final int checkSum = testRead(fileName);
					if (checkSum != this.checkSum) {
						final String msg = getName() + " expected=" + this.checkSum + " got=" + checkSum;
						throw new IllegalStateException(msg);
					}
					break;
				}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			return System.currentTimeMillis() - start;
		}

		public abstract int testWrite(final String fileName) throws Exception;

		public abstract int testRead(final String fileName) throws Exception;
	}

	private static PerfTestCase[] testCases = {
			new PerfTestCase("RandomAccessFile")
	        {
	            public int testWrite(final String fileName) throws Exception
	            {
	                RandomAccessFile file = new RandomAccessFile(fileName, "rw");
	                final byte[] buffer = new byte[PAGE_SIZE];
	                int pos = 0;
	                int checkSum = 0;

	                for (long i = 0; i < FILE_SIZE; i++)
	                {
	                    byte b = (byte)i;
	                    checkSum += b;

	                    buffer[pos++] = b;
	                    if (PAGE_SIZE == pos)
	                    {
	                        file.write(buffer, 0, PAGE_SIZE);
	                        pos = 0;
	                    }
	                }

	                file.close();

	                return checkSum;
	            }

	            public int testRead(final String fileName) throws Exception
	            {
	                RandomAccessFile file = new RandomAccessFile(fileName, "r");
	                final byte[] buffer = new byte[PAGE_SIZE];
	                int checkSum = 0;
	                int bytesRead;

	                while (-1 != (bytesRead = file.read(buffer)))
	                {
	                    for (int i = 0; i < bytesRead; i++)
	                    {
	                        checkSum += buffer[i];
	                    }
	                }

	                file.close();

	                return checkSum;
	            }
	        },
			
			new PerfTestCase("FileStream\t") {
				public int testWrite(final String fileName) throws Exception {
					int checkSum = 0;
					FileOutputStream out = new FileOutputStream(fileName);
					final int bufferSize = 1024*8;
					byte[] buffer = new byte[bufferSize];
					
					final int numIterations = (int)(FILE_SIZE/PAGE_SIZE);
					for (int n = 0; n < numIterations; n++) {
						for (int p = 0; p < bufferSize; p++) {
							byte value = (byte) n;
							buffer[p] = value;
							checkSum += value;
							
						}
						out.write(buffer);
					}

					out.close();

					return checkSum;
				}

				public int testRead(final String fileName) throws Exception {
					int checkSum = 0;
					FileInputStream in = new FileInputStream(fileName);

					final int bufferSize = 1024*32;
					byte[] buffer = new byte[bufferSize];
					int bytesRead;
					
					
					while ((bytesRead = in.read(buffer)) > 0) {
						for (int i=0; i < bytesRead; i ++) {
							checkSum += buffer[i];
						}
					}
					
					in.close();

					return checkSum;
				}
			},
			

			new PerfTestCase("BufferedStreamFile") {
				public int testWrite(final String fileName) throws Exception {
					int checkSum = 0;
					OutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));

					for (long i = 0; i < FILE_SIZE; i++) {
						byte b = (byte) i;
						checkSum += b;
						out.write(b);
					}

					out.close();

					return checkSum;
				}

				public int testRead(final String fileName) throws Exception {
					int checkSum = 0;
					InputStream in = new BufferedInputStream(new FileInputStream(fileName));

					int b;
					while (-1 != (b = in.read())) {
						checkSum += (byte) b;
					}

					in.close();

					return checkSum;
				}
			},

			new PerfTestCase("BufferedChannelFile") {
				public int testWrite(final String fileName) throws Exception {
					FileChannel channel = new RandomAccessFile(fileName, "rw").getChannel();
					ByteBuffer buffer = ByteBuffer.allocate(PAGE_SIZE);
					int checkSum = 0;

					for (long i = 0; i < FILE_SIZE; i++) {
						byte b = (byte) i;
						checkSum += b;
						buffer.put(b);

						if (!buffer.hasRemaining()) {
							buffer.flip();
							channel.write(buffer);
							buffer.clear();
						}
					}

					channel.close();

					return checkSum;
				}

				public int testRead(final String fileName) throws Exception {
					FileChannel channel = new RandomAccessFile(fileName, "rw").getChannel();
					ByteBuffer buffer = ByteBuffer.allocate(PAGE_SIZE);
					int checkSum = 0;

					while (-1 != (channel.read(buffer))) {
						buffer.flip();

						while (buffer.hasRemaining()) {
							checkSum += buffer.get();
						}

						buffer.clear();
					}

					return checkSum;
				}
			},

			new PerfTestCase("MemoryMappedFile") {
				public int testWrite(final String fileName) throws Exception {
					FileChannel channel = new RandomAccessFile(fileName, "rw").getChannel();
					MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0,
							Math.min(channel.size(), Integer.MAX_VALUE));
					int checkSum = 0;

					for (long i = 0; i < FILE_SIZE; i++) {
						if (!buffer.hasRemaining()) {
							buffer = channel.map(FileChannel.MapMode.READ_WRITE, i,
									Math.min(channel.size() - i, Integer.MAX_VALUE));
						}

						byte b = (byte) i;
						checkSum += b;
						buffer.put(b);
					}

					channel.close();

					return checkSum;
				}

				public int testRead(final String fileName) throws Exception {
					FileChannel channel = new RandomAccessFile(fileName, "rw").getChannel();
					MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0,
							Math.min(channel.size(), Integer.MAX_VALUE));
					int checkSum = 0;

					for (long i = 0; i < FILE_SIZE; i++) {
						if (!buffer.hasRemaining()) {
							buffer = channel.map(FileChannel.MapMode.READ_WRITE, i,
									Math.min(channel.size() - i, Integer.MAX_VALUE));
						}

						checkSum += buffer.get();
					}

					channel.close();

					return checkSum;
				}
			}, };
}

//RandomAccessFile	write=271,960,693	read=439,862,542 bytes/sec
//RandomAccessFile	write=377,842,350	read=436,441,129 bytes/sec
//RandomAccessFile	write=282,775,284	read=494,327,781 bytes/sec
//RandomAccessFile	write=270,024,391	read=476,694,791 bytes/sec
//RandomAccessFile	write=307,127,057	read=469,348,000 bytes/sec
//BufferedStreamFile	write=186,258,014	read=283,185,840 bytes/sec
//BufferedStreamFile	write=186,431,806	read=281,521,701 bytes/sec
//BufferedStreamFile	write=179,307,023	read=177,269,973 bytes/sec
//BufferedStreamFile	write=192,323,042	read=177,615,888 bytes/sec
//BufferedStreamFile	write=203,745,616	read=179,071,851 bytes/sec
//BufferedChannelFile	write=290,971,087	read=463,374,625 bytes/sec
//BufferedChannelFile	write=256,224,196	read=467,900,388 bytes/sec
//BufferedChannelFile	write=295,026,470	read=466,780,626 bytes/sec
//BufferedChannelFile	write=301,753,351	read=452,296,819 bytes/sec
//BufferedChannelFile	write=294,920,257	read=424,258,117 bytes/sec
//MemoryMappedFile	write=118,072,671	read=246,598,434 bytes/sec
//MemoryMappedFile	write=163,424,900	read=242,352,523 bytes/sec
//MemoryMappedFile	write=163,758,120	read=228,424,838 bytes/sec
//MemoryMappedFile	write=161,396,457	read=285,077,951 bytes/sec
//MemoryMappedFile	write=158,046,032	read=211,537,468 bytes/sec
