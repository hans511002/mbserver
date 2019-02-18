
/// 输出定位函数等,客户端用 addfollowmark("ad_dl01", X, Y); 调用 
/// addfollowmark(name, x, y)
/// 可用于多个层对象
objs = new Array();
objs_x = new Array();
objs_y = new Array();
function addfollowmark(name, x, y) {
  i = objs.length;
  objs[i] = document.getElementById(name);
  objs_x[i] = x;
  objs_y[i] = y;
}
function followmark() {
var step_ratio = 0.1;
  for(var i=0; i<objs.length; i++) {
    var fm = objs[i];
    var fm_x = typeof(objs_x[i]) == 'string' ? eval(objs_x[i]) : objs_x[i];
    var fm_y = typeof(objs_y[i]) == 'string' ? eval(objs_y[i]) : objs_y[i];

    if (fm.offsetLeft != document.body.scrollLeft + fm_x) {
      var dx = (document.body.scrollLeft + fm_x - fm.offsetLeft) * step_ratio;
      dx = (dx > 0 ? 1 : -1) * Math.ceil(Math.abs(dx));
      fm.style.left = fm.offsetLeft + dx;
    }

    if (fm.offsetTop != document.body.scrollTop + fm_y) {
      var dy = (document.body.scrollTop + fm_y - fm.offsetTop) * step_ratio;
      dy = (dy > 0 ? 1 : -1) * Math.ceil(Math.abs(dy));
      fm.style.top = fm.offsetTop + dy;
    }
    fm.style.display = '';
  }
}
action= setInterval('followmark()',10);

/// 输出定位函数
/// 客户端用 heartBeat('floater');调用  
/// floater 要定位的层
function heartBeat(id)
 {
 var step_ratio = 0.1;
self.onError=null;
currentX = currentY = 0;  
whichIt = null;           
lastScrollX = 0; lastScrollY = 0;
NS = (document.layers) ? 1 : 0;
IE = (document.all) ? 1: 0;
 var floater=document.getElementById(id);
	if(IE) { diffY = document.body.scrollTop; diffX = document.body.scrollLeft; }
	if(NS) { diffY = self.pageYOffset; diffX = self.pageXOffset; }
	if(diffY != lastScrollY) {
	percent = step_ratio * (diffY - lastScrollY);
	if(percent > 0) percent = Math.ceil(percent);
	                else percent = Math.floor(percent);
			if(IE) floater.style.pixelTop += percent;
			if(NS) floater.top += percent; 
	                lastScrollY = lastScrollY + percent;
    }
    window.setTimeout('heartBeat(\''+id+'\')',10); 
}

//关闭隐藏层函数
///  hideObj(obj);
/// 定时关闭 setTimeout("hideObj(obj);",6000);
function hideObj(obj)
{
$(obj).style.display='none';
//obj.innerHTML='';
}
/// 显示隐藏层
/// unhideObj(obj)
function unhideObj(obj)
{
$(obj).style.display='';
}

function setdiv(id)
{
	unhideObj(id);
	var top=document.body.offsetHeight-event.clientY;
    var n;
    if(top>$(id).offsetHeight+30)
    {
		n=event.clientY;
	}
	else
	{
		n=document.body.offsetHeight-$(id).offsetHeight-30;
	}
    top=document.body.scrollTop;
    top=top + n;    
    var left=document.body.offsetWidth-$(id).offsetWidth-10;
    left=left>event.clientX?event.clientX:event.clientX-$(id).offsetWidth-30+document.body.scrollLeft;
   	//hideObj('EditDiv');    
    if( left<10) left=10;
    if( top<10) top=10;
    $(id).style.top=top;    
    $(id).style.left=left;
//    unhideObj('EditDiv');
}

