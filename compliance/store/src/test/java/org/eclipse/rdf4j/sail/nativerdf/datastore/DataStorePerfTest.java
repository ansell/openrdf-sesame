/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.nativerdf.datastore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.common.io.FileUtil;
import org.eclipse.rdf4j.sail.nativerdf.datastore.DataStore;

/**
 * 
 */
public class DataStorePerfTest {
	
	public static void main(String[] args)
		throws Exception
	{
		System.out.println("DataStore performance test");
		System.out.println("==========================");
		
		System.out.println("Warming up...");
		for (int i = 0; i < 3; i++) {
			runPerformanceTest(10000);
			System.gc();
			Thread.sleep(2000);
		}

		System.out.println("Starting test...");

		List<long[]> timeDataList = new ArrayList<long[]>();
		
		for (int stringCount = 1000000; stringCount <= 3000000; stringCount += 1000000) {
			timeDataList.add( runPerformanceTest(stringCount));
			System.gc();
			Thread.sleep(1000);
		}

		System.out.println("Performance test results, average times in micro seconds");
		System.out.println("#str\tstore\tgetID\tgetData");
		for (long[] timeData : timeDataList) {
			System.out.printf("%d\t%d\t%d\t%d",
				timeData[0], timeData[1]/1000, timeData[2]/1000, timeData[3]/1000);
			System.out.println();
		}
	}
	
	private static long[] runPerformanceTest(int stringCount)
		throws Exception
	{
		System.out.println("Running performance test with " + stringCount + " strings...");
	
		long[] timeData = new long[4];
		timeData[0] = stringCount;
	
		File dataDir = FileUtil.createTempDir("datastoretest");
	
		try {
			System.out.println("Initializing data store in directory " + dataDir);
			DataStore dataStore = new DataStore(dataDir, "strings");
	
			System.out.println("Storing strings...");
			long startTime = System.nanoTime();
	
			for (int i = 1; i <= stringCount; i++) {
				dataStore.storeData(String.valueOf(i).getBytes());
			}
	
			dataStore.sync();
			long endTime = System.nanoTime();
			timeData[1] = (endTime - startTime) / stringCount;
			System.out.println("Strings stored in " + (endTime-startTime)/1E6 + " ms");
	
			System.out.println("Fetching IDs for all strings...");
			startTime = System.nanoTime();
	
			for (int i = 1; i <= stringCount; i++) {
				int sID = dataStore.getID(String.valueOf(i).getBytes());
				if (sID == -1) {
					throw new RuntimeException("Failed to get ID for string \"" + i + "\"");
				}
			}
	
			endTime = System.nanoTime();
			timeData[2] = (endTime - startTime) / stringCount;
			System.out.println("All IDs fetched in " + (endTime-startTime)/1E6 + " ms");
	
			System.out.println("Fetching data for all IDs...");
			startTime = System.nanoTime();
	
			for (int id = 1; id <= stringCount; id++) {
				String s = new String(dataStore.getData(id));
				if (s == null) {
					throw new RuntimeException("Failed to get data for ID " + id);
				}
			}
	
			endTime = System.nanoTime();
			timeData[3] = (endTime - startTime) / stringCount;
			System.out.println("All data fetched in " + (endTime-startTime)/1E6 + " ms");
			
			System.out.println("Closing DataStore...");
			dataStore.close();
			System.out.println("Done.");
			
			return timeData;
		}
		finally {
			FileUtil.deleteDir(dataDir);
		}
	}
}
