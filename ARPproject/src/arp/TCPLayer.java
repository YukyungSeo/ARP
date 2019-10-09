package arp;

import java.util.ArrayList;

public class TCPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	private class _TCP_ADDR{
		private byte[] addr = new byte[4];
		
		public _TCP_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
		}
	}
	
	private class _TCPLayer_HEADER{
		
		_TCP_ADDR TCP_dstaddr;
		_TCP_ADDR TCP_srcaddr;
		
		byte[] tcp_sport;	//source port (2byte)
		byte[] tcp_dport;	//destination port (2byte)
		byte[] tcp_seq;		//sequence number (4byte)
		byte[] tcp_ack;		//acknowledged sequence (4byte)
		byte tcp_offset;	//no use (1byte)
		byte tcp_flag;		//control flag (1byte)
		byte[] tcp_window;	//no use (2byte)
		byte[] tcp_cksum;	//check sum (2byte)
		byte[] tcp_urgptr;	//no use (2byte)
		byte[] Padding[];		//(4byte)
		byte[] tcp_data[];	//data part
		
		public _TCPLayer_HEADER() {
			this.TCP_dstaddr = new _TCP_ADDR();
			this.TCP_srcaddr = new _TCP_ADDR();
			
			this.tcp_sport = new byte[2];
			this.tcp_dport = new byte[2];
			this.tcp_seq = new byte[4];
			this.tcp_ack = new byte[4];
			this.tcp_window = new byte[2];
			this.tcp_cksum = new byte[2];
			this.tcp_urgptr = new byte[2];
			this.Padding = new char[4];
			this.tcp_data = null;
			
			this.tcp_offset = (byte)0x00;
			this.tcp_flag = (byte)0x00;
		}
	}
	
	_TCPLayer_HEADER m_sHeader = new _TCPLayer_HEADER();
	
	public void ResetHeader() {
		for(int i = 0; i < 3; i++) {
			m_sHeader.TCP_dstaddr.addr[i] = (byte) 0x00;
			m_sHeader.TCP_srcaddr.addr[i] = (byte) 0x00;
		}
		
		m_sHeader.tcp_offset = (byte)0x00;
		m_sHeader.tcp_flag = (byte)0x00;
		m_sHeader.tcp_data = null;
	}
	
	public boolean Send(byte[] input, int length) {
		//TCP 헤더의 destination port number를 확인하여, Application Layer로 구분하여 전달
		//그 외 member method는 구현가능
		if(m_sHeader.tcp_dport==m_sHeader.tcp_sport) {
			// Dest_Port가 같으면 전달 O
			byte[] bytes = ObjToByte(m_sHeader, input, length);
			this.GetUnderLayer().Send(bytes, length + 20);
		}
		// Dest_Port가 다르면 전달 X
		return false;
	}
	
	private byte[] ObjToByte(_TCP_Header Header, byte[] input, int length) {
		// TODO Auto-generated method stub
		byte[] buf = new byte[28];

		buf[0] = Header.TCP_dstaddr.addr[0];
		buf[1] = Header.TCP_dstaddr.addr[1];
		buf[2] = Header.TCP_dstaddr.addr[2];
		buf[3] = Header.TCP_dstaddr.addr[3];

		buf[4] = Header.TCP_srcaddr.addr[4];
		buf[5] = Header.TCP_srcaddr.addr[5];
		buf[6] = Header.TCP_srcaddr.addr[6];
		buf[7] = Header.TCP_srcaddr.addr[7];
		
		buf[8] = Header.tcp_sport[0];
		buf[9] = Header.tcp_sport[1];
		
		buf[10] = Header.tcp_dport[0];
		buf[11] = Header.tcp_dport[1];
		
		buf[12] = Header.tcp_seq[0];
		buf[13] = Header.tcp_seq[1];
		buf[14] = Header.tcp_seq[2];
		buf[15] = Header.tcp_seq[3];
		
		buf[16] = Header.tcp_ack[0];
		buf[17] = Header.tcp_ack[1];
		buf[18] = Header.tcp_ack[2];
		buf[19] = Header.tcp_ack[3];
		
		buf[20] = Header.tcp_offset;
		buf[21] = Header.tcp_flag;
		
		buf[22] = Header.tcp_window[0];
		buf[23] = Header.tcp_window[1];
		
		buf[24] = Header.tcp_cksum[0];
		buf[25] = Header.tcp_cksum[1];
		
		buf[26] = Header.tcp_urgptr[0];
		buf[27] = Header.tcp_urgptr[1];
		
		buf[28] = Header.Padding[0];
		buf[29] = Header.Padding[1];
		buf[30] = Header.Padding[2];
		buf[31] = Header.Padding[3];
		
		for (int i = 0; i < length; i++)
			buf[32 + i] = input[i];

		return buf;
	}
	
	public TCPLayer(String pName) {
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
		if(p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		// TODO Auto-generated method stub
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}

}
