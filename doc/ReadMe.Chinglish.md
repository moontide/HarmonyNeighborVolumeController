<div style='text-align:right;'><a href='/doc/ReadMe.中文.md'>中文</a> | <span>Chinglish</span></div>

----

# What is Harmony Neighbor Volume Controller #
Confucius said
> People usually try to find focusing on himself when he had been single for a long time.

*No, I didn't say that.* -- Confucius

**Harmony Neighbor Volume Controller can let your neighbor adjust your computer playback volume from a web interface.**
If your volume from your computer (which is running Linux on it and use PulseAudio as sound server) is too loud (or too low), your neighbor may complain about it. Then, your neighbor can adjust the volume via this web controller to fit his demand.


Usually, I suggest to run this controller with a wireless router which is running OpenWRT or DD-WRT or other roms which can setup a captive portal.
* Setup a wireless interface in router.
* Setup a captive portal, and let this portal serve the wireless interface above, and config captive portal to allow any IP address to access IP address of your computer (default port is 80 and/or 8080 depends on how Tomcat or similar servers were configured)
* Embed an iframe in Captive Portal web page, the src of iframe is the URL of this application on your computer.
* Then your neighbor can adjust your computer playback volume from this application if he/she connect to the SSID you created.
