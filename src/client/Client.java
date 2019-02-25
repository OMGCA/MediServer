package client;

import java.io.OutputStream;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import global.Patient;

public class Client {
	static Font xtDefault = new Font("Microsoft Yahei", Font.PLAIN, 14);

	public static void main(String args[]) throws Exception {
		/* 设定画面抗锯齿 */
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
		GUISetup();
	}

	/* 查询病人方法，对应SocketServer中的SendPatient */
	public static Patient retrievePatient(String host, int port, String id) throws Exception {
		/* 初始化Socket，根据服务器地址和端口地址 */
		Socket socket = new Socket(host, port);

		/* 准备发送至服务器的字段信息 */
		OutputStream outStream = socket.getOutputStream();
		socket.getOutputStream().write(id.getBytes("UTF-8"));
		/* 发送完成后关闭发送通道 */
		socket.shutdownOutput();

		/* 准备接收从服务器发来的Patient类 */
		ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

		Thread.sleep(50);
		/* 接收到后新建Patient类用于接收 */
		Patient patient = (Patient) inStream.readObject();

		/* 关闭端口 */
		inStream.close();
		outStream.close();
		socket.close();

		return patient;
	}

	/* 上传新Patient方法，对应SocketServer中的ModPatient */
	public static void submitNewPatient(String host, int port, Patient p) throws Exception {
		Socket socket = new Socket(host, port);

		/* 准备发送至服务器的Patient类 */
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		/* 将参数中的Patient写入ObjectOutputStream，并发送至服务器 */
		outStream.writeObject(p);

		/* 端口关闭 */
		outStream.close();
		socket.close();
	}

	public static void GUISetup() {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* 主界面 */
		JFrame frame = new JFrame();
		frame.setTitle("信息查询客户端");
		frame.setFont(xtDefault);
		frame.setLayout(new GridBagLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;

		JLabel server = new JLabel("服务器地址");
		server.setFont(xtDefault);
		frame.add(server, c);

		c.gridx = 1;
		JTextField serverAddress = new JTextField(10);
		serverAddress.setFont(xtDefault);
		/* 默认服务器地址为OFCR阿里云服务器 */
		/* 若连接至本机运行的服务器，改为localhost */
		serverAddress.setText("120.78.160.93");
		frame.add(serverAddress, c);

		JLabel port = new JLabel("端口");
		c.gridx = 0;
		c.gridy = 1;
		port.setFont(xtDefault);
		frame.add(port, c);

		c.gridx = 1;
		JTextField portValue = new JTextField(10);
		portValue.setFont(xtDefault);
		portValue.setText("34167");
		frame.add(portValue, c);

		c.gridx = 0;
		c.gridy = 2;
		JLabel tagID = new JLabel("查询ID");
		tagID.setFont(xtDefault);
		frame.add(tagID, c);

		c.gridx = 1;
		JTextField tagIDInput = new JTextField(10);
		tagIDInput.setFont(xtDefault);
		tagIDInput.setText("1");
		frame.add(tagIDInput, c);

		c.gridx = 0;
		c.gridy = 3;
		JButton query = new JButton("查询患者信息");
		query.setFont(xtDefault);
		query.setVisible(true);
		frame.add(query, c);

		frame.setSize(600, 300);

		/* 点击查询患者信息按钮后 */
		query.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					/* 从服务器得到Patient后 */
					Patient tmpPatient = retrievePatient(serverAddress.getText(), Integer.parseInt(portValue.getText()),
							tagIDInput.getText());
					/* 显示Patient信息的界面 */
					JFrame patientInfo = new JFrame("患者" + tmpPatient.getSlotID() + "信息");

					patientInfo.setFont(xtDefault);
					patientInfo.setLayout(new GridBagLayout());
					patientInfo.setVisible(true);

					GridBagConstraints c = new GridBagConstraints();
					c.fill = GridBagConstraints.HORIZONTAL;
					c.gridx = 0;
					c.gridy = 0;

					String[] demographics = { "姓名", "年龄", "性别", "识别号", "主治医师" };
					String[] demograInfo = { tmpPatient.getName(), String.valueOf(tmpPatient.getAge()),
							tmpPatient.getSex(), tmpPatient.getSlotID(), tmpPatient.getDoc() };

					JLabel demo = new JLabel("个人信息");
					demo.setFont(xtDefault);
					patientInfo.add(demo, c);

					JTextField[] demoField = new JTextField[demographics.length];
					JLabel[] demoLabel = new JLabel[demographics.length];

					for (int i = 0; i < demographics.length; i++) {
						demoLabel[i] = new JLabel(demographics[i]);
						demoLabel[i].setFont(xtDefault);
						c.gridx = 0;
						c.gridy = i + 1;
						patientInfo.add(demoLabel[i], c);

						demoField[i] = new JTextField(10);
						demoField[i].setFont(xtDefault);
						demoField[i].setText(demograInfo[i]);
						demoField[i].setEditable(false);
						c.gridx = 1;
						patientInfo.add(demoField[i], c);
					}

					c.gridx = 0;
					c.gridy++;

					JLabel numericLabel = new JLabel("病理数据");
					numericLabel.setFont(xtDefault);
					patientInfo.add(numericLabel, c);

					String[] numeric = { "体温", "脉搏", "呼吸", "血压舒张压", "血压收缩压", "血气分析", "血钠", "血钾" };
					String[] numericValue = { String.valueOf(tmpPatient.getBodyTemp()),
							String.valueOf(tmpPatient.getPulse()), String.valueOf(tmpPatient.getBreath()),
							String.valueOf(tmpPatient.getReleasePressure()),
							String.valueOf(tmpPatient.getTensePressure()), String.valueOf(tmpPatient.getBgAnalysis()),
							String.valueOf(tmpPatient.getBloodNa()), String.valueOf(tmpPatient.getBloodK()) };
					String[] numericUnit = { "℃", "/min", "/min", "mmHg", "mmHg", "pH", "mmol/L", "mmol/L" };

					JTextField[] dataField = new JTextField[numeric.length];
					JLabel[] dataLabel = new JLabel[numeric.length];
					JLabel[] unitLabel = new JLabel[numeric.length];

					for (int i = 0; i < numeric.length; i++) {
						dataLabel[i] = new JLabel(numeric[i]);
						dataLabel[i].setFont(xtDefault);
						c.gridx = 0;
						c.gridy++;
						patientInfo.add(dataLabel[i], c);

						dataField[i] = new JTextField(10);
						dataField[i].setText(numericValue[i]);
						dataField[i].setEditable(false);
						dataField[i].setFont(xtDefault);
						c.gridx = 1;
						patientInfo.add(dataField[i], c);

						unitLabel[i] = new JLabel(numericUnit[i]);
						unitLabel[i].setFont(xtDefault);
						c.gridx = 2;
						patientInfo.add(unitLabel[i], c);
					}

					c.gridx = 0;
					c.gridy++;

					JLabel textLabel = new JLabel("其他信息");
					textLabel.setFont(xtDefault);
					patientInfo.add(textLabel, c);

					String[] textNotes = { "手术类型", "病理报告", "影像学结果", "心电图报告", "医嘱" };
					String[] noteContent = { tmpPatient.getOpPending(), tmpPatient.getPathologyResult(),
							tmpPatient.getImaging(), tmpPatient.getECG(), tmpPatient.getDocNote() };

					JLabel[] noteLabel = new JLabel[textNotes.length];
					JTextField[] noteField = new JTextField[textNotes.length];

					for (int i = 0; i < textNotes.length; i++) {
						c.gridx = 0;
						c.gridy++;
						noteLabel[i] = new JLabel(textNotes[i]);
						noteLabel[i].setFont(xtDefault);
						patientInfo.add(noteLabel[i], c);

						c.gridx = 1;
						noteField[i] = new JTextField(10);
						noteField[i].setFont(xtDefault);
						noteField[i].setText(noteContent[i]);
						noteField[i].setEditable(false);
						patientInfo.add(noteField[i], c);
					}

					JButton edit = new JButton("编辑");
					JButton submit = new JButton("上传至服务器");
					edit.setFont(xtDefault);
					submit.setFont(xtDefault);
					c.gridx = 0;
					c.gridy++;
					patientInfo.add(edit, c);
					c.gridx = 1;
					patientInfo.add(submit, c);

					edit.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							for (int i = 0; i < demoField.length; i++) {
								demoField[i].setEditable(true);
							}
							for (int i = 0; i < dataField.length; i++) {
								dataField[i].setEditable(true);
							}
							for (int i = 0; i < noteField.length; i++) {
								noteField[i].setEditable(true);
							}
						}
					});

					submit.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								Patient newPatient = new Patient(demoField[0].getText());
								newPatient.setAge(Integer.parseInt(demoField[1].getText()));
								newPatient.setSex(demoField[2].getText());
								newPatient.setSlotID(demoField[3].getText());
								newPatient.setDoc(demoField[4].getText());

								newPatient.setBodyTemp(Double.parseDouble(dataField[0].getText()));
								newPatient.setPulse(Integer.parseInt(dataField[1].getText()));
								newPatient.setBreath(Integer.parseInt(dataField[2].getText()));
								newPatient.setReleasePressure(Integer.parseInt(dataField[3].getText()));
								newPatient.setTensePressure(Integer.parseInt(dataField[4].getText()));
								newPatient.setBgAnalysis(Double.parseDouble(dataField[5].getText()));
								newPatient.setBloodNa(Integer.parseInt(dataField[6].getText()));
								newPatient.setBloodK(Double.parseDouble(dataField[7].getText()));

								newPatient.setOpPending(noteField[0].getText());
								newPatient.setPathologyResult(noteField[1].getText());
								newPatient.setImaging(noteField[2].getText());
								newPatient.setECG(noteField[3].getText());
								newPatient.setDocNote(noteField[4].getText());

								submitNewPatient(serverAddress.getText(), 34168, newPatient);
							} catch (Exception e2) {
								e2.printStackTrace();
							}
						}
					});

					patientInfo.setSize(600, 650);

				} catch (Exception e1) {
					JFrame warningFrame = new JFrame();
					warningFrame.setTitle("警告");
					warningFrame.setSize(200, 120);
					warningFrame.setVisible(true);
					warningFrame.setLayout(new GridBagLayout());
					JLabel label = new JLabel("服务器连接失败");
					label.setFont(xtDefault);

					GridBagConstraints c = new GridBagConstraints();
					c.fill = GridBagConstraints.HORIZONTAL;
					c.gridx = 0;
					c.gridy = 0;

					warningFrame.add(label, c);
				}
			}
		});

	}

}
