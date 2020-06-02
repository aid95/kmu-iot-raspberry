import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Map;
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

class FloraSensor {
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
		try {
			baseInfo = IMUtil.getBaseInfo("IoTSDK.properties");
			tcpConnector.init(callback, baseInfo);
			tcpConnector.connect(timeOut);	
			tcpConnector.authenticate(timeOut);

			while (true) {
				transID = IMUtil.getTransactionLongRoundKey4();
				tcpConnector.requestNumColecData("battery", floraSensor.getBattery(), new Date(), transID);
				tcpConnector.requestNumColecData("temperature", floraSensor.getTemperature(), new Date(), transID);
				tcpConnector.requestNumColecData("light", floraSensor.getLight(), new Date(), transID);
				tcpConnector.requestNumColecData("conductivity", floraSensor.getConductivity(), new Date(), transID);
				tcpConnector.requestNumColecData("moisture", floraSensor.getMoisture(), new Date(), transID);
				Thread.sleep(1000);
				floraSensor.updateSensorData();
			}
		} catch(SdkException e) {
			System.out.println("Code :" + e.getCode() + " Message :" + e.getMessage());
		}
    }

    @Override
    public void handleColecRes(Long transId, String respCd) {
		System.out.println("Collect Response. Transaction ID :" + transId + " Response Code : " + respCd);
    }

    @Override
    public void handleControlReq(Long transID, Map<String, Double> numberRows, Map<String, String> stringRows) {	
		System.out.println("Handle Control Request Called. Transaction ID : " + transID);
		System.out.println(numberRows.size()+" Number Type controls. " + stringRows.size() + " String Type controls.");
			
		if (numberRows.size() > 0) {
			for(String key : numberRows.keySet()) {
			System.out.println("Tag Stream :" + key + " Value:" + numberRows.get(key));
			}
		}

		// LED control value form IoTMakers...
		if(stringRows.size() > 0) {
			for (String key : stringRows.keySet()) {
				System.out.println("Tag Stream :" + key + " Value:" + stringRows.get(key));
				if ("LED".equals(key)) {
					if ("ON".equals(stringRows.get(key))) {
					} else if ("OFF".equals(stringRows.get(key))) {
					}
				}

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
