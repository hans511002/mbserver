
var js_logger_level = 'debug';
var js_logger_hidden = true;
var js_logger_parent = null;
var js_logger_init = false;


function JsLogger(){};


JsLogger.prototype.setHidden = function(flag){
	if(flag instanceof Boolean){
		js_logger_hidden = flag;
	}
	if(js_logger_hidden==true){
		var obj = document.getElementById('js_logger_panel');
		if(obj != null){
			document.body.removeChild(obj);
			alert('');
		}
	}
}

JsLogger.prototype.debug = function(msg){
	if(js_logger_init==false){
		initDrag();
		js_logger_init = true;
	}
	if(js_logger_hidden==true){
		var log = this.createPrefixMessage();
		log += msg ;
		
		this.appendLog(this.createLog(log));
	}
}

JsLogger.prototype.appendLog = function(log){
	if(js_logger_parent==null)
		this.createParent();
	js_logger_parent.appendChild(log);
	js_logger_parent.appendChild(document.createElement('br'));
	if(js_logger_parent.scrollHeight > 250){		
		js_logger_parent.scrollTop = js_logger_parent.scrollHeight;
	}
}

JsLogger.prototype.createParent = function(){
	var obj = document.getElementById('js_logger_panel');
	if(obj == null){
		obj = document.createElement('div');
		obj.id = 'js_logger_panel';
		obj.style.width='200px';
		obj.style.height='300px';
		obj.style.position = 'absolute';
		obj.style.left = '10px';
		obj.style.top = '10px';		
		obj.style.border = '2px solid red';		
		obj.style.background = 'red';
		obj.style.cursor = 'move';		
		//obj.appendChild(title);
		var body = document.createElement('div');
		body.style.width='100%';
		body.style.height = '290px';
		body.style.background = 'white';
		body.style.color = 'red';	
		body.style.overflow = 'auto';
		body.style.cursor = 'text';
		//body.style.font = '12px';
		obj.appendChild(body);
		document.body.appendChild(obj);
		js_logger_parent = body;
	}
	
}

JsLogger.prototype.createLog = function(msg){
	var spanObj = document.createElement('span');
	spanObj.innerHTML = '<strong>'+msg+'</strong>';
	return spanObj;
}

JsLogger.prototype.createPrefixMessage = function(){
	var str = '';	
	str += '['+this.createTimeStamp()+']'+':';
	return str;
}

JsLogger.prototype.createTimeStamp = function(){
	var d = new Date();
	var s = '';
	var c = ':';
  s += d.getHours() + c;
  s += d.getMinutes() + c;
  s += d.getSeconds() ;
	return s;
}



// Global holds reference to selected element
var selectedObj;
   
// Globals hold location of click relative to element
var offsetX, offsetY;
   
// Set global reference to element being engaged and dragged
function setSelectedElem(evt) {
    var target = (evt.target) ? evt.target : evt.srcElement;
    var divID = target.id;
    //target.name + "Wrap" : "";
    if (divID) {
        if (document.layers) {
            selectedObj = document.layers[divID];
        } else if (document.all) {
            selectedObj = document.all(divID);
        } else if (document.getElementById) {
            selectedObj = document.getElementById(divID);
        }
        //setZIndex(selectedObj, 100);
        return;
    }    
    selectedObj = null;
    return;
}

   
// Turn selected element on
function engage(evt) {
    evt = (evt) ? evt : event;
    setSelectedElem(evt);
    if (selectedObj) {
        if (document.body && document.body.setCapture) {
            // engage event capture in IE/Win
            document.body.setCapture();
        }
        if (evt.pageX) {
            offsetX = evt.pageX - ((selectedObj.offsetLeft) ? 
                      selectedObj.offsetLeft : selectedObj.left);
            offsetY = evt.pageY - ((selectedObj.offsetTop) ? 
                      selectedObj.offsetTop : selectedObj.top);
        } else if (typeof evt.offsetX != "undefined") {
            offsetX = evt.offsetX - ((evt.offsetX < -2) ? 
                      0 : document.body.scrollLeft);
            offsetX -= (document.body.parentElement && 
                     document.body.parentElement.scrollLeft) ? 
                     document.body.parentElement.scrollLeft : 0
            offsetY = evt.offsetY - ((evt.offsetY < -2) ? 
                      0 : document.body.scrollTop);
            offsetY -= (document.body.parentElement && 
                     document.body.parentElement.scrollTop) ? 
                     document.body.parentElement.scrollTop : 0
        } else if (typeof evt.clientX != "undefined") {
            offsetX = evt.clientX - ((selectedObj.offsetLeft) ? 
                      selectedObj.offsetLeft : 0);
            offsetY = evt.clientY - ((selectedObj.offsetTop) ? 
                      selectedObj.offsetTop : 0);
        }
        return false;
    }
}
   
// Drag an element
function dragIt(evt) {
    evt = (evt) ? evt : event;
    if (selectedObj) {
        if (evt.pageX) {        		
            shiftTo(selectedObj, (evt.pageX - offsetX), (evt.pageY - offsetY));
        } else if (evt.clientX || evt.clientY) {        		
            shiftTo(selectedObj, (evt.clientX - offsetX), (evt.clientY - offsetY));
        }
        evt.cancelBubble = true;
        return false;
    }
}
// Position an object at a specific pixel coordinate
function shiftTo(obj, x, y) {
    var theObj = obj;
    if (theObj) {
       var units = (typeof theObj.style.left == "string") ? "px" : 0;
       theObj.style.left = x + units;
       theObj.style.top = y + units;
    }
}
  
// Turn selected element off
function release(evt) {
    if (selectedObj) {        
        if (document.body && document.body.releaseCapture) {
            // stop event capture in IE/Win
            document.body.releaseCapture();
        }
        selectedObj = null;
    }
}
   
// Assign event handlers used by both Navigator and IE
function initDrag( ) {
    if (document.layers) {
        // turn on event capture for these events in NN4 event model
        document.captureEvents(Event.MOUSEDOWN | Event.MOUSEMOVE | Event.MOUSEUP);
        return;
    } else if (document.body & document.body.addEventListener) {
        // turn on event capture for these events in W3C DOM event model
        document.addEventListener("mousedown", engage, true);
        document.addEventListener("mousemove", dragIt, true);
        document.addEventListener("mouseup", release, true);
        return;
    }
    document.onmousedown = engage;
    document.onmousemove = dragIt;
    document.onmouseup = release;
    return;
}
