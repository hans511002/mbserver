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
var taskIdOp = document.getElementById('taskId');
if (window.WebSocket) {
	socket = new WebSocket("ws://"+window.location.host+"/deploy/taskLog");
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
var  curTaskId;
var taskList={};
function getTaskList(){
	var http=new HTTPRequest();
	http.open("http://"+window.location.host+"/deploy/getAsyncTaskList?order=info");
	http.send();
	eval("var res="+http.response);
	if(res && res.code==0 && res.result){
	    res=res.result;
	    taskList=res;
	    taskIdOp.innerHTML="";
	    var innerHTML="<option value=''></option>";
	    for(var k in res){
	        innerHTML+="<option value='"+k+"'>"+k+"  "+res[k]["status"] +"</option>";
	    }
	    taskIdOp.innerHTML=innerHTML;
	}
}
getTaskList();
function sendMsg(){
    send(document.getElementById('message').value);
    document.getElementById('message').value="";
}
function changeLog(){
    curTaskId=taskIdOp.value;
    ta.value ="";
    send("getTaskLog "+curTaskId);
    var innerHTML="<strong>taskId: </strong>"+curTaskId;
    innerHTML+="<br/><strong>status: </strong>"+taskList[curTaskId]["status"];
    var dt=new Date(taskList[curTaskId]["addTime"]);
    innerHTML+="<br/><strong>addTime: </strong>"+dt;
    dt=new Date(taskList[curTaskId]["startTime"]);
    innerHTML+="<br/><strong>startTime: </strong>"+dt;
    dt=new Date(taskList[curTaskId]["endTime"]);
    innerHTML+="<br/><strong>endTime: </strong>"+dt;
    innerHTML+="<br/><strong>logFIle: </strong>"+taskList[curTaskId]["logFile"];
    innerHTML+="<br/><strong>msg: </strong>"+taskList[curTaskId]["result"];
    
    var taskInfo=document.getElementById('taskInfo');
    taskInfo.innerHTML=innerHTML;
}

pingInterval=window.setInterval(ping,10000);
//msgInterval=window.setInterval(msgCheck,1000);
