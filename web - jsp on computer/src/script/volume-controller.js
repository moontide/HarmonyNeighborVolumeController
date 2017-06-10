import $ from 'jquery';

$(document).ready(() => setInterval(GetAndDisplayVolume, 2000));

function Init ()
{
	// 初始化页面：
	// 1. 显示当前音量 （主音量、低音频道）
	// 2. 显示连入的蓝牙放音设备

	GetAndDisplayVolume ();
}

function GetAndDisplayVolume ()
{
	$.get ('pactl.jsp', {mac:$('#mac_address').val()})
	.done
	(
		function (data)
		{
			DisplayVolume (data);
		}
	);

	$.get ('pactl.jsp', {mac:$('#mac_address').val(), isSource:true})
	.done
	(
		function (data)
		{
			DisplaySourceVolume (data);
		}
	);
}

function DisplayVolume (jsonData)
{
	var volume = jsonData.sink_volume;
	$('#pulseaudio_sink_name').text (jsonData.sink_name);
	$('#sink_volume').text (volume);
	$('#sink_volume_bar').text (volume);
	$('#sink_volume_bar').css ('width', volume);

	$('#pulseaudio_sink_port').text (jsonData.sink_active_port_name);

	$('#earphone_plugged_in_text').css ('display', jsonData.earphone_plugged_in ? '' : 'none');
}

function DisplaySourceVolume (jsonData)
{
	switch (jsonData.rc)
	{
		case 0:
		case 200:
			var volume = jsonData.source_volume;
			$('#pulseaudio_source_name').text (jsonData.source_name);
			$('#source_volume').text (volume);
			$('#source_volume_bar').text (volume);
			$('#source_volume_bar').css ('width', volume);

			$('#pulseaudio_source_port').text (jsonData.source_active_port_name);
			$('#container_pulseaudio_bluetooth_connected').css ('display', '');
			break;
		case 404:
			$('#container_pulseaudio_bluetooth_connected').css ('display', 'none');
			$('#pulseaudio_source_name').text ('');
			$('#source_volume').text ('');
			$('#source_volume_bar').text ('');
			$('#source_volume_bar').css ('width', '0%');
			$('#pulseaudio_source_port').text ('');
			break;
		default:
			break;
	}
}

function AdjustVolume (sDirection)
{
	$.get ('pactl.jsp', {mac:$('#mac_address').val(), cmd:'adjust-volume', direction:sDirection})
	.done
	(
		function (data)
		{
			if (data.rc == 0 || data.rc == 200)
			{
				//DisplayVolume (data);
			}
			else
			{
				alert (data.msg);
			}
		}
	);
}

function AdjustSourceVolume (sDirection)
{
	$.get ('pactl.jsp', {mac:$('#mac_address').val(), cmd:'adjust-volume', direction:sDirection, isSource:true})
	.done
	(
		function (data)
		{
			if (data.rc == 0 || data.rc == 200)
			{
				//DisplayVolume (data);
			}
			else
			{
				alert (data.msg);
			}
		}
	);
}
