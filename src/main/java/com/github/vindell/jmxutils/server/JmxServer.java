package com.github.vindell.jmxutils.server;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import com.github.vindell.jmxutils.MonitorBean;

public class JmxServer {

	private JMXConnectorServer jmxServer = null;
	private MBeanServer mBserver = null;
	private ObjectName mbeanObjectName = null;

	public JmxServer(String objectName, String serviceURL) {
		
	}
	
	/**
	 * 启动服务
	 * 
	 * @param hostname
	 * @param port
	 */
	public void doServer(String hostname, int port) {
		mBserver = MBeanServerFactory.createMBeanServer();
		try {
			MonitorBean bean = new MonitorBean();
			mbeanObjectName = new ObjectName("zfsoft:type=MonitorBean");
			mBserver.registerMBean(bean, mbeanObjectName);
			LocateRegistry.createRegistry(port);
			JMXServiceURL url = new JMXServiceURL(
					"service:jmx:rmi:///jndi/rmi://" + hostname + ":" + port + "/monitor");
			jmxServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mBserver);
			jmxServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 停止服务
	 */
	public void stop() {
		try {
			jmxServer.stop();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			jmxServer = null;
		}
	}

	/**
	 * 设置属性
	 * 
	 * @param name
	 * @param value
	 */
	public void doAttribute(String name, Object value) {
		doAttribute(new Attribute(name, value));
	}

	/**
	 * 设置属性
	 * @param attribute
	 */
	public void doAttribute(Attribute attribute) {
		try {
			mBserver.setAttribute(mbeanObjectName, attribute);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 执行方法
	 * 
	 * @param operationName
	 */
	public void doInvoke(String operationName) {
		doInvoke(operationName, (Object[]) null);
	}

	/**
	 * 执行方法
	 * 
	 * @param operationName
	 * @param params
	 */
	public void doInvoke(String operationName, Object[] params) {
		try {
			mBserver.invoke(mbeanObjectName, operationName, params, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
