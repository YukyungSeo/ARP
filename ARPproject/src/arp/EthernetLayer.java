package arp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class EthernetLayer implements BaseLayer {
	// /lecture01_under_fall2019_Chatting&File.pdf
	// P.16
	// 혹은 주신 코드에 있음
	// import되어 있는 것을 사용하면서 다시 짜보면 어떨까
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

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

	private class _ETHERNET_HEADER {
		_ETHERNET_ADDR enet_dstaddr;
		_ETHERNET_ADDR enet_srcaddr;
		byte[] enet_type;
		byte[] enet_data; // 이건 안쓴다고 했던거 같음

		public _ETHERNET_HEADER() {
			this.enet_dstaddr = new _ETHERNET_ADDR();
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.enet_type = new byte[2];
			this.enet_data = null;
		}
	}

	_ETHERNET_HEADER m_sHeader = new _ETHERNET_HEADER();

	public EthernetLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;

	}

	// getter & setter
	public void SetEnetSrcAddress(byte[] input) {
		// TODO Auto-generated method stub
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_srcaddr.addr[i] = input[i];
		}
	}

	public void SetEnetDstAddress(byte[] input) {
		// TODO Auto-generated method stub
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_dstaddr.addr[i] = input[i];
		}
	}

	public void SetEnetType(byte[] input) {
		// TODO Auto-generated method stub
		for (int i = 0; i < 2; i++) {
			m_sHeader.enet_type[i] = input[i];
		}
	}

	public boolean Send(byte[] input, int length) {
		// 상위 계층에서 받은 것을 하위 계층으로 보내는 것
		byte[] bytes = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(bytes, length + 14);

		return false;
	}

	private byte[] ObjToByte(_ETHERNET_HEADER m_sHeader, byte[] input, int length) {
		// 상위 레이어에서 내려온 데이터에 EthernetLayer Header를 붙이는 함수
		byte[] buf = new byte[length + 14];

		buf[0] = m_sHeader.enet_dstaddr.addr[0];
		buf[1] = m_sHeader.enet_dstaddr.addr[1];
		buf[2] = m_sHeader.enet_dstaddr.addr[2];
		buf[3] = m_sHeader.enet_dstaddr.addr[3];
		buf[4] = m_sHeader.enet_dstaddr.addr[4];
		buf[5] = m_sHeader.enet_dstaddr.addr[5];

		buf[6] = m_sHeader.enet_srcaddr.addr[0];
		buf[7] = m_sHeader.enet_srcaddr.addr[1];
		buf[8] = m_sHeader.enet_srcaddr.addr[2];
		buf[9] = m_sHeader.enet_srcaddr.addr[3];
		buf[10] = m_sHeader.enet_srcaddr.addr[4];
		buf[11] = m_sHeader.enet_srcaddr.addr[5];

		buf[12] = m_sHeader.enet_type[0];
		buf[13] = m_sHeader.enet_type[1];

		for (int i = 0; i < length; i++)
			buf[14 + i] = input[i];

		return buf;
	}

	public boolean Receive(byte[] input) {
		// (어디선지는 모르지만)에서 받은 것을 상위 계층으로 보내는 것
		byte[] data;
		boolean MyPacket, Mine, Broadcast;
		MyPacket = IsItMyPacket(input);

		if (MyPacket == true) {
			// 내가 만든 패킷이면 수신하지 않음
			return false;
		} else {
			Broadcast = IsItBroadcast(input);
			if (Broadcast == false) {
				// 브로드 케스팅도 아니면서,
				Mine = IsItMine(input);
				if (Mine == false) {
					// 목적지가 자신이 아니면 수신하지 않음.
					return false;
				}
			}
		}

		// application으로 보내는 것 == 상위 계층으로 보냄
		// 0x0820 = ChatAppLayer;
		// 0x0830 = FileAppLayer;
		if (input[12] == 0x08 && input[13] == 0x20) {
			data = RemoveEtherHeader(input, input.length);
			this.GetUpperLayer(0).Receive(data);
		} else if (input[12] == 0x08 && input[13] == 0x30) {
			data = RemoveEtherHeader(input, input.length);
			this.GetUpperLayer(1).Receive(data);
		}

		return true;
	}

	private byte[] RemoveEtherHeader(byte[] input, int length) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean IsItMine(byte[] input) {
		// TODO Auto-generated method stub
		// 나에게 직접적으로 왔는가?
		return false;
	}

	private boolean IsItBroadcast(byte[] input) {
		// TODO Auto-generated method stub
		// 브로드케스팅인지 확인
		// ARP에서 다발적으로 보낸 것인지 확인
		return false;
	}

	private boolean IsItMyPacket(byte[] input) {
		// TODO Auto-generated method stub
		// 이것이 내가 보낸 packet인가?
		return false;
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
