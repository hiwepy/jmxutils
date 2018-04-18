/*
 * Copyright (c) 2010-2020, vindell (https://github.com/vindell).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.vindell.jmxutils;

import java.util.List;
import java.util.Map;

import com.github.vindell.jmxutils.CapacityUtils.Unit;
import com.github.vindell.jmxutils.property.JvmAttributes;
import com.github.vindell.jmxutils.property.MemoryInfo;

import junit.framework.TestCase;

public class JVMInfo_Test extends TestCase {


	public void testInfo() throws Exception {
		System.out.println("======================info=============================");
		Map<String, Object> infoMap = JvmInfoUtils.info();
		for (String key : infoMap.keySet()) {
			System.out.println(key + " : " + infoMap.get(key));
		}
		
		
		long estimatedTime = System.currentTimeMillis() - Long.parseLong(String.valueOf(infoMap.get(JvmAttributes.JAVA_RUNTIME_STARTTIME.getKey())));

		System.out.println("运行时间:" + estimatedTime / 1000 / 60);
		
		long estimatedUpTime = Long.parseLong(String.valueOf(infoMap.get(JvmAttributes.JAVA_RUNTIME_UPTIME)));
		
		System.out.println("进程 CPU 时间:" + estimatedUpTime / 1000 / 60);
		
		
	}

	public void wtestMemory_MB() throws Exception {
		System.out.println("======================runtime=============================");
		System.out.println(JvmInfoUtils.runtime(Unit.MB));
	}

	/*public void testMemory_KB() throws Exception {
		System.out.println(JVMInfo.runtime(Unit.KB));

	}*/
	
	public void wtestMemory() throws Exception {
		
		System.out.println("======================memory=============================");
		
		List<MemoryInfo> infoList = JvmInfoUtils.memory(Unit.KB);
		for (MemoryInfo memoryMap : infoList) {
			System.out.println( memoryMap.getType() + ":" + memoryMap.toMap());
			System.out.println( memoryMap.getType() + ":" +memoryMap.toString());
            System.out.println("===================================================");
		}
	}
	
	public void wtestMemoryPool() throws Exception {
		
		System.out.println("======================memoryPool=============================");
		 
		List<MemoryInfo> infoList = JvmInfoUtils.memoryPool(Unit.KB);
		for (MemoryInfo memoryMap : infoList) {
			System.out.println( memoryMap.getType() + ":" + memoryMap.toMap());
			System.out.println( memoryMap.getType() + ":" +memoryMap.toString());
            System.out.println("===================================================");
		}
	}
	
	public void wtestGc() throws Exception {
		System.out.println("==========================GarbageCollector=========================");
		for (Map<String, Object> infoMap : JvmInfoUtils.gc()) {
			for (String key : infoMap.keySet()) {
				System.out.println(key + " : " + infoMap.get(key));
			}
		}
	}

}
