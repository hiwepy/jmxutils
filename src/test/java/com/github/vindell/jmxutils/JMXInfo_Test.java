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
import com.github.vindell.jmxutils.property.MemoryInfo;

import junit.framework.TestCase;

public class JMXInfo_Test extends TestCase {

	public void testOs() throws Exception {
		
		System.out.println("======================OS=============================");
		
		Map<String, Object> infoMap = JmxInfoUtils.osAttributes();
		for (String key : infoMap.keySet()) {
			System.out.println(key + " : " + infoMap.get(key));
		}
	}
	
	public void testRuntime() throws Exception {
		
		System.out.println("======================runtime=============================");
		Map<String, Object> infoMap = JmxInfoUtils.runtimeAttributes();
		for (String key : infoMap.keySet()) {
			System.out.println(key + " : " + infoMap.get(key));
		}
	}
	
	public void testMemory() throws Exception {
		
		System.out.println("======================memory=============================");
		
		List<MemoryInfo> infoList = JmxInfoUtils.memoryAttributes(Unit.KB);
		for (MemoryInfo memoryMap : infoList) {
			System.out.println( memoryMap.getType() + ":" + memoryMap.toMap());
			System.out.println( memoryMap.getType() + ":" +memoryMap.toString());
            System.out.println("===================================================");
		}
	}
	
	public void testMemoryPool() throws Exception {
		
		System.out.println("======================memoryPool=============================");
		 
		List<MemoryInfo> infoList = JmxInfoUtils.memoryPoolAttributes(Unit.KB);
		for (MemoryInfo memoryMap : infoList) {
			System.out.println( memoryMap.getType() + ":" + memoryMap.toMap());
			System.out.println( memoryMap.getType() + ":" +memoryMap.toString());
            System.out.println("===================================================");
		}
	}
	
	public void testCompilation() throws Exception {
		System.out.println("==========================Compilation=========================");
		Map<String, Object> infoMap = JmxInfoUtils.compilationAttributes();
		for (String key : infoMap.keySet()) {
			System.out.println(key + " : " + infoMap.get(key));
		}
	}
	
	public void testGc() throws Exception {
		System.out.println("==========================GarbageCollector=========================");
		Map<String, Object> infoMap = JmxInfoUtils.gcAttributes();
		for (String key : infoMap.keySet()) {
			System.out.println(key + " : " + infoMap.get(key));
		}
	}
	
}
