
Date.prototype.toString=function()
{ 
	var format=arguments[0];
	format=format?format:"";
	switch (format.toLowerCase()) { 
	   case 'yyyy-mm-dd' :
	        return this.getFullYear()+'-'+(this.getMonth()+1)+'-'+this.getDate();
	        break;
	   case 'yyyy-mm-dd hh:mi:ss' :
	        return this.getFullYear()+'-'+(this.getMonth()+1)+'-'+this.getDate()+' '+this.getHours()+':'+this.getMinutes()+':'+this.getSeconds();
			break;
	   case 'yyyy/mm/dd':
			return this.getFullYear()+'/'+(this.getMonth()+1)+'/'+this.getDate();
			break;
	   case 'dd/mm/yyyy':
			return this.getDate() +'/'+(this.getMonth()+1)+'/'+this.getFullYear();
			break;
	   case 'mm/dd/yyyy':
			return this.getMonth()+'/'+(this.getMonth()+1) +'/'+this.getFullYear();
			break;
	   case 'hh:mm:ss' :
	        return this.getHours()+':'+(this.getMonth()+1)+':'+this.getSeconds();
			break;
	   case 'yyyy年mm月dd日' :
	        return this.getFullYear()+'年'+(this.getMonth()+1)+'月'+this.getDate()+'日';
	        break;
	   default :
	       return this.toLocaleString();
	} 
}

function DateToString(sdate,format)
{ 
	if(sdate==null) return "";
	if(sdate=="") return "";
	var date;
	if(typeof(new Date())!="string")
	{
		date=sdate;
	}
	else
	{
		date=new Date(sdate);
	}
	switch (format.toLowerCase()) { 
	   case 'yyyy-mm-dd' :
	        return date.getFullYear()+'-'+(date.getMonth()+1)+'-'+date.getDate();
	        break;
	   case 'yyyy-mm-dd hh:mm:ss' :
	        return date.getFullYear()+'-'+(date.getMonth()+1)+'-'+date.getDate()+' '+date.getHours()+':'+date.getMinutes()+':'+date.getSeconds();
			break;
	   case 'yyyy/mm/dd':
			return date.getFullYear()+'/'+(date.getMonth()+1)+'/'+date.getDate();
			break;
	   case 'dd/mm/yyyy':
			return date.getDate() +'/'+(date.getMonth()+1)+'/'+date.getFullYear();
			break;
	   case 'mm/dd/yyyy':
			return date.getMonth()+'/'+(date.getMonth()+1) +'/'+date.getFullYear();
			break;
	   case 'hh:mm:ss' :
	        return date.getHours()+':'+(date.getMonth()+1)+':'+date.getSeconds();
			break;
	   case 'yyyy年mm月dd日' :
	        return date.getFullYear()+'年'+(date.getMonth()+1)+'月'+date.getDate()+'日';
	        break;
	   default :
	       return date.toLocaleString();
	} 
}



// Function Name: isValidDate
// Function Description: 判断输入是否是有效的短日期格式 - "YYYY-MM-DD"
// Creation Date: 2004-7-13 9:58
// Last Modify By: N/A
// Last Modify Date: N/A
function isValidDate(str)
{
var result=str.match(/^(\d{1,4})(-|\/)(\d{1,2})\2(\d{1,2})$/);
if(result==null) return false;
var d=new Date(result[1], result[3]-1, result[4]);
return (d.getFullYear()==result[1]&&d.getMonth()+1==result[3]&&d.getDate()==result[4]);
}

// Function Name: isValidTime
// Function Description: 判断输入是否是有效的时间格式 - "HH:MM:SS"
// Creation Date: 2004-7-13 9:58
// Last Modify By: N/A
// Last Modify Date: N/A
function isValidTime(str)
{
	var result=str.match(/^(\d{1,2})(:)?(\d{1,2})\2(\d{1,2})$/);
	if (result==null) return false;
	if (result[1]>24 || result[2]>60 || result[3]>60) return false;
	return true;
}
//判断时间
function isTime(str){
//alert(str);
if(str=="")return true;//输入可空
var reg = /^(\d{1,2}):(\d{1,2}):(\d{1,2})$/;
var r = str.match(reg);
if(r==null)return false;
var d= new Date(1900,1,1,r[1],r[2], r[3]);
if(d.getHours()!=r[1])return false;
if(d.getMinutes()!=r[2])return false;
if(d.getSeconds()!=r[3])return false;
return true;
}
/**
* 是否是日期的检查(日期格式为"yyyy-mm-dd hh:mm:ss")
* 格式：年必须输入四位数且必须在1900年以后；月日时分秒要么输入两个数字，要么输入一个数字；
* @param dateStr 被检查的字符串
* @return true(是日期格式"yyyy-mm-dd hh:mm:ss"); false(不是日期格式"yyyy-mm-dd hh:mm:ss")
*/
function isDateTime(dateStr) {
	dateStr = combChar(dateStr, " ");
	var re = /^(\d{4})\-(\d{1,2})\-(\d{1,2}) (\d{1,2}):(\d{1,2}):(\d{1,2})$/;
	var r = dateStr.match(re);
	if (r == null) {
		return false;
	}
	else {
		var str = dateStr.split(" ");
		var s = str[0].split("-");
		var strTime = str[1].split(":");
		if (s[0].substring(0,2) < 19 || s[1] > 12 || s[1] < 1 || s[2] > 31 || s[2] < 1) {
			return false;
		}
		if ((s[1] == 4 || s[1] == 6 || s[1] == 9 || s[1] == 11) && s[2] == 31) {//月小
			return false;
		}

		if (((s[0] % 4 == 0) && (s[0] % 100 != 0)) || (s[0] % 400 == 0)) { //是闰年
		    if (s[1] == 2 &&  s[2] > 29) {
				return false;
			}
		}
		else {//不是闰年
		    if (s[1] == 2 &&  s[2]>28) {
				return false;
			}
		}
		if (strTime[0] > 23 || strTime[1] > 59 || strTime[2] > 59 ) {
			return false;
		}
	}
	return true;
}
/**
* 判断日期dateStr1是否小于日期dateStr2的(日期格式为"yyyy-mm-dd")
* 格式：年必须输入四位数且必须在1900年以后；月日要么输入两个数字，要么输入一个数字；
* @param dateStr1 第一个字符串
* @param dateStr2 第二个的字符串
* @return null(dateStr1格式不对或dateStr2格式不对); true(dateStr1 < dateStr2); false(dateStr1 >= dateStr2)
*/
function isBeforeDate(dateStr1, dateStr2) {
	if (!isDate(dateStr1) || !isDate(dateStr1)) {
		return null;
	}
	var s1 = dateStr1.split("-");
	var s2 = dateStr2.split("-");
	if (s1[0] < s2[0]) {//年小于
		return true;
	}
	else if (s1[0] == s2[0]) {//年相等
		if (s1[1].charAt(0) == '0') {
			s1[1] = "" + s1[1].charAt(1);
		}
		if (s2[1].charAt(0) == '0') {
			s2[1] = "" + s2[1].charAt(1);
		}
		if (s1[1] < s2[1]) {//月小于
			return true;
		}
		else if (s1[1] == s2[1]) {//月相等
			if (s1[2].charAt(0) == '0') {
				s1[2] = "" + s1[2].charAt(1);
			}
			if (s2[2].charAt(0) == '0') {
				s2[2] = "" + s2[2].charAt(1);
			}
			if (s1[2] < s2[2]) {//日小于
				return true;
			}
		}
	}
	return false;
}
/**
* 判断日期dateStr1是否等于日期dateStr2的(日期格式为"yyyy-mm-dd")
* 格式：年必须输入四位数且必须在1900年以后；月日要么输入两个数字，要么输入一个数字；
* @param dateStr1 第一个字符串
* @param dateStr2 第二个的字符串
* @return null(dateStr1格式不对或dateStr2格式不对); true(dateStr1 = dateStr2); false(dateStr1 != dateStr2)
*/
function isEqualDate(dateStr1, dateStr2) {
	if (!isDate(dateStr1) || !isDate(dateStr1)) {
		return null;
	}
	var s1 = dateStr1.split("-");
	var s2 = dateStr2.split("-");
	if (s1[0] != s2[0]) {//年不相等
		return false;
	}
	else {//年相等
		if (s1[1].charAt(0) == '0') {
			s1[1] = "" + s1[1].charAt(1);
		}
		if (s2[1].charAt(0) == '0') {
			s2[1] = "" + s2[1].charAt(1);
		}
		if (s1[1] != s2[1]) {//月不相等
			return false;
		}
		else {//月相等
			if (s1[2].charAt(0) == '0') {
				s1[2] = "" + s1[2].charAt(1);
			}
			if (s2[2].charAt(0) == '0') {
				s2[2] = "" + s2[2].charAt(1);
			}
			if (s1[2] != s2[2]) {//日不相等
				return false;
			}
		}
	}
	return true;
}

/**
* 判断日期dateStr1是否小于日期dateStr2的(日期格式为"yyyy-mm-dd hh:mm:ss")
* 格式：年必须输入四位数且必须在1900年以后；月日时分秒要么输入两个数字，要么输入一个数字；
* @param dateStr1 第一个字符串
* @param dateStr2 第二个的字符串
* @return null(dateStr1格式不对或dateStr2格式不对); true(dateStr1 < dateStr2); false(dateStr1 >= dateStr2)
*/
function isBeforeDateTime(dateStr1, dateStr2) {
	if (!isDateTime(dateStr1) || !isDateTime(dateStr1)) {
		return null;
	}
	var s1 = dateStr1.split(" ");
	var s2 = dateStr2.split(" ");
	if (isBeforeDate(s1[0], s2[0])) {//年月日小于
		return true;
	}
	else if (isEqualDate(s1[0], s2[0])) {//年月日相等
		var strTime1 = s1[1].split(":");
		var strTime2 = s2[1].split(":");
		if (strTime1[0].charAt(0) == '0') {
			strTime1[0] = "" + strTime1[0].charAt(1);
		}
		if (strTime2[0].charAt(0) == '0') {
			strTime2[0] = "" + strTime2[0].charAt(1);
		}
		if (strTime1[0] < strTime2[0]) {//小时小于
			return true;
		}
		else if (strTime1[0] == strTime2[0]) {//小时相等
			if (strTime1[1].charAt(0) == '0') {
				strTime1[1] = "" + strTime1[1].charAt(1);
			}
			if (strTime2[1].charAt(0) == '0') {
				strTime2[1] = "" + strTime2[1].charAt(1);
			}
			if (strTime1[1] < strTime2[1]) {//分小于
				return true;
			}
			else if (strTime1[1] == strTime2[1]) {//分相等
				if (strTime1[2].charAt(0) == '0') {
					strTime1[2] = "" + strTime1[2].charAt(1);
				}
				if (strTime2[2].charAt(0) == '0') {
					strTime2[2] = "" + strTime2[2].charAt(1);
				}
				if (strTime1[2] < strTime2[2]) {//秒小于
					return true;
				}
			}
		}
	}
	return false;
}

/**
* 判断日期dateStr1是否等于日期dateStr2的(日期格式为"yyyy-mm-dd hh:mm:ss")
* 格式：年必须输入四位数且必须在1900年以后；月日时分秒要么输入两个数字，要么输入一个数字；
* @param dateStr1 第一个字符串
* @param dateStr2 第二个的字符串
* @return null(dateStr1格式不对或dateStr2格式不对); true(dateStr1 = dateStr2); false(dateStr1 != dateStr2)
*/
function isEqualDateTime(dateStr1, dateStr2) {
	if (!isDateTime(dateStr1) || !isDateTime(dateStr1)) {
		return null;
	}
	var s1 = dateStr1.split(" ");
	var s2 = dateStr2.split(" ");
	if (isEqualDate(s1[0], s2[0])) {//年月日相等
		var strTime1 = s1[1].split(":");
		var strTime2 = s2[1].split(":");
		if (strTime1[0].charAt(0) == '0') {
			strTime1[0] = "" + strTime1[0].charAt(1);
		}
		if (strTime2[0].charAt(0) == '0') {
			strTime2[0] = "" + strTime2[0].charAt(1);
		}
		if (strTime1[0] == strTime2[0]) {//小时相等
			if (strTime1[1].charAt(0) == '0') {
				strTime1[1] = "" + strTime1[1].charAt(1);
			}
			if (strTime2[1].charAt(0) == '0') {
				strTime2[1] = "" + strTime2[1].charAt(1);
			}
			if (strTime1[1] == strTime2[1]) {//分相等
				if (strTime1[2].charAt(0) == '0') {
					strTime1[2] = "" + strTime1[2].charAt(1);
				}
				if (strTime2[2].charAt(0) == '0') {
					strTime2[2] = "" + strTime2[2].charAt(1);
				}
				if (strTime1[2] == strTime2[2]) {//秒相等
					return true;
				}
			}
		}
	}
	return false;
}

//判断是否润年
function IsLeapYear(Year) 
{
	if(Math.round(Year/4) == Year/4){
		if(Math.round(Year/100) == Year/100){
			if(Math.round(Year/400) == Year/400)
				return true;
			else return false;
		}else return true;
	}
	return false;
} 

/*
获得当前日期,格式YYYY-MM-DD
*/
function getCurDate()
{
    var fun_dt = new Date();
	var fun_month = eval(1+fun_dt.getMonth());
    var fun_day = fun_dt.getDate();

	if(fun_month < 10) fun_month = "0"+fun_month;
	if(fun_day < 10) fun_day = "0"+fun_day;
	var fun_ds = fun_dt.getFullYear()+"-"+fun_month+"-"+fun_day;

	return fun_ds;
}

/********************************************************************************
   begin  年月日选择项填充
********************************************************************************/


function FillYear(yearID){
	var yearList=document.all(yearID);
	//for(var I=1990;I<2100;I++){
	for(var I=2002;I>1930;I--){
		var oOption = document.createElement("OPTION");
		oOption.text=""+I;
		oOption.value=""+I;
		if(I==1979)	oOption.selected=true;
		yearList.add(oOption);
	}
}

function FillMonth(monthID){
	var monthList=document.all(monthID);
	for(var I=1;I<13;I++){
	var oOption = document.createElement("OPTION");
		if(I<10){
			oOption.text="0"+I;
			oOption.value="0"+I;
		}else{
			oOption.text=""+I;
			oOption.value=""+I;
		}
		monthList.add(oOption);
	}
}

function FillDay(dayID,maxDay){
	var count=document.all(dayID).length;
	for(var I=0;I<count;I++)
		document.all(dayID).remove(0);
		
	for(var I=1;I<=maxDay;I++){
	var oOption = document.createElement("OPTION");
		if(I<10){
			oOption.text="0"+I;
			oOption.value="0"+I;
		}else{
			oOption.text=""+I;
			oOption.value=""+I;
		}
			
		document.all(dayID).add(oOption);
	}		
}


function YearChange(yearID,monthID,dayID){
	document.all(monthID).selectedIndex=0;
	MonthChange(yearID,monthID,dayID);
}

function MonthChange(yearID,monthID,dayID){
	var nowYear=new Number(document.all(yearID).value);
	var nowMonth=new Number(document.all(monthID).value);
	
	var maxDay=30;	
	if(IsLeapYear(nowYear))
	{
	 if (nowMonth==2)
		maxDay=29;	
	}else{
	 if (nowMonth==2)
		maxDay=28;	
	}
 	if((nowMonth==1) || (nowMonth==3) || (nowMonth==5) || (nowMonth==7) || (nowMonth==8) || (nowMonth==10) || (nowMonth==12))
		maxDay=31;
	FillDay(dayID,maxDay);
	document.all(dayID).selectedIndex=0;		
}

function GetNowDate(yearID,monthID,dayID){
	return document.all(yearID).value+document.all(monthID).value+document.all(dayID).value;
}
//when's format is YYYYMMDD
function ChangeDate(yearID,monthID,dayID,when){
	var str=new String(when);
	var year=str.substring(0,4);
	var month=str.substring(4,6);
	var day=str.substring(6,8);

	setSelect(yearID,year)
	setSelect(monthID,month)
	setSelect(dayID,day)
}	
/****************************************************************************
  end 年月日选择项填充
****************************************************************************/
