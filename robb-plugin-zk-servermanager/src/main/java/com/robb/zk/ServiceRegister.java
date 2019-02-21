package com.robb.zk;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import com.robb.zk.common.Constant;

public class ServiceRegister {

	private static Logger logger = LoggerFactory.getLogger(ServiceRegister.class);
	
	public static boolean register(Environment evn) throws Exception {
		Set<String> ips = ipInfo();
		CuratorConnect connect = new CuratorConnect();
		String serverName = evn.getProperty("serverName");
		String serverDescription = evn.getProperty("serverDescription");
		String serverGroup = "/"+Constant.serviceGroupPrefix.concat(serverName);
		String port = evn.getProperty("server.port");
		//服务组是否存在
		if (connect.client.checkExists().forPath(serverGroup) == null) {
			connect.client.create().forPath(serverGroup,serverDescription.getBytes());
		}
		
		for (String ip : ips) {
			StringBuffer sb = new StringBuffer(serverGroup);
			sb.append("/").append(Constant.servicePrefix).append(serverName).append("_").append(ip).append("_").append(port);
			logger.info("注册服务：id=:{},serverDescription:{}", sb.toString(),serverDescription);
			connect.client.create().withMode(CreateMode.EPHEMERAL).forPath(sb.toString(), serverDescription.getBytes());
		}
		
		return true;
	}
	
	   private static Set<String> ipInfo(){
	        String regex = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";  
	        Set<String> ips = new HashSet<String>();
	    	try {
				InetAddress inetAddress = InetAddress.getLocalHost();
				NetworkInterface networkInterface = null;
				Enumeration<NetworkInterface> e=NetworkInterface.getNetworkInterfaces();
				String macTmp = "";
		        while(e.hasMoreElements())
		        {
		        	NetworkInterface ni = e.nextElement();
					logger.info("networkInterface name is {}", ni.getName());
	                Enumeration<InetAddress> addresses = ni.getInetAddresses();
		        	while (addresses!=null&&addresses.hasMoreElements()) {
		        		inetAddress = addresses.nextElement();
		        		if (inetAddress != null && inetAddress instanceof Inet4Address) {
							logger.info("ip4 address is {}", inetAddress.getHostAddress());
						}else {
							logger.info("ip6 address is {}", inetAddress.getHostAddress());
							continue;
						}
						if (!Pattern.matches(regex, inetAddress.getHostAddress())) {
							continue;
						}
						if (inetAddress.getHostAddress().equals("127.0.0.1")) {
							continue;
						}
						if (!inetAddress.getHostAddress().startsWith("192.168.0.")) {
							continue;
						}
//						ip = inetAddress.getHostAddress();
						ips.add(inetAddress.getHostAddress());
						break;
		        	}
		        }
			} catch (UnknownHostException | SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	return ips;
	    }

}
