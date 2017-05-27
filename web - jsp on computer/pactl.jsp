<%@ page pageEncoding="UTF-8" contentType="application/json"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.*"%>

<%@ page import="org.apache.commons.lang3.*"%>

<%@ page import="com.fasterxml.jackson.core.*"%>
<%@ page import="com.fasterxml.jackson.databind.*"%>
<%@ page import="com.fasterxml.jackson.databind.node.*"%>

<%@ page import="net.maclife.pactl.*"%>
<%@ page import="com.chinamotion.database.*"%>

<%!
void GetSinkVolume (String sSinkIndex, String sChannel, ObjectNode jsonResult)
{
	Map<String, Object> mapResult = PactlWrapper.pactl_list ("sinks");
	String sVolume = PactlWrapper.GetSinkVolumeInPercentage (mapResult, sSinkIndex, sChannel);
	jsonResult.put ("sink_volume", sVolume);

	String sKeyName_SinkDescription = "Sink #" + sSinkIndex + ".Description";
	jsonResult.put ("sink_name", (String)mapResult.get(sKeyName_SinkDescription));

	String sKeyName_ActivePort = "Sink #" + sSinkIndex + ".Active Port";
	String sActivePort = (String)mapResult.get(sKeyName_ActivePort);
	String sKeyName_ActivePortName = "Sink #" + sSinkIndex + ".Ports." + sActivePort + ".name";
	String sActivePortName = (String)mapResult.get(sKeyName_ActivePortName);
	jsonResult.put ("sink_active_port_name", sActivePortName);
	boolean bEarphonePlugged = false;
	if (StringUtils.isNotEmpty (sActivePort) && StringUtils.endsWithIgnoreCase(sActivePort, "-headphones"))
	{
		bEarphonePlugged = true;
	}
	jsonResult.put ("earphone_plugged_in", bEarphonePlugged);
}

void GetSourceVolume (String sSourceIndex, String sChannel, ObjectNode jsonResult)
{
	Map<String, Object> mapResult = PactlWrapper.pactl_list ("sources");
	String sVolume = PactlWrapper.GetSourceVolumeInPercentage (mapResult, sSourceIndex, sChannel);
	jsonResult.put ("source_volume", sVolume);

	String sKeyName_SourceDescription = "Source #" + sSourceIndex + ".Description";
	jsonResult.put ("source_name", (String)mapResult.get (sKeyName_SourceDescription));

	String sKeyName_ActivePort = "Source #" + sSourceIndex + ".Active Port";
	String sActivePort = (String)mapResult.get(sKeyName_ActivePort);
	String sKeyName_ActivePortName = "Source #" + sSourceIndex + ".Ports." + sActivePort + ".name";
	String sActivePortName = (String)mapResult.get (sKeyName_ActivePortName);
	jsonResult.put ("source_active_port_name", sActivePortName);
}

void GetLastBluetoothSourceVolume (String sChannel, ObjectNode jsonResult)
{
	Map<String, Object> mapResult = PactlWrapper.pactl_list ("sources");
	String sLastSourceBlockName_Bluetooth = PactlWrapper.GetLastSourceBlockName_Bluetooth (mapResult);
	String sVolume = null;
	//sVolume = PactlWrapper.GetSourceVolumeInPercentage (mapResult, sSourceIndex, sChannel);

	if (StringUtils.isNotEmpty (sLastSourceBlockName_Bluetooth))
	{
		sVolume = PactlWrapper.GetVolumeInPercentage (mapResult, sLastSourceBlockName_Bluetooth, sChannel);
		jsonResult.put ("source_volume", sVolume);

		String sKeyName_SourceDescription = sLastSourceBlockName_Bluetooth + ".Description";
		String sKeyName_BluetoothDeviceAlias = sLastSourceBlockName_Bluetooth + ".Properties.bluez.alias";	// 之所以用这个，而不用上面的 ".Description" 是因为目前的 bluez 模块的 .Description 不支持汉字 -- 汉字都被清空了
		//jsonResult.put ("source_name", (String)mapResult.get (sKeyName_SourceDescription));
		jsonResult.put ("source_name", (String)mapResult.get (sKeyName_BluetoothDeviceAlias));

		String sKeyName_ActivePort = sLastSourceBlockName_Bluetooth + ".Active Port";
		String sActivePort = (String)mapResult.get(sKeyName_ActivePort);
		String sKeyName_ActivePortName = sLastSourceBlockName_Bluetooth + ".Ports." + sActivePort + ".name";
		String sActivePortName = (String)mapResult.get (sKeyName_ActivePortName);
		jsonResult.put ("source_active_port_name", sActivePortName);
	}
}
%>

<%@ include file='config.jsp'%>

<%
ObjectNode jsonResult = null;
String sMAC = StringUtils.trimToEmpty (request.getParameter ("mac"));	// 如果是通过 captive portal 访问的，应该会带过来使用人的设备（比如手机、笔记本电脑或者插在任何机器上的无线网卡）的 MAC 地址
String sClientIP = request.getRemoteAddr ();
String sAction = StringUtils.trimToEmpty (request.getParameter ("cmd"));
String sIsSource = StringUtils.trimToEmpty (request.getParameter ("isSource"));
boolean isSource = false;
try
{
	if (StringUtils.isNotEmpty (sIsSource))
	{
		isSource = Boolean.parseBoolean (sIsSource);
	}
}
catch (Exception e)
{
}

ObjectMapper jacksonObjectMapper_Loose = new ObjectMapper ();	// 不那么严格的选项，但解析时也支持严格选项
	jacksonObjectMapper_Loose.configure (JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);	// 允许不对字段名加引号
	jacksonObjectMapper_Loose.configure (MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);	// 字段名不区分大小写

	jacksonObjectMapper_Loose.configure (JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);	// 允许用单引号把数值引起来
	jacksonObjectMapper_Loose.configure (JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);	// 允许数值前面带 0

jsonResult = jacksonObjectMapper_Loose.createObjectNode ();
jsonResult.put ("rc", 0);
try
{

PactlWrapper.sServer = "localhost";	// 防止【运行 tomcat 的用户是 tomcat 时 执行 pactl 会失败】的问题

if (StringUtils.isEmpty (sAction) || StringUtils.equalsIgnoreCase (sAction, "get-volume"))
{
	if (! isSource)
		GetSinkVolume (sPACTL_SINK_DEVICE, sPACTL_SINK_DEVICE_CHANNEL_TO_GET_VOLUME, jsonResult);
	else
	{
		GetLastBluetoothSourceVolume (sPACTL_SOURCE_DEVICE_CHANNEL_TO_GET_VOLUME, jsonResult);
		if (jsonResult.get ("source_volume") == null)	// 没有蓝牙音源接入进来
		{
			jsonResult.put ("rc", 404);
		}
	}
}
else if (StringUtils.equalsIgnoreCase (sAction, "adjust-volume"))
{
	String sDirection = StringUtils.trimToEmpty (request.getParameter ("direction"));

	Map<String, Object> mapResult = PactlWrapper.pactl_list ("sinks");
	String sVolume = PactlWrapper.GetSinkVolumeInPercentage (mapResult, sPACTL_SINK_DEVICE, sPACTL_SINK_DEVICE_CHANNEL_TO_GET_VOLUME);
	sVolume = sVolume.substring (0, sVolume.length() - 1);	// 去掉百分号
	int nCurrentVolume = Integer.parseInt (sVolume);
	int nDestinationVolume = nCurrentVolume;
	boolean bAdjusted = false;
	if (StringUtils.equalsIgnoreCase (sDirection, "+"))
	{
		nDestinationVolume += nVOLUME_ADJUST_STEP;
		if (nCurrentVolume >= nMAX_ALLOWED_VOLUME_TO_ADJUST)	// 当前音量已经超过能调节到的最大音量
		{
			jsonResult.put ("rc", 403);
			jsonResult.put ("msg", "当前音量 " + sVolume + "% 已经超过(或者等于)你能调节到的最大音量 " + nMAX_ALLOWED_VOLUME_TO_ADJUST + "%，所以禁止再调大音量。");
		}
		else
		{
			if (nDestinationVolume > nMAX_ALLOWED_VOLUME_TO_ADJUST)
			{
				nDestinationVolume = nMAX_ALLOWED_VOLUME_TO_ADJUST;
				//jsonResult.put ("rc", 403);
				//jsonResult.put ("msg", "您能调节到的最大音量是 " + nMAX_ALLOWED_VOLUME_TO_ADJUST + "%");
			}
			PactlWrapper.pactl_set_sink_volume (sPACTL_SINK_DEVICE, nDestinationVolume + "%");
		}
	}
	else if (StringUtils.equalsIgnoreCase (sDirection, "-"))
	{
		nDestinationVolume -= nVOLUME_ADJUST_STEP;
		if (nCurrentVolume <= nMIN_ALLOWED_VOLUME_TO_ADJUST)	// 当前音量已经低于能调节到的最小音量
		{
			jsonResult.put ("rc", 403);
			jsonResult.put ("msg", "当前音量 " + sVolume + "% 已经低于(或者等于)你能调节到的最小音量 " + nMIN_ALLOWED_VOLUME_TO_ADJUST + "%，所以禁止再调小音量。");
		}
		else
		{
			if (nDestinationVolume < nMIN_ALLOWED_VOLUME_TO_ADJUST)
			{
				nDestinationVolume = nMIN_ALLOWED_VOLUME_TO_ADJUST;
			}
			PactlWrapper.pactl_set_sink_volume (sPACTL_SINK_DEVICE, nDestinationVolume + "%");
		}
	}
	else
	{
		/*
		if (true && bPLAY_BAIDU_TTS_WHEN_ADJUSTING_VOLUME)
		{
			// 调用百度 TTS 合成文字
			// "设备 *** 调节音量到 50%"
		}
		jsonResult.put ("volume", "当前音量");
		jsonResult.put ("volume.lfe", "当前低音声道音量");
		jsonResult.put ("volume.bluetooth", "当前蓝牙输入音量");
		*/
	}
}
}
catch (Exception e)
{
	e.printStackTrace ();
}


out.println (jsonResult);
%>
