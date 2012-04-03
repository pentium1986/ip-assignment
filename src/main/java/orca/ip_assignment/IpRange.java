package orca.ip_assignment;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpRange {
	private int address;
	private int netmask;
	private int length;
	
	public IpRange() {
		
	}
	
	public IpRange(int addr, int nm, int len) {
		address = addr;
		netmask = nm;
		length = len;
	}
	
	public void setStartAddress(int addr) {
		address = addr;
	}
	
	public void setLength(int len) {
		length = len;
	}
	
	public void setNetmask(int nm) {
		netmask = nm;
	}
	
	public int getStartAddress() {
		return address;
	}
	
	public int getLength() {
		return length;
	}
	
	public int getNetmask() {
		return netmask;
	}
	
	public static String convertAddrToStr(int addr) {
		byte[] addrArray = new byte[] {
				(byte)((addr >>> 24) & 0xff),
				(byte)((addr >>> 16) & 0xff),
				(byte)((addr >>> 8) & 0xff),
				(byte)(addr & 0xff)
		};
		try {
			return InetAddress.getByAddress(addrArray).getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
