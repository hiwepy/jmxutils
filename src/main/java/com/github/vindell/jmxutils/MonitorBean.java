package com.github.vindell.jmxutils;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MonitorBean {
	private boolean debug = false;

	public Map<String, Object> getClassPeriod() {
		ClassLoadingMXBean clmx = ManagementFactory.getClassLoadingMXBean();
		// 返回当前加载到 Java 虚拟机中的类的数量。
		int lcc = clmx.getLoadedClassCount();
		// 返回自 Java 虚拟机开始执行到目前已经加载的类的总数。
		long tlcc = clmx.getTotalLoadedClassCount();
		// 活动线程
		long ucc = clmx.getUnloadedClassCount();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tlcc", tlcc);
		map.put("ucc", ucc);
		map.put("lcc", lcc);
		DateFormat df = new SimpleDateFormat("hh:mm:ss");
		map.put("ts", df.format(new Date(System.currentTimeMillis())));
		return map;
	}

	private long upTime = -1L;
	private long processCpuTime = -1L;
	private int nCPUs;
	private long prevUpTime = 0L;
	private long prevProcessCpuTime = 0L;
	private boolean iscpufirst = false;

	public Map<String, Object> getCpuPeriod() {
		RuntimeMXBean rmBean = ManagementFactory.getRuntimeMXBean();
		OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();
		Map<String, Object> map = new HashMap<String, Object>();
		nCPUs = operatingSystem.getAvailableProcessors();
		upTime = rmBean.getUptime();
		final String className = operatingSystem.getClass().getName();
		if ("com.sun.management.OperatingSystem".equals(className)
				|| "com.sun.management.UnixOperatingSystem".equals(className)) {
			final com.sun.management.OperatingSystemMXBean sunOSMBean = (com.sun.management.OperatingSystemMXBean) operatingSystem;
			processCpuTime = sunOSMBean.getProcessCpuTime();
			long l = processCpuTime / 1000000;
			debug("CPU处理时间:" + l + "毫秒");
			float cpuUsedTime = (float) processCpuTime / 1000000000;
			debug("CPU处理时间:" + cpuUsedTime + "秒");
		}

		if (prevUpTime > 0L && upTime > prevUpTime) {
			// elapsedCpu is in ns and elapsedTime is in ms.
			long elapsedCpu = processCpuTime - prevProcessCpuTime;
			long elapsedTime = upTime - prevUpTime;
			// cpuUsage could go higher than 100% because elapsedTime
			// and elapsedCpu are not fetched simultaneously. Limit to
			// 99% to avoid Plotter showing a scale from 0% to 200%.
			float cpuUsage = Math.min(99F, elapsedCpu / (elapsedTime * 10000F * nCPUs));
			debug("CPU使用率：" + cpuUsage + "% /" + Math.round(cpuUsage * Math.pow(10.0, 1)) + "/"
					+ String.format("%.1f", cpuUsage));

			map.put("u", String.format("%.1f", cpuUsage));
			DateFormat df = new SimpleDateFormat("hh:mm:ss");
			map.put("ts", df.format(new Date(System.currentTimeMillis())));
		}
		if (!iscpufirst) {
			map.put("u", String.format("%.1f", 0f));
			DateFormat df = new SimpleDateFormat("hh:mm:ss");
			map.put("ts", df.format(new Date(System.currentTimeMillis())));
			iscpufirst = true;
		}
		prevUpTime = upTime;
		prevProcessCpuTime = processCpuTime;
		return map;
	}

	public Map<String, Object> getMemoryPeriod() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		try {
			MemoryMXBean mmxb = ManagementFactory.getMemoryMXBean();
			MemoryUsage mu = mmxb.getHeapMemoryUsage();
			debug("getHeapMemoryUsage/getUsed = " + mu.getUsed());
			map.put("HeapMemory", mu.getUsed());
			MemoryUsage nmu = mmxb.getNonHeapMemoryUsage();
			debug("getNonHeapMemoryUsage/getUsed = " + nmu.getUsed());
			map.put("NonHeapMemory", nmu.getUsed());

			List<MemoryPoolMXBean> mpmxbList = ManagementFactory.getMemoryPoolMXBeans();
			for (MemoryPoolMXBean mpmxb : mpmxbList) {
				debug("MemoryPoolMXBean Name=" + mpmxb.getName());
				String[] mmnArray = mpmxb.getMemoryManagerNames();
				String mpmxbName = mpmxb.getName();
				long used = 0L;
				for (String mmn : mmnArray) {
					debug("MemoryManagerMXBean/ManagerNames=" + mmn);
					MemoryUsage mpmxbU = mpmxb.getUsage();
					debug("MemoryPoolMXBean/getUsed = " + mpmxbU.getUsed());
					used += mpmxbU.getUsed();
				}
				debug("################################");
				map.put(mpmxbName, used);
			}
			DateFormat df = new SimpleDateFormat("hh:mm:ss");
			map.put("ts", df.format(new Date(System.currentTimeMillis())));
		} catch (Exception e) {
			debug(e.getMessage());
		}
		return map;
	}

	public Map<String, Object> getSystemInfo() {
		OperatingSystemMXBean osMBean = ManagementFactory.getOperatingSystemMXBean();
		com.sun.management.OperatingSystemMXBean sunOSMBean = (com.sun.management.OperatingSystemMXBean) osMBean;
		String osName = osMBean.getName();
		String osVersion = osMBean.getVersion();
		String osArch = osMBean.getArch();
		int nCPUs = osMBean.getAvailableProcessors();
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("操作系统", osName + " " + osVersion);
		map.put("体系结构", osArch);
		map.put("处理器数目", nCPUs + "");
		String os = System.getProperty("os.name") + ' ' + System.getProperty("sun.os.patch.level") + ", "
				+ System.getProperty("os.arch") + '/' + System.getProperty("sun.arch.data.model");
		map.put("系统详细", os);
		map.put("主机名称", MonitorUtils.getHostName());
		map.put("主机地址", MonitorUtils.getHostAddress());
		// Must use separator of remote OS, not File.pathSeparator
		// from this local VM. In the future, consider using
		// RuntimeMXBean to get the remote system property.
		if (sunOSMBean != null) {
			String[] kbStrings1 = MonitorUtils.formatKByteStrings(sunOSMBean.getCommittedVirtualMemorySize());
			String[] kbStrings2 = MonitorUtils.formatKByteStrings(sunOSMBean.getTotalPhysicalMemorySize(),
					sunOSMBean.getFreePhysicalMemorySize(), sunOSMBean.getTotalSwapSpaceSize(),
					sunOSMBean.getFreeSwapSpaceSize());
			map.put("分配的虚拟内存", kbStrings1[0]);
			map.put("物理内存总量", kbStrings2[0]);
			map.put("可用物理内存", kbStrings2[1]);
			map.put("交换空间总量", kbStrings2[2]);
			map.put("可用交换空间", kbStrings2[3]);
		}
		String username = System.getProperty("user.name");
		map.put("用户", username);
		String userhome = System.getProperty("user.home");
		map.put("用户目录", userhome);
		String userdir = System.getProperty("user.dir");
		map.put("当前目录", userdir);
		String tmpdir = System.getProperty("java.io.tmpdir");
		map.put("临时目录", tmpdir);
		String timezone = System.getProperty("user.timezone");
		map.put("时区", timezone);
		String country = System.getProperty("user.country");
		map.put("国家", country);
		String language = System.getProperty("user.language");
		map.put("语言", language);
		String fileseparator = System.getProperty("file.separator");
		map.put("默认名称分隔符", fileseparator);
		String pathseparator = System.getProperty("path.separator");
		map.put("默认路径分隔符", pathseparator);
		String fileencoding = System.getProperty("file.encoding");
		map.put("默认文件编码", fileencoding);
		String lineseparator = System.getProperty("line.separator");
		map.put("默认换行符", lineseparator);

		return map;
	}

	public Map<String, Object> getSystemJava() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		String javaVersion = System.getProperty("java.runtime.name") + ", "
				+ System.getProperty("java.runtime.version");
		map.put("版本信息", javaVersion);
		String javahome = System.getProperty("java.home");
		map.put("JAVA_HOME目录", javahome);
		return map;
	}

	public Map<String, Object> getSystemServer() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		try {
			if (System.getProperty("catalina.home") != null) {
				map.put("应用服务器", "Tomcat");
			} else {
				//map.put("应用服务器", ServerDetector.getServerId());
			}
		} catch (Exception e) {
			map.put("应用服务器", "未知或者没有");
		}
		return map;
	}

	public Map<String, Object> getThreadPeriod() {
		ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
		// 守护线程
		int dtc = tmxb.getDaemonThreadCount();
		// 线程峰值
		int ptc = tmxb.getPeakThreadCount();
		// 线程总数
		long tstc = tmxb.getTotalStartedThreadCount();
		// 活动线程
		long tc = tmxb.getThreadCount();
		// 非守护线程
		long ndtc = tc - dtc;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tstc", tstc);
		map.put("ptc", ptc);
		map.put("tc", tc);
		map.put("dtc", dtc);
		map.put("ndtc", ndtc);
		DateFormat df = new SimpleDateFormat("hh:mm:ss");
		map.put("ts", df.format(new Date(System.currentTimeMillis())));
		return map;
	}

	public List<Map<String, Object>> getThreads() {
		ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
		Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
		List<Thread> threads = new ArrayList<Thread>(map.keySet());
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> content = null;
		long id = 0L;
		ThreadInfo threadInfo = null;

		int alive = 0;
		for (Thread t : threads) {
			if (t.isAlive())
				alive++;
			content = new HashMap<String, Object>();
			id = t.getId();
			content.put("Id", id);
			content.put("Name", t.getName());
			debug(t.getPriority());
			content.put("Priority", MonitorUtils.getThreadPriorityCN(t.getPriority()));
			threadInfo = tmxb.getThreadInfo(id, Integer.MAX_VALUE);
			content.put("State", threadInfo.getThreadState().name());
			content.put("CpuTime", tmxb.getThreadCpuTime(id));
			content.put("UserTime", tmxb.getThreadUserTime(id));
			content.put("Daemon", t.isDaemon());
			StackTraceElement[] stes = threadInfo.getStackTrace();
			StringBuffer sb = new StringBuffer();
			for (StackTraceElement ste : stes) {
				sb.append("类名:[").append(ste.getClassName()).append("]/方法名:[").append(ste.getMethodName()).append("] ");
			}
			content.put("StackTrace", sb.toString());

			list.add(content);
		}
		debug("getDaemonThreadCount() = " + tmxb.getDaemonThreadCount());
		debug("getPeakThreadCount = " + tmxb.getPeakThreadCount());
		debug("getTotalStartedThreadCount = " + tmxb.getTotalStartedThreadCount());
		debug("getThreadCount = " + tmxb.getThreadCount());
		debug("threads = " + threads.size());
		debug("alive = " + alive);
		return list;
	}

	public Map<String, Object> getVmInfo() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd F hh:mm:ss SSS");
		RuntimeMXBean rmBean = ManagementFactory.getRuntimeMXBean();
		CompilationMXBean cmpMBean = ManagementFactory.getCompilationMXBean();
		ThreadMXBean tmBean = ManagementFactory.getThreadMXBean();
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		ClassLoadingMXBean clMBean = ManagementFactory.getClassLoadingMXBean();
		OperatingSystemMXBean osMBean = ManagementFactory.getOperatingSystemMXBean();

		com.sun.management.OperatingSystemMXBean sunOSMBean = (com.sun.management.OperatingSystemMXBean) osMBean;

		Map<String, Object> map = new LinkedHashMap<String, Object>();

		String dateTime = df.format(System.currentTimeMillis());
		map.put("当前时间", dateTime);
		map.put("连接名称", "");
		map.put("虚拟机实现版本", rmBean.getVmName() + rmBean.getVmVersion());
		map.put("虚拟机实现供应商", rmBean.getVmVendor());
		map.put("虚拟机的名称", rmBean.getName());
		map.put("虚拟机的正常运行时间", "" + rmBean.getUptime());
		if (sunOSMBean != null) {
			float cpuUsedTime = (float) sunOSMBean.getProcessCpuTime() / 1000000000;
			map.put("Process CPU time", "" + cpuUsedTime);
		}

		if (cmpMBean != null) {
			map.put("即时 (JIT) 编译器的名称", cmpMBean.getName());
			map.put("编译累积耗费时间",
					cmpMBean.isCompilationTimeMonitoringSupported() ? cmpMBean.getTotalCompilationTime() + ""
							: "Unavailable");
		} else {
			map.put("即时 (JIT) 编译器的名称", "Unavailable");
		}

		int tlCount = tmBean.getThreadCount();
		int tdCount = tmBean.getDaemonThreadCount();
		int tpCount = tmBean.getPeakThreadCount();
		long ttCount = tmBean.getTotalStartedThreadCount();
		// 当前线程个数
		debug("当前线程个数:" + tlCount);
		map.put("活动线程的当前数目[守护线程和非守护线程]", tlCount);
		map.put("自从虚拟机启动或峰值重置以来峰值活动线程计数", tpCount);
		map.put("活动守护线程的当前数目", tdCount);
		map.put("自从虚拟机启动以来创建和启动的线程总数目", ttCount);

		long clCount = clMBean.getLoadedClassCount();
		long cuCount = clMBean.getUnloadedClassCount();
		long ctCount = clMBean.getTotalLoadedClassCount();
		map.put("当前加载到虚拟机中的类的数量", clCount);
		map.put("从虚拟机开始执行到目前已经加载的类的总数", ctCount);
		map.put("从虚拟机开始执行到目前已经卸载的类的总数", cuCount);

		// 用于对象分配的堆的当前内存使用量
		MemoryUsage u = memoryBean.getHeapMemoryUsage();
		String[] strings11 = MonitorUtils.formatKByteStrings(u.getUsed(), u.getMax());
		map.put("堆的已使用的内存量", strings11[0]);
		map.put("堆的可以用于内存管理的最大内存量", strings11[1]);
		String[] strings22 = MonitorUtils.formatKByteStrings(u.getCommitted());
		// 分配的内存
		debug("分配的内存：" + u.getCommitted() / 1024 + "Kb");
		map.put("堆的已提交给虚拟机使用的内存量", strings22[0]);

		map.put("内存其终止被挂起的对象的近似数目", "" + memoryBean.getObjectPendingFinalizationCount());

		List<GarbageCollectorMXBean> garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans();
		for (GarbageCollectorMXBean garbageCollectorMBean : garbageCollectors) {
			String gcName = garbageCollectorMBean.getName();
			long gcCount = garbageCollectorMBean.getCollectionCount();
			long gcTime = garbageCollectorMBean.getCollectionTime();

			map.put("内存池[Garbage collector]", MonitorUtils.getText("GcInfo", gcName, gcCount,
					(gcTime >= 0) ? MonitorUtils.formatTime(gcTime) : MonitorUtils.getText("Unavailable")));
		}

		return map;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isDebug() {
		return this.debug;
	}

	public void debug(Object... objects) {
		if (isDebug()) {
			for (Object o : objects) {
				System.out.println(o);
			}
		}
	}
}