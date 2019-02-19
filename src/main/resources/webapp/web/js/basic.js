function $() 
{ 
 	var elements = new Array();  
	for (var i = 0; i < arguments.length; i++) 
	{ 
		var element = arguments[i]; 
		if (typeof element == 'string')
		{ 
			if (document.getElementById)
				element = getObjectById(element);
			else if (document.all)
				element = document.all(id);
			else
		  		element = eval(id);
		}
		if (arguments.length == 1) 
		  return element; 
		elements.concat(element);
	}
	return elements; 
}
function $N() 
{ 
  	var elements = new Array();  
  	for (var i = 0; i < arguments.length; i++) 
	{
   		var element = arguments[i]; 
	    if (typeof element == 'string') 
      		element =document.getElementsByName(element);
    	if (arguments.length == 1) 
      		return element;
    	elements.concat(element); 
  	} 
	return elements; 
}
function $T() 
{ 
  var elements = new Array();  
  for (var i = 0; i < arguments.length; i++) 
  { 
    var element = arguments[i]; 
    if (typeof element == 'string') 
      element =document.getElementsByTagName(element); 
    if (arguments.length == 1) 
      return element;
    elements.concat(element); 
  } 
  return elements; 
 }
function getObjectById(id)
{
    if (typeof(id) != "string" || id == "") return null;    
    if (document.getElementById) return document.getElementById(id);
    if (document.all) return document.all(id);
    try {return eval(id);} catch(e){ return null;}
} 

function SymError()
{
  return true;
}
 
function KeyDown() 
{
	if(typeof(checkEnter)=="function")checkEnter();
	 //(event.keyCode==8)  || //屏蔽退格删除键
    if ((window.event.altKey)&&((window.event.keyCode==37)|| (window.event.keyCode==39)))
    {  
        alert('不准你使用ALT+方向键前进或后退网页！');
        event.returnValue=false; 
     } 
     if (  (event.keyCode==116)||                
      (event.keyCode==112)||    
      (event.ctrlKey && event.keyCode==82))  
     {
        event.keyCode=0;
        event.returnValue=false;
     }
     if ((event.ctrlKey)&&(event.keyCode==78))  
     event.returnValue=false;
     if ((event.shiftKey)&&(event.keyCode==121)) 
     event.returnValue=false;
     if (window.event.srcElement.tagName.toUpperCase()  == 'A' && window.event.shiftKey) 
      window.event.returnValue = false;                            
    if ((window.event.altKey)&&(window.event.keyCode==115))             
    { 
      window.showModelessDialog('about:blank','','dialogWidth:1px;dialogheight:1px');
      return false;
	  }
	
} 
function contextmenu()
{
  	return false;
}
function clone (obj)
{
	if(obj.constructor==Array)
	{
		var newArr=new Array();
		for(var key in obj)
		{
			newArr[key]=obj[key];
		}
		return newArr;
	}
	else
	{
		var newArr={};
		for(var key in obj)
		{
			newArr[key]=obj[key];
		}
		return newArr;
	}
}
//window.onerror = SymError;
//window.document.attachEvent('onkeydown',KeyDown);
//window.document.attachEvent('oncontextmenu',contextmenu);
