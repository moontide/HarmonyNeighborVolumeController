package net.maclife.pactl;

import java.io.*;
import java.util.*;


public class PactlWrapper
{
	public static final String CLIENT_NAME = "JavaWrapperForPactl";

	String sServer;

	public static String pactl (String... params)
	{
		String[] args = new String[3 + (params==null ? 0 : params.length)];
		args[0] = "pactl";
		args[1] = "--client-name";
		args[2] = CLIENT_NAME;
		if (params != null)
		{
			for (int i=0; i<params.length; i++)
				args[3+i] = params[i];
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
System.out.println (sLine);
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
System.out.println ("pactl 执行 " + (rc == 0 ? "成功" : "失败：" + rc));
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
		for (String sBlock : arrayBlocks)
		{
			String[] arrayLines = sBlock.split ("\\n");
			String sLine1 = arrayLines[0];
			if (sLine1.startsWith ("Sink") || sLine1.startsWith ("Source"))
			{
			String sAttributeName = "";
			for (int i=1; i<arrayLines.length; i++)
			{
				String sLine = arrayLines[i];
				sLine = sLine.substring (1);	// 去掉左边的 TAB 字符
				if (sAttributeName.equalsIgnoreCase ("Volume") && sLine.startsWith (" "))
				{
					String sBalance = sLine.trim ().substring ("balance ".length ());
					mapResult.put (sLine1 + ".Volume.balance", sBalance);
				}
				else if (sAttributeName.equalsIgnoreCase ("Properties") && sLine.startsWith ("	"))
				{
					sLine = sLine.substring (1);	// 再去掉左边的第二个 TAB 字符
					String[] arrayProperty = sLine.split (" = ", 2);
					mapResult.put (sLine1 + ".Properties." + arrayProperty[0], arrayProperty[1]);
				}
				else if (sAttributeName.equalsIgnoreCase ("Ports") && sLine.startsWith ("	"))
				{
					sLine = sLine.substring (1);	// 再去掉左边的第二个 TAB 字符
					//String[] arrayProperty = sLine.split (": *", 2);
					//mapResult.put (sLine1 + ".Ports." + arrayProperty[0], arrayProperty[1]);
				}
				else if (sAttributeName.equalsIgnoreCase ("Formats") && sLine.startsWith ("	"))
				{
					sLine = sLine.substring (1);	// 再去掉左边的第二个 TAB 字符
					//String[] arrayFormat = sLine.split (" = ", 2);
					//mapResult.put (sLine1 + ".Formats." + arrayFormat[0], arrayFormat[1]);
				}
				else
				{
					if (sLine.isEmpty ())	// 本块的最后一行
						continue;
					String[] arrayLine = sLine.split (": *", 2);
					sAttributeName = arrayLine[0];
					String sAttributeValue = arrayLine[1];
					if (sAttributeName.equalsIgnoreCase ("Volume"))
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
									mapResult.put (sLine1 + ".Volume." + sChannelName + ".integer", arrayChannelVolumeExpressions[0]);
								if (arrayChannelVolumeExpressions.length >= 2)
									mapResult.put (sLine1 + ".Volume." + sChannelName + ".percentage", arrayChannelVolumeExpressions[1]);
								if (arrayChannelVolumeExpressions.length >= 3)
									mapResult.put (sLine1 + ".Volume." + sChannelName + ".dB", arrayChannelVolumeExpressions[2]);
							}
						}
					}
					else if (sAttributeName.equalsIgnoreCase ("Properties"))
					{
					}
					else if (sAttributeName.equalsIgnoreCase ("Ports"))
					{
					}
					else if (sAttributeName.equalsIgnoreCase ("Formats"))
					{
					}
				}
			}
			}
			//mapResult.put (sLine1, sBlock);
		}
		return mapResult;
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


	public static String GetSinkVolumeInPercentage (Map<String, Object> mapResult, String sSinkIndex, String sChannelName)
	{
		return (String)mapResult.get ("Sink #" + sSinkIndex + ".Volume." + sChannelName + ".percentage");
	}
	public static String GetSinkVolumeInPercentage (String sSinkIndex, String sChannelName)
	{
		Map<String, Object> mapResult = pactl_list ("sinks");
//System.out.println (mapResult);
//System.out.println (mapResult.get ("Sink #" + sSinkIndex + ".Volume." + sChannelName + ".integer"));
//System.out.println (mapResult.get ("Sink #" + sSinkIndex + ".Volume." + sChannelName + ".percentage"));
//System.out.println (mapResult.get ("Sink #" + sSinkIndex + ".Volume." + sChannelName + ".dB"));
		return GetSinkVolumeInPercentage (mapResult, sSinkIndex, sChannelName);
	}
	public static String GetSinkVolumeInPercentage (String sSinkIndex)
	{
		return GetSinkVolumeInPercentage (sSinkIndex, "front-left");
	}

	public static String GetSourceVolumeInPercentage (Map<String, Object> mapResult, String sSourceIndex, String sChannelName)
	{
		return (String)mapResult.get ("Source #" + sSourceIndex + ".Volume." + sChannelName + ".percentage");
	}
	public static String GetSourceVolumeInPercentage (String sSourceIndex, String sChannelName)
	{
		Map<String, Object> mapResult = pactl_list ("sources");
		return GetSourceVolumeInPercentage (mapResult, sSourceIndex, sChannelName);
	}
	public static String GetSourceVolumeInPercentage (String sSourceIndex)
	{
		return GetSourceVolumeInPercentage (sSourceIndex, "front-left");
	}

	public static void main (String[] args)
	{
		GetSinkVolumeInPercentage ("0");
	}
}
