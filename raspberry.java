import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import com.kt.smcp.gw.ca.comm.exception.SdkException;
import com.kt.smcp.gw.ca.gwfrwk.adap.stdsys.sdk.tcp.BaseInfo;
import com.kt.smcp.gw.ca.gwfrwk.adap.stdsys.sdk.tcp.IMCallback;
import com.kt.smcp.gw.ca.gwfrwk.adap.stdsys.sdk.tcp.IMTcpConnector;
import com.kt.smcp.gw.ca.util.IMUtil;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;

class FloraSensor 
{
	private Double light;
	private Double battery;
	private Double temperature;
	private Double moisture;
	private Double conductivity;

	private String floraMacAddress;

	FloraSensor(String mac) {
		floraMacAddress = mac;
	}

	public void updateSensorData() throws Exception {
		Runtime run = Runtime.getRuntime();
		Process proc= run.exec("sudo python3 demo.py --backend bluepy poll " + floraMacAddress);
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

		String s = null;
		String sOut = "";
		while((s = stdInput.readLine()) != null) {
			sOut = sOut + s;
		}

		if (!sOut.isEmpty() && !(sOut.contains("Error") || sOut.contains("Traceback"))) {
			String[] sensorData = sOut.split(":");
			temperature 	= Double.parseDouble(sensorData[0]);
			moisture 		= Double.parseDouble(sensorData[1]);
			light 			= Double.parseDouble(sensorData[2]);
			conductivity 	= Double.parseDouble(sensorData[3]);
			battery 		= Double.parseDouble(sensorData[4]);
		}
	}

	public Double getBattery() { return battery; }
	public Double getTemperature() { return temperature; }
	public Double getLight() { return light; };
	public Double getConductivity() { return conductivity; }
	public Double getMoisture() { return moisture; }
}

public class raspberry extends IMCallback  
{
	private static FloraSensor floraSensor = null;

    public raspberry() {
		floraSensor = new FloraSensor("C4:7C:8D:66:49:DC");
    }

    public static void main(String[] args) throws Exception {
		// callback fuction call...
		raspberry callback = new raspberry();
		IMTcpConnector tcpConnector = new IMTcpConnector();
		BaseInfo baseInfo = null;
		
		Long transID;
		Long timeOut = (long)3000;
		Map<String,Double> numberRows = new HashMap<String, Double>();

		try {
			baseInfo = IMUtil.getBaseInfo("IoTSDK.properties");
			tcpConnector.init(callback, baseInfo);
			tcpConnector.connect(timeOut);	
			tcpConnector.authenticate(timeOut);

			while (true) {
				transID = IMUtil.getTransactionLongRoundKey4();
				numberRows.put("battery", floraSensor.getBattery());
				numberRows.put("temperature", floraSensor.getTemperature());
				numberRows.put("light", floraSensor.getLight());
				numberRows.put("conductivity", floraSensor.getConductivity());
				numberRows.put("moisture", floraSensor.getMoisture());
				tcpConnector.requestNumColecDatas(numberRows, new Date(), transID);
				floraSensor.updateSensorData();
				numberRows.clear();
				Thread.sleep(1000);
			}
		} catch(SdkException e) {
			System.out.println("Code :" + e.getCode() + " Message :" + e.getMessage());
		}
    }

    @Override
    public void handleColecRes(Long transId, String respCd) {
    }

    @Override
    public void handleControlReq(Long transID, Map<String, Double> numberRows, Map<String, String> stringRows) {	
		if(stringRows.size() > 0) {
			for (String key : stringRows.keySet()) {
				switch (key) {
					case "battery":
						break;
					default:
						break;
				}
			}
		}
    }
}
