function Init ()
{
	// 初始化页面：
	// 1. 显示当前音量 （主音量、低音频道）
	// 2. 显示连入的蓝牙放音设备

	GetAndDisplayVolume ();
}

function DisplayVolume (jsonData)
{
	var volume = jsonData.volume;
	$('#pulseaudio_sink_name').text (jsonData.sink_name);
	$('#current_main_volume').text (volume);
	$('#main_volume_bar').text (volume);
	$('#main_volume_bar').css ('width', volume);

	$('#earphone_plugged_in_text').css ('display', jsonData.earphone_plugged_in ? '' : 'none');
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
}

function AdjustVolume (sDirection)
{
	$.get ('pactl.jsp', {mac:$('#mac_address').val(), cmd:'adjust-volume', direction:sDirection})
	.done
	(
		function (data)
		{
			if (data.rc == 0)
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
