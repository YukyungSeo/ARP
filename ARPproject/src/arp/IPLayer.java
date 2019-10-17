package arp;

import java.util.ArrayList;

public class IPLayer implements BaseLayer {
	
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	private class _IP_ADDR {
		private byte[] addr = new byte[4];

		public _IP_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
		}
	}
	
	private class _IP_HEADER{
		
		_IP_ADDR IP_dstaddr;
		_IP_ADDR IP_srcaddr;
		
		byte[] IP_len;
		byte[] IP_id;
		byte[] IP_fragoff;
		byte[] IP_cksum;
		byte[] IP_data;

		byte IP_verlen;
		byte IP_tos;
		byte IP_ttl;
		byte IP_proto;
		
		public _IP_HEADER(){
			this.IP_dstaddr = new _IP_ADDR();
			this.IP_srcaddr = new _IP_ADDR();
			
			this.IP_len = new byte[2];
			this.IP_id = new byte[2];
			this.IP_fragoff = new byte[2];
			this.IP_cksum = new byte[2];
			
			
			this.IP_tos = (byte)0x00;
			this.IP_verlen = (byte)0x00;
			this.IP_ttl = (byte)0x00;
			this.IP_proto = (byte)0x80;
			this.IP_data = null;

		}
	}
	
	_IP_HEADER m_sHeader = new _IP_HEADER();
	
	public void ResetHeader() {
		for (int i = 0; i < 3; i++) {
			m_sHeader.IP_dstaddr.addr[i] = (byte) 0x00;
			m_sHeader.IP_srcaddr.addr[i] = (byte) 0x00;
		}
		m_sHeader.IP_tos = (byte)0x00;
		m_sHeader.IP_verlen = (byte)0x00;
		m_sHeader.IP_ttl = (byte)0x00;
		m_sHeader.IP_proto = (byte) 0x00;
		m_sHeader.IP_data = null;
	}
	
	public IPLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}
	
	public _IP_ADDR GetEnetDstAddress() {
		return m_sHeader.IP_dstaddr;
	}

	public _IP_ADDR GetEnetSrcAddress() {
		return m_sHeader.IP_srcaddr;
	}
	
	public void SetIPDstAddress(byte[] input) {
		for (int i = 0; i < 4; i++) {
			m_sHeader.IP_dstaddr.addr[i] = input[i];
		}
	}

	public void SetIPSrcAddress(byte[] input) {
		for (int i = 0; i < 4; i++) {
			m_sHeader.IP_srcaddr.addr[i] = input[i];
		}
	}
	
	public byte[] ObjToByte(_IP_HEADER Header, byte[] input, int length) {
		byte[] buf = new byte[length + 20];

		buf[0] = Header.IP_dstaddr.addr[0];
		buf[1] = Header.IP_dstaddr.addr[1];
		buf[2] = Header.IP_dstaddr.addr[2];
		buf[3] = Header.IP_dstaddr.addr[3];

		buf[4] = Header.IP_srcaddr.addr[0];
		buf[5] = Header.IP_srcaddr.addr[1];
		buf[6] = Header.IP_srcaddr.addr[2];
		buf[7] = Header.IP_srcaddr.addr[3];
		
		buf[8] = Header.IP_len[0];
		buf[9] = Header.IP_len[1];
		
		buf[10] = Header.IP_id[0];
		buf[11] = Header.IP_id[1];
		
		buf[12] = Header.IP_fragoff[0];
		buf[13] = Header.IP_fragoff[1];
		
		buf[14] = Header.IP_cksum[0];
		buf[15] = Header.IP_cksum[1];
		
		buf[16] = Header.IP_tos;
		buf[17] = Header.IP_verlen;
		buf[18] = Header.IP_ttl;
		buf[19] = Header.IP_proto;


		for (int i = 0; i < length; i++)
			buf[20 + i] = input[i];

		return buf;
	}
	
	public boolean Send(byte[] input, int length) {
		byte[] bytes = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(bytes, length + 20);

		return false;
	}

	public byte[] RemoveIPHeader(byte[] input, int length) {
		byte[] data = new byte[length - 20];
		for (int i = 0; i < length - 20; i++)
			data[i] = input[20 + i];
		return data;
	}

	public boolean IsItMyPacket(byte[] input) {
		for (int i = 0; i < 4; i++) {
			if (m_sHeader.IP_srcaddr.addr[i] == input[4 + i])
				continue;
			else
				return false;
		}
		return true;
	}

	public boolean IsItMine(byte[] input) {
		for (int i = 0; i < 4; i++) {
			if (m_sHeader.IP_dstaddr.addr[i] == input[i])
				continue;
			else {
				return false;
			}
		}
		return true;
	}

	public boolean Receive(byte[] input) {
		byte[] data;
		boolean MyPacket, Mine;
		MyPacket = IsItMyPacket(input);

		if (MyPacket == true){
			//���� ���� ��Ŷ�̸� �������� ����.
			return false;
		}else {
				Mine = IsItMine(input);
				if (Mine == false){
					// �������� �ڽ��� �ƴϸ� �������� ����.
					return false;
				}
			}
		
			data = RemoveIPHeader(input, input.length);
			this.GetUpperLayer(0).Receive(data);
		
		return true;
	}
	
	@Override
	public String GetLayerName() {
		return pLayerName;
	}


	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
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
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}
}
