//execScript("searchGuideLine()","javascript");
//刷新当前窗口
function Refresh(){location.reload();}
//刷新父窗口
function RefreshParent(){ parent.location.reload();}
//刷新打开当前的窗口
function RefreshOpener(){ opener.location.reload();}
//窗口打开函数
function winopen(url,formname)
{
    if(formname==null)
    {
        return  window.open(url,'','resizable=big,width='+screen.width-20+',height='+screen.height-40+',left=-2,top=-2,toolbar=no, status=no, menubar=no,location=no, resizable=yes, scrollbars=yes');
    }
    else
    {
       return window.open(url,formname,'resizable=big,width='+window.screen.width-20+',height='+window.screen.height-40+',left=-2,top=-2,toolbar=no, status=no, menubar=no,location=no, resizable=yes, scrollbars=yes');
    }
}
//指定的框架页面转换
function JavaScriptFrameHref(url,FrameName){
if(FrameName==null)
{location.replace(url);}
else
{FrameName.location.replace(url);}
}
//在父窗口中打开URL
function GotoParentWindow(parentWindowUrl){window.parent.window.document.location.href=parentWindowUrl;}
//替换当前窗体的打开窗口
function ReplaceOpenerWindow(openerWindowUrl){window.opener.location.replace(openerWindowUrl );}
//在当前窗体的打开窗口的父窗口的指定名称的子窗口中打开URL
// <param name="frameName">要打开URL的窗口(框架)名称</param>
// <param name="frameWindowUrl">要打开的URL</param>
function ReplaceOpenerParentFrame(frameName,frameWindowUrl){window.opener.parent.frameName.location.replace(frameWindowUrl);}
//在当前窗体的打开窗口的父窗口中打开URL
function ReplaceOpenerParentWindow(openerParentWindowUrl){window.opener.parent.location.replace(openerParentWindowUrl );}

function gotoURL(url)
{
	//location.href=url;
	window.document.location.replace(url);
}
//关闭窗口
function CloseWindow(){ top.window.close();  }
//关闭当前窗口的打开窗口
function CloseOpenerWindow(){window.opener.window.top.close();  }
//关闭子窗口
function CloseChildWindow(WindowName){ WindowName.close();}

//打开模态窗口的JS函数
function showDialogWithParams(url,parms,left,top,width,height,otherPar)
{
	return ShowDialog(url,parms,left,top,width,height,otherPar);
}
function ShowDialog(url,parms,left,top,width,height,otherPar)
{
         var features= "dialogWidth:" + (width?width:window.screen.availWidth-10) + "px"
                + ";dialogHeight:" + (height?height:window.screen.availHeight-50) + "px"
                + ";dialogLeft:" + (left?left:0) + "px"
                + ";dialogTop:" + (top?top:0) + "px";
           if(otherPar!=null)
           {
               features+=otherPar;
           }
           else
           {
                features+= ";center:yes;help:no;resizable:no;status:no;scroll:no";
           }
        	return showModalDialog(url,parms,features);
}
 
//设置窗体对象的值
function SetHtmlElementValue(formName,elementName,elementValue)
{
if(document.formName.elementName !=null){document.formName.elementName.value =elementValue;}
}
//设置HTML对象的值
function SetinnerHTMLValue(elementName,elementValue)
{
if(document.getElementById('elementName') !=null){document.getElementById('elementName').innerHTML =elementValue;}
}

// 设置对象Style属性 
function setObjStyle(obj,styname,value)
{
$(obj).style.setAttribute(styname,value);
}
//设置对象属性
function setObjArtt(obj,attrName,value)
{
$(obj).setAttribute(attrName,value);
}

//设置select 的选择值
function setSelect(id,value){
	for(var I=0;I<$(id).length;I++){
		if($(id).options(I).value==value){
			$(id).selectedIndex=I;
			break;
		}
	}		
}
function addSelect(id,value){
	for(var I=0;I<$(id).length;I++){
		if($(id).options(I).value==value){
			$(id).options(I).selected=true;
			break;
		}
	}		
}
 //(int)a/b
function intDivid(a,b)
{
	var c=a/b;
	var tmpArray=new String(c).split('.');
	return (new Number(tmpArray[0]));
}

      function NumberText()
      {
      try
       {
            if ( !(((window.event.keyCode >= 48) && (window.event.keyCode <= 57)) 
               || (window.event.keyCode == 13) || (window.event.keyCode == 46) 
               || (window.event.keyCode == 45)))
             {
                 window.event.keyCode = 0 ;
             }
       }
       catch(e)
       {}
      }
      
// 获取URL 参数
function AnalysUrl(what, chSet)
{
	if (arguments.length<2)
	{
		chSet = "[0-9A-Za-z_-]*";
	}
	var url = window.location;
	var tmpStr=new String(url);
	var splitStr=what + '=';
	var tmpArr=tmpStr.split(splitStr);
	if(tmpArr.length == 1)
		return '';
	tmpStr = tmpArr[1];
	tmpArr = tmpStr.split('&');
	var value = new String(tmpArr[0].match(new RegExp(chSet)));
	return value;
}
function getQuery(name,srcStr)
{
      var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
      if(!srcStr)
      {
      	srcStr=window.location.search.substr(1);
      }
      var r = srcStr.match(reg);
      if (r!=null) return unescape(r[2]); return null;
}

    
//填充URL参数
function FillUrl(urlStr, what, value)
{
        var tmpStr=new String(urlStr);
        var splitStr=what + '=';
        var tmpArr=tmpStr.split(splitStr);

        if(tmpArr.length == 1)
        {
                if(tmpStr.indexOf('?') < 0)
                        var returnUrl = tmpStr + '?' + what + '=' + value;
                else
                        var returnUrl = tmpStr + '&' + what + '=' + value;
        }
        else
        {
                tmpStr = tmpArr[1];
                var pos = tmpStr.indexOf('&');
                if(new Number(pos) < 0)
                        var returnUrl =  tmpArr[0] + what + '=' + value;
                else
                        var returnUrl =  tmpArr[0] + what + '=' + value + tmpStr.substring(pos);
        } 
        return returnUrl;
}

 /***************************************************
 *
 ****************************************************/
  //取随机数
function rnd() { 
	rnd.today=new Date(); 
	rnd.seed=rnd.today.getTime(); 
	rnd.seed = (rnd.seed*9301+49297) % 233280; 
	return rnd.seed/(233280.0); 
}; 

function rand(number) { 
	return Math.ceil(rnd()*number); 
}; 
 

/******************************************************************************
* Cookie functions.                                                           *
******************************************************************************/

 //设置cookie的函数
function SetCookie (name,value)   {
            var the_cookie = "'"+name + "=" + escape (value) +"'";
            var dateexpire = 'Tuesday, 01-Dec-2020 12:00:00 GMT';
            document.cookie = the_cookie + '; expires='+dateexpire;
}
//设置cookie的函数 路径和认证
function SetCookieWithPar (name,value,expires,path,domain,secure)   {   
   var argv = SetCookie.arguments;
   var argc = SetCookie.arguments.length;
   var Expires=new Date();
   if(expires!=null && expires!='')
   {
        Expires =new Date(expires);
   }
   else
   {
        Expires.setTime(Expires.getTime()+(30*24*60*60*1000));//有效日期 30天
   }
   document.cookie = name + "=" + escape (value);
   document.cookie +='; expires=' + expires.toGMTString() +
     ((path == null) ? '' : ('; path=' + path)) +
     ((domain == null) ? '' : ('; domain=' + domain)) +
	((secure == true) ? '; secure' : '');}
// 输出JS取cookie的函数
// 通过名称 /name
function GetCookieByname(sName)
{
   var aCookie = document.cookie.split(";");
   for (var i=0; i < aCookie.length; i++)
   {
     var aCrumb = aCookie[i].split("=");
     if (sName == aCrumb[0])
     return unescape(aCrumb[1]);
   }
   return null;
}
//名称 /索引 取cookie的函数 
function getCookie(name) {
  var search;
  search = name + "="
  offset = document.cookie.indexOf(search) 
  if (offset != -1) {
    offset += search.length ;
    end = document.cookie.indexOf(";", offset) ;
    if (end == -1)
      end = document.cookie.length;
    return unescape(document.cookie.substring(offset, end));
  }
  else
    return "";
}
//删除
function deleteCookie(name) {
  SetCookie(name, "");
}
/******************************************************************************
* End of cookie functions.                                                    *
******************************************************************************/
  

/*是否整数 */
function isInt(str){
	var reg = /^[1-9]*[0-9]*$/g ;
	if(reg.test(str))
		return true;
	else
		return false;
}

//复制
function JM_cc(ob){
var obj=MM_findObj(ob);
 if (obj) {
obj.select();js=obj.createTextRange();js.execCommand("Copy");}
}
//粘贴
function JM_pp(ob){
var obj=MM_findObj(ob);
 if (obj) {
obj.select();js=obj.createTextRange();
js.execCommand("Paste");}
}
//查找元素
function MM_findObj(n, d) { //v4.0
  var p,i,x;  if(!d) d=document;
  if((p=n.indexOf("?"))>0&&parent.frames.length) {
	d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document);
  if(!x && document.getElementById) x=document.getElementById(n); return x;
}
   


 /**
  *  只允许用户在输入框中输入数字
  *  调用例子<input name="xmhtje_bd" type="text" onKeyDown='onlynumber()'>
  *   键的对应数字190,110：小数点，13：回车，109,189：-,37:左箭头，38:下箭头，39:右箭头，40：上箭头，8：退格 48-57：大键盘数字，
        96-105：小键盘数字，9：TAB键，46：删除
  */
 function onlynumber()
{
	 var fun_iCode=event.keyCode;
	//alert(fun_iCode);
	if(((fun_iCode<48)||((fun_iCode>57)&&(fun_iCode<96))||fun_iCode>105)&&((fun_iCode!=8)&&(fun_iCode!=109)&&(fun_iCode!=189)&&(fun_iCode!=9)
			&&(fun_iCode!=229)&&(fun_iCode!=13)&&(fun_iCode!=110)&&(fun_iCode!=190)&&(fun_iCode!=46)&&(fun_iCode!=27))&&((fun_iCode<37)||(fun_iCode>40)))
			{
			event.returnValue=false;
			}
}
 function only09()
{
    var fun_iCode=event.keyCode;
    if(((fun_iCode<48)||((fun_iCode>57)&&(fun_iCode<96))||fun_iCode>105)
          &&((fun_iCode!=8)&&(fun_iCode!=9)
                &&(fun_iCode!=229)&&(fun_iCode!=13)&&(fun_iCode!=46)&&(fun_iCode!=27))&&((fun_iCode<37)||(fun_iCode>40)))
              {
                  event.returnValue=false;
              }
}
//取得控件的绝对位置
function getie(e){
var t=e.offsetTop;
var l=e.offsetLeft;
while(e=e.offsetParent){
t+=e.offsetTop;
l+=e.offsetLeft;
}
return [t,l];
}

//光标是停在文本框文字的最后
function cc()
{
	var e=event.srcElement;
	var r=e.createTextrange();
	r.movestart(character,e.value.length);
	r.collapse(true);
	r.select();
}
//判断上一页的来源
//document.referrer
/*
textarea自适应文字行数的多少
<textarearows=1 name=s1 cols=27 onpropertychange="this.style.posheight=
this.scrollheight">
</textarea>
*/


/////////////////////文本框编辑器///////////////////////
function insertValueQuery(txtId,text)
{
    var myQuery = $(txtId);
    if(text.trim()!="")
	{
        //IE support
        if (document.selection)
		{
            myQuery.focus();
            sel = document.selection.createRange();
            sel.text = text;
            myQuery.focus();
        }
        //MOZILLA/NETSCAPE support
        else if (myQuery.selectionStart || myQuery.selectionStart == "0")
		{
            var startPos = myQuery.selectionStart;
            var endPos = myQuery.selectionEnd;
            var chaineSql = myQuery.value;
            myQuery.value = chaineSql.substring(0, startPos) + text + chaineSql.substring(endPos, chaineSql.length);
        }
		else
		{
            myQuery.value += text;
        }
    }
    event.srcElement.focus();
}
//onfocus="selectContent( this, sql_box_locked, true )"
//onclick=insertValueQuery() ondblclick=insertValueQuery()
//////////////////////////////////////////////////////////////////////////

function isValidEmail(str)
{
var result=str.match(/^\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/);
if(result==null) return false;
return true;
}

function isURL(url){
  if(url==null)return false;
  var re = /^[a-zA-z]+:\/\/(\w+(-\w+)*)(\.(\w+(-\w+)*))*/g; //匹配url地址的正则表达式
  if(re.test(url))return true;
  return false;
}
