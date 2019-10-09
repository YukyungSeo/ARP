package arp;

import java.util.ArrayList;

public class TCPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	private class TCPLayer_HEADER{
		short tcp_sport;	//source port (2byte)
		short tcp_dport;	//destination port (2byte)
		int tcp_seq;		//sequence number (4byte)
		int tcp_ack;		//acknowledged sequence (4byte)
		char tcp_offset;	//no use (1byte)
		char tcp_flag;		//control flag (1byte)
		short tcp_window;	//no use (2byte)
		short tcp_cksum;	//check sum (2byte)
		short tcp_urgptr;	//no use (2byte)
		char Padding[];		//(4byte)
		char tcp_data[];	//data part
		
		public TCPLayer_HEADER() {
			// 이렇게 하는게 아닌것 같음......
			tcp_sport = 0;
			tcp_dport = 0;
			tcp_seq = 0;
			tcp_ack = 0;
			tcp_offset = 0;
			tcp_flag = 0;
			tcp_window = 0;
			tcp_cksum = 0;
			tcp_urgptr = 0;
			Padding = new char[4];
			//tcp_data = new char[TCP_DATA_SIZE];
		}
	}
	
	TCPLayer_HEADER m_sHeader = new TCPLayer_HEADER();
	
	public boolean Send(byte[] input, int length) {
		//TCP 헤더의 destination port number를 확인하여, Application Layer로 구분하여 전달
		//그 외 member method는 구현가능
		if(m_sHeader.tcp_dport==m_sHeader.tcp_sport) {
			// Dest_Port가 같으면 전달 O
			
		}
		// Dest_Port가 다르면 전달 X
		return false;
	}
	
	public TCPLayer(String pName) {
		// TODO Auto-generated constructor stub
		pLayerName = pName;
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
