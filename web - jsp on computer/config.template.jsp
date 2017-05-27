<%
String sPACTL_SINK_DEVICE = "0";
String sPACTL_SINK_DEVICE_CHANNEL_TO_GET_VOLUME = "front-left";

//String sPACTL_SOURCE_DEVICE = "3";	// 蓝牙设备如果不断断开、不断接入，则序号会不断增加，则不能通过 Index 的方式来定位蓝牙 Source
String sPACTL_SOURCE_DEVICE_CHANNEL_TO_GET_VOLUME = "front-left";

int nMAX_ALLOWED_VOLUME_TO_ADJUST = 100;	//
int nMIN_ALLOWED_VOLUME_TO_ADJUST = 50;	//
int nVOLUME_ADJUST_STEP = 5;	//

boolean bALLOW_ADJUST_VOLUME_WHEN_WEARING_EARPHONE = false;

boolean bPLAY_BAIDU_TTS_WHEN_ADJUSTING_VOLUME = true;
String sBAIDU_TTS_APPID = "";
String sBAIDU_TTS_KEY = "";
%>
