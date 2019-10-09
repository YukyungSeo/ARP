package arp;

import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

public class ARPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	public ArrayList<_CACHE> queue_arpcache;

	private class _ETHERNET_ADDR {
		private byte[] addr = new byte[6];

		public _ETHERNET_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
			this.addr[4] = (byte) 0x00;
			this.addr[5] = (byte) 0x00;
		}
	}

	private class _INTERNET_ADDR {
		private byte[] addr = new byte[4];

		public _INTERNET_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
		}
	}
	
	public class _CACHE{
		
	}

	private class _ARP_Header {
		byte[] hw_type;
		byte[] proto_type;
		byte length_inetaddr;
		byte length_enetaddr;
		byte[] opcode;
		_ETHERNET_ADDR enet_srcaddr;
		_INTERNET_ADDR inet_srcaddr;
		_ETHERNET_ADDR enet_dstaddr;
		_INTERNET_ADDR inet_dstaddr;

		public _ARP_Header() {
			this.hw_type = new byte[2];
			this.proto_type = new byte[2];
			this.length_inetaddr = (byte) 0x00;
			this.length_enetaddr = (byte) 0x00;
			this.opcode = new byte[2];
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.inet_srcaddr = new _INTERNET_ADDR();
			this.enet_dstaddr = new _ETHERNET_ADDR();
			this.inet_dstaddr = new _INTERNET_ADDR();
		}
	}

	_ARP_Header m_sHeader = new _ARP_Header();

	public ARPLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	private void ResetHeader() {
		// TODO Auto-generated method stub
		m_sHeader.hw_type[0] = (byte) 0x00;
		m_sHeader.hw_type[1] = (byte) 0x00;
		m_sHeader.proto_type[0] = (byte) 0x00;
		m_sHeader.proto_type[1] = (byte) 0x00;
		m_sHeader.length_enetaddr = (byte) m_sHeader.enet_srcaddr.addr.length;
		m_sHeader.length_inetaddr = (byte) m_sHeader.inet_srcaddr.addr.length;
		m_sHeader.opcode[0] = (byte) 0x00;
		m_sHeader.opcode[1] = (byte) 0x00;

		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_dstaddr.addr[i] = (byte) 0x00;
			m_sHeader.enet_srcaddr.addr[i] = (byte) 0x00;
		}
		for (int i = 0; i < 4; i++) {
			m_sHeader.inet_dstaddr.addr[i] = (byte) 0x00;
			m_sHeader.inet_srcaddr.addr[i] = (byte) 0x00;
		}
	}

	public boolean Send(byte[] input, int length) {
		byte[] bytes = this.ObjToByte(m_sHeader);
		this.GetUnderLayer().Send(bytes, length + 28);
		return false;
	}

	private byte[] ObjToByte(_ARP_Header Header) {
		// TODO Auto-generated method stub
		byte[] buf = new byte[28];

		buf[0] = Header.hw_type[0];
		buf[1] = Header.hw_type[1];
		buf[2] = Header.proto_type[0];
		buf[3] = Header.proto_type[1];
		buf[4] = Header.length_enetaddr;
		buf[5] = Header.length_inetaddr;
		buf[6] = Header.opcode[0];
		buf[7] = Header.opcode[1];

		for (int i = 0; i < 6; i++) {
			buf[8 + i] = Header.enet_srcaddr.addr[i];
		}
		for (int i = 0; i < 4; i++) {
			buf[14 + i] = Header.inet_srcaddr.addr[i];
		}
		for (int i = 0; i < 6; i++) {
			buf[18 + i] = Header.enet_dstaddr.addr[i];
		}
		for (int i = 0; i < 4; i++) {
			buf[24 + i] = Header.inet_dstaddr.addr[i];
		}

		return buf;
	}

	public boolean Receive(byte[] input) {
		if (IsItMine(input) == false) {
			return false;
		}
		
		// 내 것이면
		if (IsItReply(input) == false) {
			// 요청일 경우
			// 설정 & 재전송
			for (int i = 0; i < 6; i++) {
				m_sHeader.enet_dstaddr.addr[i] = input[8+i];
			}
			for (int i = 0; i < 4; i++) {
				m_sHeader.inet_dstaddr.addr[i] = input[14+i];
			}
			m_sHeader.opcode[0] = (byte) 0;
			m_sHeader.opcode[1] = (byte) 2;
			
			return this.Send(input, 0);
		}
		
		// 답변일 경우
		// cache table에 저장
		
		return true;
	}

	private boolean IsItReply(byte[] input) {
		// TODO Auto-generated method stub
		if(m_sHeader.opcode[0] == (byte)0 && m_sHeader.opcode[0] == (byte)2) {
			return true;
		}
		return false;
	}

	private boolean IsItMine(byte[] input) {
		// TODO Auto-generated method stub
		for (int i = 0; i < 6; i++) {
			if (m_sHeader.inet_srcaddr.addr[i] == input[24 + i])
				continue;
			else {
				return false;
			}
		}
		return true;
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
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
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		if (p_UnderLayer == null)
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
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}
}
