import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.kt.smcp.gw.ca.comm.exception.SdkException;
import com.kt.smcp.gw.ca.gwfrwk.adap.stdsys.sdk.tcp.BaseInfo;
import com.kt.smcp.gw.ca.gwfrwk.adap.stdsys.sdk.tcp.IMTcpConnector;
import com.kt.smcp.gw.ca.gwfrwk.adap.stdsys.sdk.tcp.LogIf;
import com.kt.smcp.gw.ca.util.IMUtil;

public class SampleLight {
	public static void main(String[] args) {
		LogIf callback = new LogIf();
		IMTcpConnector imTcpConnector = new IMTcpConnector();
		BaseInfo baseInfo = null;
		Long transID;
		Long timeOut = (long) 3000;

		try {
			// 초기화, 연결 및 인증
			baseInfo = IMUtil.getBaseInfo("IoTSDK.properties");
			imTcpConnector.activate(callback, baseInfo, timeOut);						
			// 숫자형 단건 전송
			transID = IMUtil.getTransactionLongRoundKey4();
			imTcpConnector.requestNumColecData("temperature", 22.0, new Date(), transID);
			// 문자형 단건 전송
			transID = IMUtil.getTransactionLongRoundKey4();
			imTcpConnector.requestStrColecData("LED", "ON", new Date(), transID);
			// 숫자형 다건 전송
			transID = IMUtil.getTransactionLongRoundKey4();
			Map<String, Double> numberRows = new HashMap<String, Double>();
			numberRows.put("latitude", 20.2);
			numberRows.put("longitude", 23.8);
			imTcpConnector.requestNumColecDatas(numberRows, new Date(), transID);
			// 문자형 다건 전송
			transID = IMUtil.getTransactionLongRoundKey4();
			Map<String, String> stringRows = new HashMap<String, String>();
			stringRows.put("power", "on");
			stringRows.put("speed", "high");
			imTcpConnector.requestStrColecDatas(stringRows, new Date(), transID);
			// 복합형(문자+숫자) 다건 전송
			transID = IMUtil.getTransactionLongRoundKey4();
			imTcpConnector.requestColecDatas(numberRows, stringRows, new Date(), transID);
			// 해제 및 종료
			imTcpConnector.deactivate();
		} catch (SdkException e) {
			System.out.println("Code :" + e.getCode() + " Message :" + e.getMessage());
		}
	}
}
