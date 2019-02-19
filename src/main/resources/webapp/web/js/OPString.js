  
//空检查
function isNull(str) {
	if (str == null || str == 'null') {
		return '';
	}
	else {
		return str;
	}
}
 
 function isHtmlNull(str) {
	if (str == null || str == 'null') {
		return '&nbsp;';
	}
	else {
		return str;
	}
}

  //去除字符串的空格  
  
  function CheckStrNull(str)
  {
     var temp="";
	 for(var i=0;i<str.length;i++)
  	 {
       if(str.substring(i,i+1)!=" ")
	  {
          temp+=str.substring(i,i+1);
       }
 	 }
	 return temp;
  }
  
  //检查字符串是否为空格或空
  
  function IsNullStr(str)
  { 
  if(str == null) return true;
       var temp="";
  	  for(var i=0;i<str.length;i++)
  	 {
       if(str.substring(i,i+1)!=" ")
	   {
          temp+=str.substring(i,i+1);
       }
 	 }
	 if(temp=="")
	 {
	   return true;
	 }
	 else
	 {
	   return false;
	 }
  }
    
//去除字符串中前后空格

function trim(str){
    var rp1=/(\s*)(.*\b)(\s*)/g;
    var rp2=/(\s*)/g;
    str=str.replace(rp1,"$2");
    return str.replace(rp2,"")==""?"":str;
}
String.prototype.trim=function ()
{
	return this.replace(/(^\s*)|(\s*$)/g,"");
}

//去除字符串左边(前)空格

function ltrim(str){
    var rp1=/(\s*)(.*\b)(\s*)/g;
    var rp2=/(\s*)/g;
    str=str.replace(rp1,"$2$3");
    return str.replace(rp2,"")==""?"":str;
}

//去除字符串右边(后)空格

function rtrim(str){
    var rp1=/(\s*)(.*\b)(\s*)/g;
    var rp2=/(\s*)/g;
    str=str.replace(rp1,"$1$2");
    return str.replace(rp2,"")==""?"":str;
}

/// 字符串字节长度

function strByteLen(str) {
    var fun_len = 0;
    var fun_i=0;
    if (str==null){
      return 0;
    }
    fun_len = str.length;
    for (fun_i = 0; fun_i < fun_len; fun_i++) {
        if (str.charCodeAt(fun_i) < 0 || str.charCodeAt(fun_i) > 255) {
         fun_len++;
      }
    }
    return fun_len;
}
// Function Name: len
// Function Description: 返回字符串的实际长度, 一个汉字算2个长度
// Creation Date: 2004-7-13 9:58
// Last Modify By: N/A
// Last Modify Date: N/A

function lenbyte(str)
{
return str.replace(/[^\x00-\xff]/g, "**").length;
}

/// 字符串字符数

function strLen(str){ return str.length;}


//判断是否是ASCII字符

function IsAscii(ch)
{
	if(ch > 0 && ch <= 255)
	{
		return true;
	}
	else
	{
		return false;
	}
}
/**********************************************************
	根据需要的长度将字符串截断
	input : str要截断的字符串 length要截断的长度
	output: 截断后的字符串。
	如果截断后字符串长度比要求的大一，则末尾为一个英文字母加一个汉字的形式，将后面的汉字截掉
	如...d号＝＝》...d ，以保证长度不超长
*/
function getFmtStr(str,length)
{
	var len,idx;
	len = 0;
	idx = 0;
	if(length>=strByteLen(str)) return str;
	var str_result = "";
	length=length-1;
	while (len < length)
	{
		str_result += str.charAt(idx);
		if(IsAscii(str.charCodeAt(idx)))
		{
			len += 1;
		}
		else
		{
			len += 2;
		}
		idx += 1;
	}
	if(len > length)
	{
		str_result = str_result.substr(0,str_result.length-1);
	}
	return str_result+"...";
}
 
//检测字符串是否为空或空格

function IsNullStr(str)
  { 
       var temp='';
  	  for(var i=0;i<str.length;i++)
  	 {
       if(str.substring(i,i+1)!=' ')
	   {
          temp+=str.substring(i,i+1);
       }
 	 }
	 if(temp=="")
	 {
	   return true;
	 }
	 else
	 {
	   return false;
	 }
  }
  
  //移除字符串中的空格
  
  function ReMoveStrNull(str)
  {
     var temp='';
	 for(var i=0;i<str.length;i++)
  	 {
       if(str.substring(i,i+1)!=' ')
	  {
          temp+=str.substring(i,i+1);
       }
 	 }
	 return temp;
  }
  //----------------------------------------------------除S以外的字符
function isCharsInBagEx (s, bag)
{
    var i,c;
    for (i = 0; i < s.length; i++)
    {
        c = s.charAt(i);
        if (bag.indexOf(c) > -1)  return c;
    }
    return "";
}
//---------------------------------------------------检验字符串中是否含有禁止的字符！如： '<>/?&#=
//如果为空或超出长度或含有禁止的字符，则返回 true ； 合法则返回 false
function isBadCharInName(s){
    var errorChar;
    var badChar = "><,[]{}?/+=|\\'\":;~!@#$%^&()`";
    if (isEmpty(s)){
        alert("内容为空，请重新输入！");
        return true;
    }
    errorChar = isCharsInBagEx(s, badChar)
    if (errorChar != "" ){
        alert("您输入的内容" + s+"是无效的,\n\n请不要在其中输入字符" + errorChar + "!\n\n请重新输入合法的内容！" );
        return true;
    }
    if (s.length<1 || s.length>50){
        alert("必须在1至50个字符之间");
        return true;
    }

    return false;
}




/**
* 合并紧挨着的相同的字符
* @param str 被合并的字符串
* @param strChar 字符
* @return 完成合并后的字符串
*/
function combChar(str, strChar) {
	if (strChar == null || strChar == "") {
		return str;
	}
	var len = str.length;
	var index = 0;//上次匹配的地方
	var isFirst = "false";//第一个字符开始的子串是否与strChar匹配
	for (i = 0; i < len; i++) {
		if (strChar.length > (len - i)) {
			break;
		}
		var strTemp = str.substring(i, i + strChar.length);
		if (strTemp == strChar) {
			if (i == 0) {
				isFirst = "true";
			}
			//如果这次匹配与上次匹配是连续匹配
			if (index == (i - strChar.length)) {
				//如果上次匹配的地方不为0，或者上次匹配的地方是第一个字符，才是连续匹配
				if (index != 0 || isFirst == "true") {
					str = str.substring(0,index) + str.substring(index + strChar.length);
					len = len - strChar.length;
					i = i - strChar.length;
				}
			}
			index = i;
			i = i + strChar.length - 1;
		}
		else {
			index = 0;
		}
	}
    return str;
}


//alert(combChar("2004200420042004aaaa","2004"));
/**
 * 判断是否是文件地址，即是否输入了文件不能解析的字符
 * @param str String
 * @return boolean
 */
function isFileAddress(str) {
    if(str==null)return false;
    if(str.indexOf("/")>=0)return false;
    else if (str.indexOf("\\")>=0)return false;
    else if (str.indexOf(":")>=0)return false;
    else if (str.indexOf("*")>=0)return false;
    else if (str.indexOf("?")>=0)return false;
    else if (str.indexOf("<")>=0)return false;
    else if (str.indexOf(">")>=0)return false;
    else if (str.indexOf("|")>=0)return false;
  return true;
}

/**
 * 判断是否有尖括号和单引号
 * @param str String
 * @return boolean
 */
function hasNotSharpAndSinglequotes(str) {
  if (str == null) {
    return true;
  }
  if (str.indexOf("<") >= 0) {
    return false;
  }
  else if (str.indexOf("'") >= 0) {
    return false;
  }
  return true;
}
/**
* 检测是否是英文字母
* @param str 被检查的字符串
* @return true; false
*/
function isEnglish(str) {
	var Letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	for (i = 0; i < str.length; i++) {
		var checkChar = str.charAt(i);
		if (Letters.indexOf(checkChar) == -1) {
			return false;
		}
	}
	return true;
}

/**
* 检测是否是合法的名字（字母，数字，下划线，且第一个字符不能为数字）
* @param str 被检查的字符串
* @return true; false
*/
function isValidName(str) {
	var Letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_";
	for (i = 0; i < str.length; i++) {
		var checkChar = str.charAt(i);
		if (Letters.indexOf(checkChar) == -1) {
			return false;
		}
		if (i == 0 && isInteger(checkChar)) {
			return false;
		}
	}
	return true;
}
 
 //替换字符串函数      srcStr:源 desDelim:目标字符串 srcDelim:要替换的源字符串
function ConvStr(srcStr, desDelim, srcDelim)
{
	var tmpStr = new String(srcStr);
	var tmpArray = tmpStr.split(srcDelim);
	var tmp='';
	for(var i=0;i<tmpArray.length;i++)
	{
		if(i == 0)
			tmp += tmpArray[i];
		else
			tmp += desDelim+tmpArray[i];
	}	
	return tmp;
}   
String.prototype.replaceAll=function (olds,news,ignore)
{
	var re=null ;
	if(ignore)
	{
		re = new RegExp(olds,"ig");
	}
	else
	{
		re = new RegExp(olds,"g");
	}
	return this.replace(re,news);
}
 function urlHandle(urlStr) {
	urlStr = urlStr.replaceAll("\\%","%25");
	urlStr = urlStr.replaceAll("\\#","%23");
	urlStr = urlStr.replaceAll("\\&","%26");
	urlStr = urlStr.replaceAll("\\+","%2B");
	//urlStr = urlStr.replaceAll("\\","%2F");
	urlStr = urlStr.replaceAll("\\=","%3D");
	urlStr = urlStr.replaceAll("\\?","%3F");
	return urlStr;
}


//在JAVASCRIPT中替换左右尖括号 < >  "  '  \  &
function maskHTMLCode(str) {
  if(str!=null){
      str = replaceAll(str,"&","&amp;");
      str = replaceAll(str,"<","&lt;");
      str = replaceAll(str,">","&gt;");
      str = replaceAll(str,"\"","&quot;");
      str = replaceAll(str,"'","&apos;");
  }
  return str;
} 
function unmaskHTMLCode(str) {
  if(str!=null){
      str = replaceAll(str,"&amp;","&");
      str = replaceAll(str,"&lt;","<");
      str = replaceAll(str,"&gt;",">");
      str = replaceAll(str,"&quot;","\"");
      str = replaceAll(str,"&apos;","'");
  }
  return str;
} 

function HTMLEncode(str) {
  if(str!=null){ 
      str = replaceAll(str,"\r\n","<br/>");
      str = replaceAll(str,"\n","<br/>");
      str = replaceAll(str,"\r","<br/>"); 
      str = replaceAll(str,"\t","&nbsp;"); 
      str = replaceAll(str," ","&nbsp;"); 
  }
  else
  {
  	str="";
  }
  return str;
}
function HTMLDecode(str) {
  if(str!=null){ 
      str = replaceAll(str,"<br/>","\r\n");
      str = replaceAll(str,"<br>","\r\n");
      str = replaceAll(str,"&nbsp;"," "); 
  }
  else
  {
  	str="";
  }
  return str;
}

function replaceAll(str,olds,news)
{
    if(str!=null)
    //str=str.replace(/olds/g, news);
	while(str.indexOf(olds)>=0)
	{
		str=str.replace(olds,news);
	}
	return str;
}
 