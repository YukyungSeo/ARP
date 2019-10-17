package arp;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

public class ARPDlg extends JFrame implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	String path;

	private static LayerManager m_LayerMgr = new LayerManager();
	int selected_index;
	private JTextField ChattingWrite;
	private JTextField FileDir_path;

	JComponent contentPane;

	JTextArea ChattingArea;
	JTextArea srcMacAddress;
	JTextArea dstMacAddress;

	JLabel lblSelectNic;
	JLabel lblsrc;
	JLabel lbldst;

	JButton Setting_Button;
	JButton File_select_Button;
	JButton Chat_send_Button;
	JButton NIC_select_Button;
	JButton File_send_Button;

	JComboBox comboBox;

	FileDialog fd;
	private JTextField tf_ip_addrass;

	public static void main(String[] args){
		m_LayerMgr.AddLayer(new NILayer("NI"));
		m_LayerMgr.AddLayer(new EthernetLayer("Ethernet"));
		m_LayerMgr.AddLayer(new ARPLayer("ARP"));
		m_LayerMgr.AddLayer(new IPLayer("IP"));
		m_LayerMgr.AddLayer(new TCPLayer("TCP"));
		m_LayerMgr.AddLayer(new ARPDlg("GUI"));

		m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *ARP +IP ( -ARP *TCP ( *GUI ) ) ) )");
	}

	public ARPDlg(String pName){
		pLayerName = pName;
		
		// SRC IP & MAC ADDRESS SETTING
		try {
			InetAddress ip = InetAddress.getLocalHost();
			NetworkInterface ni = NetworkInterface.getByInetAddress(ip);
			if (ni != null) {
				byte[] src_macAddress = ni.getHardwareAddress();
				((EthernetLayer) m_LayerMgr.GetLayer("Ethernet")).SetEnetSrcAddress(src_macAddress);
				((ARPLayer) m_LayerMgr.GetLayer("ARP")).SetEnetSrcAddress(src_macAddress);
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JLabel label_IP_Addrass = new JLabel("IP주소");
		contentPane.add(label_IP_Addrass, BorderLayout.WEST);
		
		tf_ip_addrass = new JTextField();
		contentPane.add(tf_ip_addrass, BorderLayout.CENTER);
		tf_ip_addrass.setColumns(10);
		
		JButton bt_send = new JButton("Send");
		bt_send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String tf_ipaddr = tf_ip_addrass.getText();
				System.out.println(tf_ipaddr);
				
				byte[] dstAddress = new byte[4];
				String[] byte_dst = tf_ipaddr.split("\\.");
				for (int i = 0; i < 4; i++) {
					dstAddress[i] = (byte) Integer.parseInt(byte_dst[i], 16);
				}
				((TCPLayer) m_LayerMgr.GetLayer("TCP")).SetInetDstAddress(dstAddress);
				((IPLayer) m_LayerMgr.GetLayer("IP")).SetIPDstAddress(dstAddress);
				((ARPLayer) m_LayerMgr.GetLayer("ARP")).SetInetDstAddress(dstAddress);
				
				
				try {
					String MyIPAddrass = InetAddress.getLocalHost().getHostAddress();
					byte[] srcAddress = new byte[4];
					String[] byte_src = MyIPAddrass.split("\\.");
					for (int i = 0; i < 4; i++) {
						srcAddress[i] = (byte) Integer.parseInt(byte_src[i], 16);
					}
					((TCPLayer) m_LayerMgr.GetLayer("TCP")).SetInetSrcAddress(srcAddress);
					((IPLayer) m_LayerMgr.GetLayer("IP")).SetIPSrcAddress(srcAddress);
					((ARPLayer) m_LayerMgr.GetLayer("ARP")).SetInetSrcAddress(srcAddress);
					((TCPLayer) m_LayerMgr.GetLayer("TCP")).Send(new byte[0], 0);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		contentPane.add(bt_send, BorderLayout.EAST);
		
		tf_ip_addrass = new JTextField();
		contentPane.add(tf_ip_addrass, BorderLayout.CENTER);
		tf_ip_addrass.setColumns(10);
		
		setVisible(true);
	}	
	
	public boolean Receive(byte[] input) {
		byte[] data = input;
		String Text = new String(data);
		ChattingArea.append("[RECV] : " + Text + "\n");
		return false;
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
	public String GetLayerName() {
		// TODO Auto-generated method stub
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
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}
}
