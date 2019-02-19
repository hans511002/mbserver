function checkSum(obj)
{   
  var   max=obj.style.maxlength;   
  if(strByteLen(obj.value)>max)
  {  
  	obj.focus();   
    obj.select();  
  	alert("您在这里输入的字符允许最大长度为"+max);   
  }   
} 