var myDate=new Date();
var time=myDate.getTime();
var socket;
var pingInterval;
var connected=false;
var msgInterval;
if (!window.WebSocket) {
	window.WebSocket = window.MozWebSocket;
}
var lineCount=0;
var lastLineCount=0;
var lineRate=0;
var msgBuffer=[];
var ta = document.getElementById('responseText');
if (window.WebSocket) {
	socket = new WebSocket("ws://"+window.location.host+"/deploy/LogsWS");
	socket.onmessage = function(event) {
		if(lineRate>10){
		    msgBuffer[msgBuffer.length]=event.data;
		}else{
			if(!ta)ta = document.getElementById('responseText');
			ta.value = ta.value  + event.data+ '\n';
			ta.scrollTop=ta.scrollHeight;
		}
		if(!msgInterval && lineCount>500){
 	        if(ta.value.length>50000)
	            ta.value = ta.value.substring(ta.value.length-50000);
	        lineCount=lastLineCount=0;
	    }
		lineCount++;
		//myDate=new Date(); 
		time=new Date().getTime();
	};
	socket.onopen = function(event) {
		var ta = document.getElementById('responseText');
		ta.value = "连接开启!";
		//myDate=new Date();
		time=new Date().getTime();
		//pingInterval=window.setInterval(ping,10000);
		connected=true;
	};
	socket.onclose = function(event) {
 		ta.value = ta.value + "连接被关闭";
		connected=false;
		//window.clearInterval(pingInterval);
	};
} else {
	alert("你的浏览器不支持 WebSocket！");
}
function msgCheck(){
    if(msgBuffer.length){
        ta.value = ta.value + msgBuffer.join("\n")+ '\n';
        ta.scrollTop=ta.scrollHeight;
        msgBuffer.length=0;
    }
    lineRate=lineCount-lastLineCount;
    lastLineCount=lineCount;
    if(lineCount>500){
        if(ta.value.length>50000)
            ta.value = ta.value.substring(ta.value.length-50000);
        lineCount=lastLineCount=0;
    }
}
function send(message) {
	if (!window.WebSocket) {
		return;
	}
	if (socket.readyState == WebSocket.OPEN) {
		socket.send(message);
	} else {
		alert("连接没有开启.");
	}
}
function ping(){
	myDate=new Date();
	if(!connected){
		window.location.reload();
	}
	if(myDate.getTime()-time>=600000){
		send("ping "+time+" "+myDate.toString("yyyy-mm-dd hh:mi:ss"));
	}
}
function sendMsg(){
    send(document.getElementById('message').value);
    document.getElementById('message').value="";
}
pingInterval=window.setInterval(ping,10000);
//msgInterval=window.setInterval(msgCheck,1000);
