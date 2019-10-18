package arp;

import java.util.ArrayList;
import java.util.Iterator;
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
	public CircularArrayQueue<_ENTRY> queue_arpTable;

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

	private class TimeThread extends Thread {
		public void run() {
			try {
				Thread.sleep(10000); // 10분 후로 교체 해줘야함
				System.out.println(queue_arpTable.deQueue());
			} catch (InterruptedException e) {
				System.out.println(e.getMessage()); // 오류 출력(방법은 여러가지)
			}
		}
	}

	public class _ENTRY {
		_INTERNET_ADDR inet_addr;
		_ETHERNET_ADDR enet_addr;

		public _ENTRY(_INTERNET_ADDR inet_addr, _ETHERNET_ADDR enet_addr) {
			this.inet_addr = inet_addr;
			this.enet_addr = enet_addr;
		}
	}

	private class _ARP_Header {
		byte[] hw_type; // hw type
		byte[] proto_type; // protocal type
		byte length_inetaddr; // ip 주소 길이
		byte length_enetaddr; // mac 주소 길이
		byte[] opcode; // arp message가 request 이면 1, reply 이면 2
		_ETHERNET_ADDR enet_srcaddr; // 본인 mac 주소
		_INTERNET_ADDR inet_srcaddr; // 본인 ip 주소
		_ETHERNET_ADDR enet_dstaddr; // 상대방 mac 주소
		_INTERNET_ADDR inet_dstaddr; // 상대방 ip 주소

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
		queue_arpTable = new CircularArrayQueue<_ENTRY>(10);
	}

	public void SetInetDstAddress(byte[] input) {
		for (int i = 0; i < 4; i++) {
			m_sHeader.inet_dstaddr.addr[i] = input[i];
		}
	}

	public void SetInetSrcAddress(byte[] input) {
		for (int i = 0; i < 4; i++) {
			m_sHeader.inet_srcaddr.addr[i] = input[i];
		}
	}

	public void SetEnetSrcAddress(byte[] input) {
		// TODO Auto-generated method stub
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_srcaddr.addr[i] = input[i];
		}
	}

	private void SetEnetDstAddress(byte[] input) {
		// TODO Auto-generated method stub
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_dstaddr.addr[i] = input[i];
		}
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

	private boolean inQueue(byte[] input) {
		// TODO Auto-generated method stub
		// queue에 해당 mac 주소가 있는지 확인
		byte[] buf = new byte[4];
		// input에서 dst mac 주소 꺼냄
		for (int i = 0; i < 4; i++) {
			buf[i] = input[i];
		}
		// table에 존재하는지 확인
		for (int i = 0; i < this.queue_arpTable.size(); i++) {
			_ENTRY entry = queue_arpTable.elementAt(i);
			for (int j = 0; j < 4; j++) {
				if (entry.inet_addr.addr[j] == buf[j])
					return true;
			}
		}
		return false;
	}

	public boolean Send(byte[] input, int length) {
		if (inQueue(input) == false) {
			// dst address 에 ff.ff.ff.ff 넣어주기
			byte[] broadcast_dst_macAddress = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
					(byte) 0xff };
			((EthernetLayer) GetUnderLayer()).SetEnetDstAddress(broadcast_dst_macAddress);
			this.SetEnetDstAddress(broadcast_dst_macAddress);
			byte[] bytes = this.ObjToByte(m_sHeader, input, length);

			// 만약 여기에 ???? 도 넣는다면... 잘못된 ip여서 답장이 안오면 어떡하지..?

			this.GetUnderLayer().Send(bytes, 28);
			return true;
		}
		this.GetUnderLayer().Send(input, length);
		return true;
	}

	private byte[] ObjToByte(_ARP_Header Header, byte[] input, int length) {
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
		// 상대방의 enet과 inet 주소를 저장
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_dstaddr.addr[i] = input[8 + i];
		}
		for (int i = 0; i < 4; i++) {
			m_sHeader.inet_dstaddr.addr[i] = input[14 + i];
		}

		if (IsItReply(input) == false) {
			// 1. 답변일 경우
			// cache table에 저장
			_ENTRY newEntry = new _ENTRY(m_sHeader.inet_dstaddr, m_sHeader.enet_dstaddr);
			this.queue_arpTable.enQueue(newEntry);
			new TimeThread().start();

			return true;
		} else {
			// 2. 요청일 경우
			m_sHeader.opcode[0] = (byte) 0;
			m_sHeader.opcode[1] = (byte) 2;

			// swapping & 재전송
			return this.Send(input, 0);
		}

	}

	private boolean IsItReply(byte[] input) {
		// TODO Auto-generated method stub
		if (m_sHeader.opcode[0] == (byte) 0 && m_sHeader.opcode[0] == (byte) 2) {
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
