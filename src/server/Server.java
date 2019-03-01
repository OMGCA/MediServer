package server;

import global.Patient;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {
	/* 初始化ServerSocket和Socket类，用于查询病人 */
	private static ServerSocket serverSocket = null;
	private static Socket socket = null;

	/* 初始化ServerSocket和Socket类，用于修改/新增病人 */
	private static ServerSocket modSocket = null;
	private static Socket mSocket = null;

	/* 定义病人查询和修改/新增使用的网络端口 */
	private static int queryPort = 34167;
	private static int modPort = 34168;

	/* 定义病人类列表 */
	private static List<Patient> patientList = null;
	/* 数据库文件位置 */
	private static String database = "patient.data";
	private static String logFileName = "Server Log.log";
	private static File logFile = new File(logFileName);

	public static void main(String[] args) throws Exception {
		/* 定义多线程，使得两个网络端口处于并行状态 */
		Server threadRun = new Server();

		/* 定义查询病人线程 */
		Server.SendPatient sendPatient = threadRun.new SendPatient();

		/* 定义修改/新增病人线程 */
		Server.ModPatient modPatient = threadRun.new ModPatient();

		/* 启动线程 */
		sendPatient.start();
		modPatient.start();
	}

	/* 查询病人线程定义 */
	class SendPatient extends Thread {
		public void run() {
			while (true) { // 无限循环，防止查询完成端口关闭后无法继续进行查询
				try {
					/* 初始化ServerSocket类 */
					serverSocket = new ServerSocket(queryPort);
					writeToLog(logFile, "Port " + queryPort + " is opened.");
					System.out.println("等待客户端请求");
					/* 等待客户端连接 */
					socket = serverSocket.accept();

					/* 从数据库导入病人信息 */
					importPatientInfo();
					String clientAddress = socket.getRemoteSocketAddress().toString();
					System.out.println("客户端" + clientAddress + " 已连接，端口 " + queryPort);
					writeToLog(logFile, clientAddress + " has connected to server");

					/* 定义InputStream，此方法中从客户端得到字段的输入，故使用InputStream */
					InputStream inStream = null;
					/* 得到客户端发来的字段 */
					inStream = socket.getInputStream();

					byte[] bytes = new byte[1024];
					int len;

					StringBuilder sb = new StringBuilder();
					while ((len = inStream.read(bytes)) != -1) {
						sb.append(new String(bytes, 0, len, "UTF-8"));
					}
					/* 通过接收到的从客户端发来的字段，筛选出将要发送至客户端的病人信息 */
					Patient patient = queryPatient(sb.toString());
					writeToLog(logFile, clientAddress + " has initialized a request on info of patient " + sb);
					System.out.println("收到NFC标签" + sb + "的信息请求");

					/* 定义ObjectOutputStream，输出为发送至客户端的Patient类，故使用ObjectOutputStream */
					ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
					/* 将第82行中筛选出的病人信息写入outStream */
					outStream.writeObject(patient);
					writeToLog(logFile, "Info of patient " + sb + " has been sent to " + clientAddress);
					System.out.println("已将患者" + patient.getName() + "信息发送至客户端");
					System.out.println("-------------------");

					/* 关闭所有端口 */
					inStream.close();
					outStream.close();
					socket.close();
					serverSocket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/* 修改/新增病人线程定义 */
	class ModPatient extends Thread {
		public void run() {
			while (true) { // 无限循环，防止查询完成端口关闭后无法继续进行查询
				try {
					/* 初始化ServerSocket类 */
					modSocket = new ServerSocket(modPort);
					writeToLog(logFile, "Port " + modPort + " is opened.");
					System.out.println("等待新的患者信息发送");
					mSocket = modSocket.accept();

					/* 等待客户端接入 */
					String clientAddress = socket.getRemoteSocketAddress().toString();
					System.out.println("客户端" + mSocket.getRemoteSocketAddress().toString() + "已连接至端口" + modPort);
					writeToLog(logFile, clientAddress + " has connected to server");

					/* 修改/新增病人需要接收客户端发送的Patient类，故使用ObjectInputStream */
					ObjectInputStream inStream = new ObjectInputStream(mSocket.getInputStream());
					/* 新建本地Patient类，存储客户端发送的Patient类 */
					Patient p = (Patient) inStream.readObject();
					writeToLog(logFile, clientAddress + " is attempting to modify info of patient " + p.getSlotID());
					System.out.println(
							"客户端" + mSocket.getRemoteSocketAddress().toString() + "已修改病人" + p.getName() + "信息");
					System.out.println("-------------------");

					/* 调用修改病人方法 */
					String msg = modPatient(p, p.getSlotID(), clientAddress);
					/* 同步数据库 */
					importPatientInfo();

					OutputStream outStream = mSocket.getOutputStream();
					mSocket.getOutputStream().write(msg.getBytes("UTF-8"));
					mSocket.shutdownOutput();

					/* 关闭端口 */
					inStream.close();
					outStream.close();
					mSocket.close();
					modSocket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/* 事件写入外部日志方法 */
	public static void writeToLog(File logFile, String contentType) {
		/* 获得时间戳 */
		String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss").format(new Date());
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), "UTF-8"));

			writer.write("[" + timestamp + "]" + "\t");
			writer.write(contentType + "\n");

			writer.flush();
			writer.close();

			System.gc();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* 导入病人信息方法 */
	public static void importPatientInfo() {
		/* 初始化patientList */
		patientList = new ArrayList<>();

		/* 文件位置为数据库 */
		File f = new File(database);
		/* 逗号分隔符 */
		String splitBy = (",");
		String line = "";

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
			while ((line = br.readLine()) != null) {
				/* 将数据库中每一行以逗号分隔存入String[]中 */
				String[] tmpArr = line.split(splitBy);
				/* 新建Patient类 */
				Patient tmpPatient = new Patient(tmpArr[0]);
				/* 详细记录新Patient信息后添加至patientList中 */
				registerPatient(tmpPatient, tmpArr);
			}
			new FileInputStream(f).close();
		} catch (Exception e) {
			System.out.println("PATIENT_IMPORT_ERROR: Specific data file can not be found or accessed.");
			e.printStackTrace();
		}
	}

	public static String modPatient(Patient p, String slotID, String clientAddress) {
		/* 新建临时文件 */
		File fTmp = new File("database.tmp");

		/* 跳过待修改床位信息 */
		for (int i = 0; i < patientList.size(); i++) {

			if (!patientList.get(i).getSlotID().equals(slotID)) {
				addPatient(patientList.get(i), fTmp);
			}
		}

		/* 加入待修改床位新信息 */
		addPatient(p, fTmp);

		File originalDatabase = new File(database);
		/* 垃圾回收，否则接下来的代码无法执行 */
		System.gc();

		/* 删除原始数据库 */
		if (originalDatabase.delete()) {
			System.out.println("正在更新数据库");
			writeToLog(logFile, "Updating database");
		}

		else {
			System.out.println("DATABASE_DELETE_ERROR: Fail to delete original database");
			writeToLog(logFile, "DATABASE_DELETE_ERROR: Fail to delete original database");
		}

		/* 重命名临时数据库 */
		if (fTmp.renameTo(originalDatabase)) {
			String success = "病人信息修改成功";
			System.out.println("数据库更新完成");
			writeToLog(logFile, "Database has been updated");
			writeToLog(logFile,
					"Info of patient " + p.getSlotID() + " has been successfully modified by " + clientAddress);
			return success;
		}

		else {
			String error = "DATABASE_UPDATE_ERROR: Fail to rename tmp database";
			System.out.println(error);
			writeToLog(logFile, error);
			writeToLog(logFile, "Info of patient " + p.getSlotID() + " modification operation has failed, attempted by "
					+ clientAddress);
			return error;
		}
	}

	/* 新增病人至数据库方法 */
	public static void addPatient(Patient p, File f) {
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8"));

			writer.write(p.getName() + ",");
			writer.write(p.getAge() + ",");
			writer.write(p.getSex() + ",");
			writer.write(p.getSlotID() + ",");
			writer.write(p.getDoc() + ",");
			writer.write(p.getBodyTemp() + ",");
			writer.write(p.getPulse() + ",");
			writer.write(p.getBreath() + ",");
			writer.write(p.getReleasePressure() + ",");
			writer.write(p.getTensePressure() + ",");
			writer.write(p.getBgAnalysis() + ",");
			writer.write(p.getBloodNa() + ",");
			writer.write(p.getBloodK() + ",");
			writer.write(p.getOpPending() + ",");
			writer.write(p.getPathologyResult() + ",");
			writer.write(p.getImaging() + ",");
			writer.write(p.getECG() + ",");
			writer.write(p.getDocNote() + ",");
			writer.write("\r\n");

			writer.flush();
			writer.close();

			new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8").close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* 注册病人至patientList方法 */
	public static void registerPatient(Patient p, String[] infoStream) {
		/* 从数据库写入病人信息 */
		p.setAge(Integer.parseInt(infoStream[1]));
		p.setSex(infoStream[2]);
		p.setSlotID(infoStream[3]);
		p.setDoc(infoStream[4]);
		p.setBodyTemp(Double.parseDouble(infoStream[5]));
		p.setPulse(Integer.parseInt(infoStream[6]));
		p.setBreath(Integer.parseInt(infoStream[7]));
		p.setReleasePressure(Integer.parseInt(infoStream[8]));
		p.setTensePressure(Integer.parseInt(infoStream[9]));
		p.setBgAnalysis(Double.parseDouble(infoStream[10]));
		p.setBloodNa(Integer.parseInt(infoStream[11]));
		p.setBloodK(Double.parseDouble(infoStream[12]));
		p.setOpPending(infoStream[13]);
		p.setPathologyResult(infoStream[14]);
		p.setImaging(infoStream[15]);
		p.setECG(infoStream[16]);
		p.setDocNote(infoStream[17]);
		/* 添加至patientList中 */
		patientList.add(p);
	}

	/* 筛选病人信息 */
	public static Patient queryPatient(String ID) {
		/* 线性搜索patientList */
		for (int i = 0; i < patientList.size(); i++) {
			/* 当查询的ID符合某行病人ID时返回该病人，筛选结束 */
			if (patientList.get(i).getSlotID().equals(ID)) {
				return patientList.get(i);
			}
		}
		/* 查询不到相关病人信息时则返回空的Patient类 */
		return new Patient("");
	}
}
