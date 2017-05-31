<%@ page pageEncoding="UTF-8" contentType="text/html"%>

<%@ include file='config.jsp'%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>
	<title>睦邻音量控制器</title>
	<link href='style.css' type='text/css' rel='stylesheet'/>
	<script type='text/javascript' src='jquery.min.js'></script>
	<script type='text/javascript' src='volume-controller.js'></script>
</head>

<body>
	<input id='mac_address' type='hidden' value='<%=request.getParameter("mac")==null ? "" : request.getParameter("mac")%>'/>
	<div id='volume_controller_container'>
	<h1>放音音量控制</h1><%-- 输出音量调节 --%>
	<div>
		<div id='earphone_plugged_in_text' style='display:none'>耳机已经插上，禁止调节音量。<br/>
		如果你的确还听到声音，则有可能：
		<ul>
			<li>
				<ul>
					<li>音箱可能接的不是本电脑</li>
					<li>或者音箱接的是不同的声卡</li>
				</ul>
				这几种情况需要去敲门了，或者通过其他途径告知。
			</li>
			<li>或者听到的是其他家音箱发出的声音</li>
		</ul>
		</div><%-- 只在接音箱时才允许调节音量（但对于音箱上自带耳机接口的情况，对电脑来说区分不出来：还是属于音箱，还是允许调节音量）--%>

		<div>所控制的放音设备: <span id='pulseaudio_sink_name' class='data'></span><br/><span id='pulseaudio_sink_port' class='data'></span></div>
		<div>当前音量: <span id='sink_volume' class='data'></span><%-- 要不断更新，有可能从不同的源头调节音量 --%>
			<div class='volume-bar-container'><div id='sink_volume_bar' class='volume-bar'></div></div>
			<div>
				<button onClick='AdjustVolume ("-");'>音量--</button>
				<button onClick='AdjustVolume ("+");' style='float:right;'>音量++</button>
			</div>

			<label style='margin:auto auto'><input type='checkbox' onCheck='$("#lfe_channel_volume_controller_container").css("visibility", (self.checked ? "" : "hidden"));'/>对低音声道单独调节</label>
			<div id='lfe_channel_volume_controller_container' style='visibility:hidden'>
				低音声道音量：<span id='current_lfe_volume'></span>
				<div class='volume-bar-container'><div id='lfe_channel_volume_bar' class='volume-bar'></div></div>
				<div>
					<button>低音声道音量--</button>
					<button style='float:right;'>低音声道音量++</button>
				</div>
			</div>
		</div>
		<div>调节音量最大限制: <%=nMAX_ALLOWED_VOLUME_TO_ADJUST%>%</div>
		<div>调节音量最小限制: <%=nMIN_ALLOWED_VOLUME_TO_ADJUST%>%</div>
		<div>调节音量步伐大小: <%=nVOLUME_ADJUST_STEP%>%</div>
	</div>
	</div>

	<div id='bluetooth_container'>
	<h1>蓝牙及其输入音量控制</h1>
	<div>
		当前接入的蓝牙设备【品牌名/制造商公司名】: <span id='pulseaudio_source_name' class='data'></span><br/><span id='pulseaudio_source_port' class='data'></span>
		<div id='container_pulseaudio_bluetooth_connected'>
			蓝牙 MAC 地址:<br/>
			蓝牙 MAC 地址对应的厂商:
			<br/>
			当前音量: <span id='source_volume' class='data'></span><%-- 要不断更新，有可能从不同的源头调节音量 --%>
			<div class='volume-bar-container'><div id='source_volume_bar' class='volume-bar'></div></div>
			<div>
				<button onClick='AdjustSourceVolume ("-");'>音量--</button>
				<button onClick='AdjustSourceVolume ("+");' style='float:right;'>音量++</button>
			</div>
		</div>
	</div>
	</div>


	<div id='feedback_container'>
	<h1>留言反馈</h1>
	<div>
		<label><input type='radio' name='complain_category' value='too-loud'/>声音太大</label>
		<label><input type='radio' name='complain_category' value='too-low'/>声音太小</label>
		<label><input type='radio' name='complain_category' value='bad-taste'/>音乐类型不合口味</label>
		<label><input type='radio' name='complain_category' value='others'/>其他</label>
		<br/>
		具体内容：<br/>
		<textarea cols='80' rows='10'></textarea>
	</div>
	</div>


<script type='text/javascript'>
$('body').ready (Init);
setInterval (GetAndDisplayVolume, 2000);
</script>

</body>
</html>
