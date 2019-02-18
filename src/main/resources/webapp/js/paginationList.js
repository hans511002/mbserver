function readListData(wheres,orderby,funName,endFun)
{
    funName(PageSize, CurrenPagenum,wheres, orderby, function(res)
    {
     	$('GridView').innerHTML =res[0];
		PageCount=res[1]!=0?res[1]:1;		
		RowCount=res[2];
		if(PageSize>0)
		{
        	$('PageDes').innerHTML ="总条"+RowCount+"条记录,每页"+PageSize+"条,当前第"+CurrenPagenum+"/"+PageCount+"页";
         	bandPageSelect();
        	setPager();
        }
        else
        {
        	if($("page_down_tr"))
        		$("page_down_tr").style.display="none";
        }
        if(endFun)eval(endFun);
    });
}
function bandPageSelect()
{
    if($('PageDropDownList').length==PageCount)
    {
    	$('PageDropDownList').selectedIndex =CurrenPagenum-1;
        return;
    }
    $('PageDropDownList').length=0;
    for(i=1 ;i<=PageCount;i++)
    {
       $('PageDropDownList').options[document.getElementById('PageDropDownList').length] = new Option('第'+i+'页',i);
       if(CurrenPagenum==i)
       {
       		$('PageDropDownList').selectedIndex =i-1;
       }
    }   
}
function changePage()
{
	CurrenPagenum=$('PageDropDownList').value;
	LoadGuideList(wheres,orderby);
}
function getFirstPage() 
{
 CurrenPagenum=1;
 LoadGuideList(wheres,orderby);
}
function getNextPage() 
{
CurrenPagenum++; 
if(CurrenPagenum>PageCount) CurrenPagenum=PageCount;
 LoadGuideList(wheres,orderby);
}
function  getPreiPage()
{
CurrenPagenum--;
if(CurrenPagenum<=0) CurrenPagenum=1;
 LoadGuideList(wheres,orderby);
} 
 function getLastPage() 
 {
 CurrenPagenum=PageCount;
 LoadGuideList(wheres,orderby);
}
 
function setPager()
{
    document.getElementById("getFirstPage").disabled =false;    
    document.getElementById("getPreiPage").disabled =false;    
    document.getElementById("getNextPage").disabled =false;    
    document.getElementById("getLastPage").disabled =false;    
    document.getElementById("getFirstPage").href='#';
    document.getElementById("getFirstPage").onclick=function(evel){getFirstPage();return false;};
    document.getElementById("getPreiPage").href='#';
    document.getElementById("getPreiPage").onclick=function(evel){getPreiPage();return false;};
    document.getElementById("getNextPage").href='#';
    document.getElementById("getNextPage").onclick=function(evel){getNextPage();return false;};
    document.getElementById("getLastPage").href='#';
    document.getElementById("getLastPage").onclick=function(evel){getLastPage();return false;};
    if(CurrenPagenum==PageCount && CurrenPagenum==1)
    {
        document.getElementById("getFirstPage").disabled =true;    
        document.getElementById("getPreiPage").disabled =true;    
        document.getElementById("getNextPage").disabled =true;    
        document.getElementById("getLastPage").disabled =true;    
        document.getElementById("getFirstPage").onclick=null;
        document.getElementById("getPreiPage").onclick=null;
        document.getElementById("getNextPage").onclick=null;
        document.getElementById("getLastPage").onclick=null;
        return;
    }
    if(CurrenPagenum==PageCount)
    {
        document.getElementById("getNextPage").onclick=null
        document.getElementById("getNextPage").disabled =true;
        document.getElementById("getLastPage").onclick=null
        document.getElementById("getLastPage").disabled =true;
        return;
    }
    if(CurrenPagenum==1)
    {
        document.getElementById("getFirstPage").onclick=null;
        document.getElementById("getFirstPage").disabled =true;    
        document.getElementById("getPreiPage").onclick=null;
        document.getElementById("getPreiPage").disabled =true;    
        return;
    }
}