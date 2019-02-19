// JScript 文件
function setdiv(obj)
{
	unhideObj(obj);
	var e=event.srcElement;
 	var val=getie(e);
	var top=document.body.offsetHeight-(val[0]+e.offsetHeight-document.body.scrollTop);//event.clientY;
    var n=0;
    if(top > $(obj).offsetHeight+5)
    {
		top=val[0]+e.offsetHeight-5; // n=event.clientY+document.body.scrollTop;
	}
	else
	{
		top=val[0]-$(obj).offsetHeight+2;
		//n=document.body.offsetHeight-$(obj).offsetHeight-10+document.body.scrollTop;
	}
    var left=document.body.offsetWidth-(val[1]-document.body.scrollLeft);
    //alert(left+"__"+e.offsetWidth);
    left=left>$(obj).offsetWidth?val[1]+5:val[1]-$(obj).offsetWidth-5+e.offsetWidth;
    //alert(left+"__"+e.offsetWidth);
  	//hideObj('EditDiv');
    if( left<10) left=10;
    if( top<10) top=10;
    $(obj).style.top=top;
    $(obj).style.left=left;
//    unhideObj('EditDiv');
}

var tmpElement=null;
var dragElement=null;
var downX,downY,tmp_o_x,tmp_o_y;
var dragActive=0;
var draging=0;
var isClone=true;
var dragZindex=0;
function readyDrag(isInForm,ParLevel,id,NotClone)
{
	dragActive=1;
	if(NotClone) isClone=false;
    if(id)
	{
		if(typeof(id) =="string")
		{
			if(document.getElementById)dragElement=document.getElementById(id);
			else if(document.all)dragElement=document.all(id);
		}
		else
		{
			dragElement=id;
		}
	}
	else
	{
		dragElement=event.srcElement;
		for(i=ParLevel;i>0;i--)
		{
			dragElement=dragElement.parentNode;
		}		
	}
	if(dragElement.tagName.toUpperCase()!="DIV")
		return;	
	if(NotClone)tmpElement=dragElement;
	else tmpElement=dragElement.cloneNode(true);
	tmpElement.style.filter="alpha(opacity=80)";
	dragZindex=tmpElement.style.zIndex;
	if(!NotClone)tmpElement.style.zIndex=dragElement.style.zIndex+1;
	
	tmpElement.style.position="absolute";
	if(dragElement.offsetParent.tagName!="BODY")
	{
		dragElement.style.left=dragElement.offsetLeft+dragElement.parentNode.style.pixelLeft;
		dragElement.style.top=dragElement.offsetTop+dragElement.parentNode.style.pixelTop;
    }
	downX=event.clientX;
	downY=event.clientY;
	tmp_o_x=dragElement.style.pixelLeft;
	tmp_o_y=dragElement.style.pixelTop;
	dragElement.parentNode.appendChild(tmpElement);
/*	if(isInForm)
	{
		document.forms[0].appendChild(tmpElement);
	}
	else
	{
		document.body.appendChild(tmpElement);
	}
	*/
	document.onmousemove=startDrag;	
	//tmpElement.attachEvent('onmousemove',startDrag);
	//window.document.attachEvent('oncontextmenu',contextmenu); ";
	document.onmouseup=endDrag;
}
function startDrag()
{
	if(dragActive==1&&event.button==1&&dragElement!=null&&tmpElement!=null)
	{
		tmpElement.style.visibility="visible";
		tmpElement.style.left=tmp_o_x+event.clientX-downX;
		tmpElement.style.top=tmp_o_y+event.clientY-downY;
		if(isClone)dragElement.style.backgroundColor="#CCCCCC";
		document.body.style.cursor="move";
		draging=1;		        
        document.onselectstart=function(){return false;}
	} 
}
function endDrag()
{ 
	if(dragActive==1&&tmpElement!=null)
	{
		tmpElement.style.filter="alpha(opacity=100)";
		tmpElement.style.zIndex=dragZindex;
		document.body.style.cursor="default";
		if(draging==1)
		{
			if(isClone)dragElement.removeNode(true);
			tmpElement.style.display='';
			draging=0;
		}
        document.onselectstart=function(){return true;}
	}
	document.onmousemove=function(){return true;};	
	document.onmouseup=function(){return true;};
	dragElement=null;
	tmpElement=null;
	refElement=null;
	dragActive=0;
}
 
function hideObj(obj)
{
	$(obj).style.display='none';
}
function unhideObj(obj)
{
	$(obj).style.display='';
}
