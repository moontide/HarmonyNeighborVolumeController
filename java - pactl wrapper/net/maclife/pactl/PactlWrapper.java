package net.maclife.pactl;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.*;


public class PactlWrapper
{
	public static final String CLIENT_NAME = "JavaWrapperForPactl";

	public static String sServer = null;

	public static String pactl (String... params)
	{
		int nExtraParams = 3;
		if (StringUtils.isNotEmpty (sServer))
			nExtraParams += 2;
		String[] args = new String[nExtraParams + (params==null ? 0 : params.length)];
		args[0] = "pactl";
		args[1] = "--client-name";
		args[2] = CLIENT_NAME;
		if (StringUtils.isNotEmpty (sServer))
		{
			args[3] = "--server";	// 这些 options 貌似只能在 pactl 的命令前面，在后面会报错，比如 `pactl list sinks --server localhost`: "Specify nothing, or one of: modules, sinks, sources, sink-inputs, source-outputs, clients, samples, cards"
			args[4] = sServer;
		}
		if (params != null)
		{
			for (int i=0; i<params.length; i++)
				args[nExtraParams+i] = params[i];
		}

		ProcessBuilder pbPactl = new ProcessBuilder ();
		pbPactl.environment ().put ("LANG", "C");
		pbPactl.command (args);
		StringBuilder sb = new StringBuilder ();
		try
		{
			Process p = pbPactl.start();
			InputStream isStdout = p.getInputStream();
			InputStream isStderr = p.getErrorStream();
			//while (isStdout. != -1);	// consume it
			//while (isStderr.read() != -1);	// consume it
			//isStdout.close();
			//isStderr.close();
			BufferedReader br = null;
			String sLine = null;
			br = new BufferedReader (new InputStreamReader (isStdout));
			while ((sLine = br.readLine ()) != null)
			{
//System.out.println (sLine);
				sb.append (sLine);
				sb.append ("\n");
			}
			br.close ();

			br = new BufferedReader (new InputStreamReader (isStderr));
			while ((sLine = br.readLine ()) != null)
			{
System.err.println (sLine);
			}
			br.close ();

			p.waitFor();
			int rc = p.exitValue();
			if (rc != 0)
			{
System.out.println ("pactl 执行 " + (rc == 0 ? "成功" : "失败：" + rc));
			}
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
		return sb.toString ();
	}

	public static String pactlWith1MandatoryArguments (String arg1, String... otherParams)
	{
		String[] args = new String[1 + (otherParams==null ? 0 : otherParams.length)];
		args[0] = arg1;
		if (otherParams != null)
		{
			for (int i=0; i<otherParams.length; i++)
				args[1+i] = otherParams[i];
		}
		return pactl (args);
	}

	public static String pactlWith2MandatoryArguments (String arg1, String arg2, String... otherParams)
	{
		String[] args = new String[2 + (otherParams==null ? 0 : otherParams.length)];
		args[0] = arg1;
		args[1] = arg2;
		if (otherParams != null)
		{
			for (int i=0; i<otherParams.length; i++)
				args[2+i] = otherParams[i];
		}
		return pactl (args);
	}

	public static String pactlWith3MandatoryArguments (String arg1, String arg2, String arg3, String... otherParams)
	{
		String[] args = new String[3 + (otherParams==null ? 0 : otherParams.length)];
		args[0] = arg1;
		args[1] = arg2;
		args[2] = arg3;
		if (otherParams != null)
		{
			for (int i=0; i<otherParams.length; i++)
				args[3+i] = otherParams[i];
		}
		return pactl (args);
	}

	public static String pactlWith4MandatoryArguments (String arg1, String arg2, String arg3, String arg4, String... otherParams)
	{
		String[] args = new String[4 + (otherParams==null ? 0 : otherParams.length)];
		args[0] = arg1;
		args[1] = arg2;
		args[2] = arg3;
		args[3] = arg4;
		if (otherParams != null)
		{
			for (int i=0; i<otherParams.length; i++)
				args[4+i] = otherParams[i];
		}
		return pactl (args);
	}

	// -------------------------------------------------------------------------
	//
	// 一级封装
	//
	// -------------------------------------------------------------------------

	public static String pactl_help ()
	{
		return pactlWith1MandatoryArguments ("--help");
	}

	public static String pactl_version ()
	{
		return pactlWith1MandatoryArguments ("--version");
	}

	public static String pactl_stat (String... arrayOtherParams)
	{
		return pactlWith1MandatoryArguments ("stat", arrayOtherParams);
	}

	public static String pactl_info (String... arrayOtherParams)
	{
		return pactlWith1MandatoryArguments ("info", arrayOtherParams);
	}

	public static  Map<String, Object> pactl_list (String... arrayOtherParams)
	{
		return ParsePactlListOutput (pactlWith1MandatoryArguments ("list", arrayOtherParams));
	}

	public static Map<String, Object> ParsePactlListOutput (String sOutputOfListCommand)
	{
		Map<String, Object> mapResult = new HashMap<String, Object> ();
		String[] arrayBlocks = sOutputOfListCommand.split ("\\n\\n");
		for (int i=0; i<arrayBlocks.length; i++)
		{
			String sBlock = arrayBlocks[i];
			String[] arrayBlockLines = sBlock.split ("\\n");
			String sFirstLineOfBlock = arrayBlockLines[0];
			if (StringUtils.startsWithIgnoreCase (sFirstLineOfBlock, "Sink") || StringUtils.startsWithIgnoreCase (sFirstLineOfBlock, "Source"))
			{
				ParseSinkOrSourceBlock (sBlock, sFirstLineOfBlock, arrayBlockLines, mapResult);

				if (StringUtils.startsWithIgnoreCase (sFirstLineOfBlock, "Sink"))
				{
					mapResult.put ("LastSinkBlockName", sFirstLineOfBlock);

					String sDriver = (String)mapResult.get (sFirstLineOfBlock + ".Driver");
					if (StringUtils.startsWithIgnoreCase (sDriver, "module-bluez"))
					{
						mapResult.put ("LastSinkBlockName_Bluetooth", sFirstLineOfBlock);
						String sBluetoothProtocol = (String)mapResult.get (sFirstLineOfBlock + ".Properties.bluetooth.protocol");
						if (StringUtils.equalsIgnoreCase (sBluetoothProtocol, "a2dp_sink"))	// 这里会出现 a2dp_source ? 貌似不会
						{
							mapResult.put ("LastSinkBlockName_Bluetooth_a2dp_sink", sFirstLineOfBlock);
						}
					}
				}
				else if (StringUtils.startsWithIgnoreCase (sFirstLineOfBlock, "Source"))
				{
					mapResult.put ("LastSourceBlockName", sFirstLineOfBlock);

					String sDriver = (String)mapResult.get (sFirstLineOfBlock + ".Driver");
					if (StringUtils.startsWithIgnoreCase (sDriver, "module-bluez"))
					{
						mapResult.put ("LastSourceBlockName_Bluetooth", sFirstLineOfBlock);
						String sBluetoothProtocol = (String)mapResult.get (sFirstLineOfBlock + ".Properties.bluetooth.protocol");
						if (StringUtils.equalsIgnoreCase (sBluetoothProtocol, "a2dp_source"))	// 这里会出现 a2dp_sink ? 貌似不会
						{
							mapResult.put ("LastSourceBlockName_Bluetooth_a2dp_source", sFirstLineOfBlock);
						}
					}
				}
			}
			mapResult.put (sFirstLineOfBlock, sBlock);

			List<String> listBlockNames = (List<String>) mapResult.get ("BlockNamesList");
			if (listBlockNames == null)
			{
				listBlockNames = new ArrayList<String> ();
				mapResult.put ("BlockNamesList", listBlockNames);
			}
			listBlockNames.add (sFirstLineOfBlock);
		}
		return mapResult;
	}

	public static void ParseSinkOrSourceBlock (String sBlock, String sFirstLineOfBlock, String[] arrayBlockLines, Map<String, Object> mapResult)
	{
		String sAttributeName = "";
		for (int i=1; i<arrayBlockLines.length; i++)
		{
			String sLine = arrayBlockLines[i];
			sLine = sLine.substring (1);	// 去掉左边的 TAB 字符
			if (StringUtils.equalsIgnoreCase (sAttributeName, "Volume") && sLine.startsWith (" "))
			{
				String sBalance = sLine.trim ().substring ("balance ".length ());
				mapResult.put (sFirstLineOfBlock + ".Volume.balance", sBalance);
			}
			else if (StringUtils.equalsIgnoreCase (sAttributeName, "Properties") && sLine.startsWith ("	"))
			{
				sLine = sLine.substring (1);	// 再去掉左边的第二个 TAB 字符
				String[] arrayProperty = sLine.split (" = ", 2);
				//mapResult.put (sFirstLineOfBlock + ".Properties." + arrayProperty[0], arrayProperty[1]) - 1);
				mapResult.put (sFirstLineOfBlock + ".Properties." + arrayProperty[0], StringUtils.substring (arrayProperty[1], 1, StringUtils.length (arrayProperty[1]) - 1));	// 去掉前后的引号
			}
			else if (StringUtils.equalsIgnoreCase (sAttributeName, "Ports") && sLine.startsWith ("	"))
			{
				sLine = sLine.substring (1);	// 再去掉左边的第二个 TAB 字符
				String[] arrayPort = sLine.split (": *", 2);
				mapResult.put (sFirstLineOfBlock + ".Ports." + arrayPort[0], arrayPort[1]);

				// 从 arrayPort[1] 中取出 port 的名称、优先级、是否可用（是否当前的端口？）
				String sPortName = "";
				String sPortPriority = "";
				String sPortAvailability = "";
				sPortName = arrayPort[1].substring (0, arrayPort[1].indexOf ('(') - 1);
				mapResult.put (sFirstLineOfBlock + ".Ports." + arrayPort[0] + ".name", sPortName);

				String sPortProperties = arrayPort[1].substring (arrayPort[1].indexOf ('(') + 1, arrayPort[1].indexOf (')') - 1);
				String[] arrayPortProperties = arrayPort[1].split (", *");
				for (String sPortProperty : arrayPortProperties)
				{
					if (StringUtils.containsIgnoreCase (sPortProperty, "available"))
					{
						sPortAvailability = sPortProperty;
						mapResult.put (sFirstLineOfBlock + ".Ports." + arrayPort[0] + ".availability", sPortAvailability);
					}
					else if (sPortProperty.contains (": "))
					{
						String[] arrayPortProperty = sPortProperty.split (": ");
						if (StringUtils.equalsIgnoreCase (arrayPortProperty[0], "priority"))
						{
							sPortPriority = arrayPortProperty[1];
							mapResult.put (sFirstLineOfBlock + ".Ports." + arrayPort[0] + ".priority", sPortPriority);
						}
					}
				}
			}
			else if (StringUtils.equalsIgnoreCase (sAttributeName, "Formats") && StringUtils.startsWithIgnoreCase (sLine, "	"))
			{
				sLine = sLine.substring (1);	// 再去掉左边的第二个 TAB 字符
			}
			else
			{
				if (StringUtils.isEmpty (sLine))	// 本块的最后一行
					continue;
				String[] arrayLine = sLine.split (": *", 2);
				sAttributeName = arrayLine[0];
				String sAttributeValue = arrayLine[1];
				if (StringUtils.equalsIgnoreCase (sAttributeName, "Volume"))
				{
					String[] arrayChannels = sAttributeValue.split (", *");
					for (String sChannel : arrayChannels)
					{
						String[] arrayChannel = sChannel.split (": *", 2);
						String sChannelName = arrayChannel[0];
						String sChannelVolumeExpression = arrayChannel[1];
						String[] arrayChannelVolumeExpressions = sChannelVolumeExpression.split (" +/ +");
						if (arrayChannelVolumeExpressions != null)
						{
							if (arrayChannelVolumeExpressions.length >= 1)
								mapResult.put (sFirstLineOfBlock + ".Volume." + sChannelName + ".integer", arrayChannelVolumeExpressions[0]);
							if (arrayChannelVolumeExpressions.length >= 2)
								mapResult.put (sFirstLineOfBlock + ".Volume." + sChannelName + ".percentage", arrayChannelVolumeExpressions[1]);
							if (arrayChannelVolumeExpressions.length >= 3)
								mapResult.put (sFirstLineOfBlock + ".Volume." + sChannelName + ".dB", arrayChannelVolumeExpressions[2]);
						}
					}
				}
				else if (StringUtils.equalsIgnoreCase (sAttributeName, "Properties"))
				{
				}
				else if (StringUtils.equalsIgnoreCase (sAttributeName, "Ports"))
				{
				}
				else if (StringUtils.equalsIgnoreCase (sAttributeName, "Formats"))
				{
				}
				else if (sAttributeName.charAt (0) != '\t' && sAttributeName.charAt (0) != ' ')
				{
					mapResult.put (sFirstLineOfBlock + "." + sAttributeName, sAttributeValue);
				}
			}
		}
	}

	public static String pactl_exit (String... arrayOtherParams)
	{
		return pactlWith1MandatoryArguments ("exit", arrayOtherParams);
	}

	public static String pactl_upload_sample (String sFileName, String sCachedNameOfThisSample, String... arrayOtherParams)
	{
		return pactlWith3MandatoryArguments ("upload-sample", sFileName, sCachedNameOfThisSample, arrayOtherParams);
	}
	public static String pactl_upload_sample (String sFileName, String... arrayOtherParams)
	{
		return pactl_upload_sample (sFileName, null, arrayOtherParams);
	}

	public static String pactl_play_sample (String sCachedNameOfThisSample, String sSink, String... arrayOtherParams)
	{
		return pactlWith3MandatoryArguments ("play-sample", sCachedNameOfThisSample, sSink, arrayOtherParams);
	}
	public static String pactl_play_sample (String sCachedNameOfThisSample, String... arrayOtherParams)
	{
		return pactl_play_sample (sCachedNameOfThisSample, null, arrayOtherParams);
	}

	public static String pactl_remove_sample (String sCachedNameOfThisSample, String... arrayOtherParams)
	{
		return pactlWith2MandatoryArguments ("remove-sample", sCachedNameOfThisSample, arrayOtherParams);
	}

	public static String pactl_load_module (String sModuleName, String ... arrayLoadMoudleAndOtherArgs)
	{
		return pactlWith2MandatoryArguments ("load-module", sModuleName, arrayLoadMoudleAndOtherArgs);
	}

	public static String pactl_unload_sample (String sModuleInstanceIndexOrModuleName, String... arrayOtherParams)
	{
		return pactlWith2MandatoryArguments ("unload-module", sModuleInstanceIndexOrModuleName, arrayOtherParams);
	}

	public static String pactl_move_sink_input (String sPlaybackStreamIndex, String sSinkNameOrSinkIndex, String... arrayOtherParams)
	{
		return pactlWith3MandatoryArguments ("move-sink-input", sPlaybackStreamIndex, sSinkNameOrSinkIndex, arrayOtherParams);
	}

	public static String pactl_move_source_output (String sRecordingStreamIndex, String sSourceNameOrSourceIndex, String... arrayOtherParams)
	{
		return pactlWith3MandatoryArguments ("move-source-output", sRecordingStreamIndex, sSourceNameOrSourceIndex, arrayOtherParams);
	}

	public static String pactl_suspend_sink (String sSinkNameOrSinkIndex, boolean bSuspendOrResume, String... arrayOtherParams)
	{
		return pactlWith3MandatoryArguments ("suspend-sink", sSinkNameOrSinkIndex, bSuspendOrResume ? "1" : "0", arrayOtherParams);
	}
	public static String SuspendSink (String sSinkNameOrSinkIndex, String... arrayOtherParams)
	{
		return pactl_suspend_sink (sSinkNameOrSinkIndex, true, arrayOtherParams);
	}
	public static String ResumeSink (String sSinkNameOrSinkIndex, String... arrayOtherParams)
	{
		return pactl_suspend_sink (sSinkNameOrSinkIndex, false, arrayOtherParams);
	}

	public static String pactl_suspend_source (String sSourceNameOrSourceIndex, boolean bSuspendOrResume, String... arrayOtherParams)
	{
		return pactlWith3MandatoryArguments ("suspend-source", sSourceNameOrSourceIndex, bSuspendOrResume ? "1" : "0", arrayOtherParams);
	}
	public static String SuspendSource (String sSourceNameOrSourceIndex, String... arrayOtherParams)
	{
		return pactl_suspend_source (sSourceNameOrSourceIndex, true, arrayOtherParams);
	}
	public static String ResumeSource (String sSourceNameOrSourceIndex, String... arrayOtherParams)
	{
		return pactl_suspend_source (sSourceNameOrSourceIndex, false, arrayOtherParams);
	}

	public static String pactl_set_card_profile (String sCardNameOrCardIndex, String sProfile, String... arrayOtherParams)
	{
		return pactlWith3MandatoryArguments ("set-card-profile", sCardNameOrCardIndex, sProfile, arrayOtherParams);
	}

	public static String pactl_set_default_sink (String sSinkNameOrSinkIndex, String... arrayOtherParams)
	{
		return pactlWith2MandatoryArguments ("set-default-sink", sSinkNameOrSinkIndex, arrayOtherParams);
	}

	public static String pactl_set_sink_port (String sSinkNameOrSinkIndex, String sPortName, String... arrayOtherParams)
	{
		return pactlWith3MandatoryArguments ("set-sink-port", sSinkNameOrSinkIndex, sPortName, arrayOtherParams);
	}

	public static String pactl_set_default_source (String sSourceNameOrSourceIndex, String... arrayOtherParams)
	{
		return pactlWith2MandatoryArguments ("set-default-source", sSourceNameOrSourceIndex, arrayOtherParams);
	}

	public static String pactl_set_source_port (String sSourceNameOrSourceIndex, String sPortName, String... arrayOtherParams)
	{
		return pactlWith3MandatoryArguments ("set-source-port", sSourceNameOrSourceIndex, sPortName, arrayOtherParams);
	}

	public static String pactl_set_port_latency_offset (String sCard, String sPortName, String sOffset, String... arrayOtherParams)
	{
		return pactlWith4MandatoryArguments ("set-port-latency-offset", sCard, sPortName, sOffset, arrayOtherParams);
	}

	public static String pactl_set_sink_volume (String sSinkNameOrSinkIndex, String sMainChannelVolumeExpression, String... arrayOtherChannelsVolumeExpressionAndOtherParams)
	{
		return pactlWith3MandatoryArguments ("set-sink-volume", sSinkNameOrSinkIndex, sMainChannelVolumeExpression, arrayOtherChannelsVolumeExpressionAndOtherParams);
	}

	public static String pactl_set_source_volume (String sSourceNameOrSourceIndex, String sMainChannelVolumeExpression, String... arrayOtherChannelsVolumeExpressionAndOtherParams)
	{
		return pactlWith3MandatoryArguments ("set-source-volume", sSourceNameOrSourceIndex, sMainChannelVolumeExpression, arrayOtherChannelsVolumeExpressionAndOtherParams);
	}

	public static String pactl_set_sink_input_volume (String sSinkInputIndex, String sMainChannelVolumeExpression, String... arrayOtherChannelsVolumeExpressionAndOtherParams)
	{
		return pactlWith3MandatoryArguments ("set-sink-input-volume", sSinkInputIndex, sMainChannelVolumeExpression, arrayOtherChannelsVolumeExpressionAndOtherParams);
	}
	public static String SetApplicationVolume (String sApplicationIndex, String sMainChannelVolumeExpression, String... arrayOtherChannelsVolumeExpressionAndOtherParams)
	{
		return pactl_set_sink_input_volume (sApplicationIndex, sMainChannelVolumeExpression, arrayOtherChannelsVolumeExpressionAndOtherParams);
	}

	public static String pactl_set_source_output_volume (String sSourceIndex, String sMainChannelVolumeExpression, String... arrayOtherChannelsVolumeExpressionAndOtherParams)
	{
		return pactlWith3MandatoryArguments ("set-source-output-volume", sSourceIndex, sMainChannelVolumeExpression, arrayOtherChannelsVolumeExpressionAndOtherParams);
	}

	public static String pactl_set_sink_mute (String sSinkNameOrSinkIndex, String sMuteOption, String... arrayOtherParams)
	{
		return pactlWith3MandatoryArguments ("set-sink-mute", sSinkNameOrSinkIndex, sMuteOption, arrayOtherParams);
	}
	public static String MuteVirtualCard (String sSinkNameOrSinkIndex, String... arrayOtherParams)
	{
		return pactl_set_sink_mute (sSinkNameOrSinkIndex, "1", arrayOtherParams);
	}
	public static String UnmuteVirtualCard (String sSinkNameOrSinkIndex, String... arrayOtherParams)
	{
		return pactl_set_sink_mute (sSinkNameOrSinkIndex, "0", arrayOtherParams);
	}
	public static String ToggleMuteVirtualCard (String sSinkNameOrSinkIndex, String... arrayOtherParams)
	{
		return pactl_set_sink_mute (sSinkNameOrSinkIndex, "toggle", arrayOtherParams);
	}

	public static String pactl_set_source_mute (String sSourceNameOrSourceIndex, String sMuteOption, String... arrayOtherParams)
	{
		return pactlWith3MandatoryArguments ("set-source-mute", sSourceNameOrSourceIndex, sMuteOption, arrayOtherParams);
	}
	public static String MuteRecordingDevice (String sSourceNameOrSourceIndex, String... arrayOtherParams)
	{
		return pactl_set_source_mute (sSourceNameOrSourceIndex, "1", arrayOtherParams);
	}
	public static String UnmuteRecordingDevice (String sSourceNameOrSourceIndex, String... arrayOtherParams)
	{
		return pactl_set_source_mute (sSourceNameOrSourceIndex, "0", arrayOtherParams);
	}
	public static String ToggleMuteRecordingDevice (String sSourceNameOrSourceIndex, String... arrayOtherParams)
	{
		return pactl_set_source_mute (sSourceNameOrSourceIndex, "toggle", arrayOtherParams);
	}

	public static String pactl_set_sink_input_mute (String sSinkInputIndex, String sMuteOption, String... arrayOtherParams)
	{
		return pactlWith3MandatoryArguments ("set-sink-input-mute", sSinkInputIndex, sMuteOption, arrayOtherParams);
	}
	public static String MuteApplication (String sApplicationIndex, String... arrayOtherParams)
	{
		return pactl_set_sink_input_mute (sApplicationIndex, "1", arrayOtherParams);
	}
	public static String UnmuteApplication (String sApplicationIndex, String... arrayOtherParams)
	{
		return pactl_set_sink_input_mute (sApplicationIndex, "0", arrayOtherParams);
	}
	public static String ToggleMuteApplication (String sApplicationIndex, String... arrayOtherParams)
	{
		return pactl_set_sink_input_mute (sApplicationIndex, "toggle", arrayOtherParams);
	}

	public static String pactl_set_source_output_mute (String sSourceOutputIndex, String sMuteOption, String... arrayOtherParams)
	{
		return pactlWith3MandatoryArguments ("set-source-output-mute", sSourceOutputIndex, sMuteOption, arrayOtherParams);
	}
	public static String MuteRecordingDeviceSource (String sSourceOutputIndex, String... arrayOtherParams)
	{
		return pactl_set_source_output_mute (sSourceOutputIndex, "1", arrayOtherParams);
	}
	public static String UnmuteRecordingDeviceSource (String sSourceOutputIndex, String... arrayOtherParams)
	{
		return pactl_set_source_output_mute (sSourceOutputIndex, "0", arrayOtherParams);
	}
	public static String ToggleMuteRecordingDeviceSource (String sSourceOutputIndex, String... arrayOtherParams)
	{
		return pactl_set_source_output_mute (sSourceOutputIndex, "toggle", arrayOtherParams);
	}

	public static String pactl_set_sink_format (String sSinkIndex, String sFormats, String... arrayOtherParams)
	{
		return pactlWith3MandatoryArguments ("set-sink-format", sSinkIndex, sFormats, arrayOtherParams);
	}

	public static String pactl_subscribe ()
	{
		throw new RuntimeException ("You shouldn't use this function. use StartEventMonitor function instead.");
		//return pactl ("subscribe");
	}


	// -------------------------------------------------------------------------
	//
	// 二级封装：一些便利函数。
	// 使用该类的应用程序可以结合一类封装里的返回结果（主要是 pactl_list），然后利用二级封装的这些便利函数来进行处理。
	//
	// -------------------------------------------------------------------------

	/**
	 * 从 BlockName 中取出索引号，比如：从 "Sink #1" 取出 "1"
	 * @param sBlockName
	 * @return
	 */
	public static String GetIndexFromBlockName (String sBlockName)
	{
		return sBlockName.substring (sBlockName.indexOf("#") + 1);
	}

	/**
	 * 根据 Block 和索引号生成 BlockName
	 * @param sBlock 必须是按 pactl 的 BlockName 习惯命名的字符串。pactl 当前命名习惯：首字母大写的一个单词，如 "Sink"、"Source"。
	 * @param sIndex
	 * @return
	 */
	public static String GenerateBlockNameFromIndex (String sBlock, String sIndex)
	{
		return sBlock + " #" + sIndex;
	}
	public static String GenerateSinkBlockNameFromIndex (String sIndex)
	{
		return GenerateBlockNameFromIndex ("Sink", sIndex);
	}
	public static String GenerateSourceBlockNameFromIndex (String sIndex)
	{
		return GenerateBlockNameFromIndex ("Source", sIndex);
	}


	public static String GetVolumeInPercentage (Map<String, Object> mapResult, String sBlockName, String sChannelName)
	{
		return (String)mapResult.get (sBlockName + ".Volume." + sChannelName + ".percentage");
	}

	public static String GetSinkVolumeInPercentage (Map<String, Object> mapResult, String sSinkIndex, String sChannelName)
	{
		return GetVolumeInPercentage (mapResult, GenerateSinkBlockNameFromIndex(sSinkIndex), sChannelName);
	}
	public static String GetSinkVolumeInPercentage (String sSinkIndex, String sChannelName)
	{
		Map<String, Object> mapResult = pactl_list ("sinks");
//System.out.println (mapResult);
//System.out.println (mapResult.get (GenerateSinkBlockNameFromIndex (sSinkIndex) + ".Volume." + sChannelName + ".integer"));
//System.out.println (mapResult.get (GenerateSinkBlockNameFromIndex (sSinkIndex) + ".Volume." + sChannelName + ".percentage"));
//System.out.println (mapResult.get (GenerateSinkBlockNameFromIndex (sSinkIndex) + ".Volume." + sChannelName + ".dB"));
		return GetSinkVolumeInPercentage (mapResult, sSinkIndex, sChannelName);
	}
	public static String GetSinkVolumeInPercentage (String sSinkIndex)
	{
		return GetSinkVolumeInPercentage (sSinkIndex, "front-left");
	}

	public static String GetSourceVolumeInPercentage (Map<String, Object> mapResult, String sSourceIndex, String sChannelName)
	{
		return GetVolumeInPercentage (mapResult, GenerateSourceBlockNameFromIndex (sSourceIndex), sChannelName);
	}
	public static String GetSourceVolumeInPercentage (String sSourceIndex, String sChannelName)
	{
		Map<String, Object> mapResult = pactl_list ("sources");
//System.out.println (mapResult);
		return GetSourceVolumeInPercentage (mapResult, sSourceIndex, sChannelName);
	}
	public static String GetSourceVolumeInPercentage (String sSourceIndex)
	{
		return GetSourceVolumeInPercentage (sSourceIndex, "front-left");
	}
	public static String GetLastSourceBlockName_Bluetooth (Map<String, Object> mapResult)
	{
		return (String)mapResult.get ("LastSourceBlockName_Bluetooth");
	}


	public static String GetSinkName (Map<String, Object> mapResult, String sSinkIndex)
	{
		String sKeyName_SinkDescription = GenerateSinkBlockNameFromIndex (sSinkIndex) + ".Description";
		return (String)mapResult.get(sKeyName_SinkDescription);
	}
	public static String GetSinkActivePort (Map<String, Object> mapResult, String sSinkIndex)
	{
		String sKeyName_ActivePort = GenerateSinkBlockNameFromIndex (sSinkIndex) + ".Active Port";
		String sActivePort = (String)mapResult.get(sKeyName_ActivePort);
		return sActivePort;
	}
	public static String GetSinkPortName (Map<String, Object> mapResult, String sSinkIndex, String sPort)
	{
		String sKeyName_PortName = GenerateSinkBlockNameFromIndex (sSinkIndex) + ".Ports." + sPort + ".name";
		String sPortName = (String)mapResult.get(sKeyName_PortName);
		return sPortName;
	}
	public static boolean IsEarphonePluggedIn (String sActivePort)
	{
		boolean bEarphonePlugged = false;
		if (StringUtils.isNotEmpty (sActivePort) && StringUtils.endsWithIgnoreCase(sActivePort, "-headphones"))
		{
			bEarphonePlugged = true;
		}
		return bEarphonePlugged;
	}

	public static void main (String[] args)
	{
		GetSinkVolumeInPercentage ("0");
		GetSourceVolumeInPercentage ("1");
	}
}
