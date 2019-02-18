<%@ page language="java" import="java.util.*"  import="com.bi.share.*"  import="com.ery.base.support.jdbc.*" pageEncoding="UTF-8"%>
<%@page import ="com.bi.common.*" %>
   var Btree = new MzTreeView('Btree');
   Btree.nodes['-1_root']='text:指标维度根;method:nodeClick(b);';
   Btree.icons    = {
    L0        : 'L0.gif',  //┏
    L1        : 'L1.gif',  //┣
    L2        : 'L2.gif',  //┗
    L3        : 'L3.gif',  //━
    L4        : 'L4.gif',  //┃
    PM0       : 'P0.gif',  //＋┏
    PM1       : 'P1.gif',  //＋┣
    PM2       : 'P2.gif',  //＋┗
    PM3       : 'P3.gif',  //＋━
    empty     : 'L5.gif',     //空白图
    root      : 'folder.gif',   //缺省的根节点图标
    folder    : 'folder.gif',  //缺省的文件夹图标
    file      : 'file.gif',    //缺省的文件图标
    exit      : 'exit.gif'
  };
  Btree.iconsExpand = {  //存放节点图片在展开时的对应图片
    PM0       : 'M0.gif',     //－┏
    PM1       : 'M1.gif',     //－┣
    PM2       : 'M2.gif',     //－┗
    PM3       : 'M3.gif',     //－━
    folder    : 'folderopen.gif',
    exit      : 'exit.gif'
  };
  Btree.setIconPath('../images/TreeImages/');
<%
DataAccess access=new DataAccess();
com.bi.share.DataTable typeTable=new com.bi.share.DataTable();

String  nodeStr ="";


 
String sql= "select t.dim_id,t.dim_name,t.pre_dim_id,t.state from is_dim t";

System.out.println(sql);
typeTable.Fill(sql);
for(int i=0;i<typeTable.rowsCount;i++)
{
	String value=typeTable.rows[i][0].toString().trim();
	nodeStr="";
	nodeStr+="text:"+typeTable.rows[i][1].toString().trim()+"("+value+");";
	nodeStr+="hint:"+typeTable.rows[i][1].toString().trim()+";";//6
	nodeStr+="disabled:"+!Convert.ToBool(Convert.ToInt(typeTable.rows[i][3]))+";";
	nodeStr+="method:nodeClick(b);";
	nodeStr+="value:"+typeTable.rows[i][0]+";";
	if(typeTable.rows[i][2].toString().trim().equals("0"))
	{
		out.print("Btree.nodes['root_"+typeTable.rows[i][0].toString().trim()+"']=\""+nodeStr+"\";\r\n");
	}
	else
	{out.print("Btree.nodes['"+typeTable.rows[i][2].toString().trim()+"_"+typeTable.rows[i][0].toString().trim()+"']=\""+nodeStr+"\";\r\n");
		
	}
}

%>
document.getElementById('baseMenu').innerHTML=Btree.toString();

//Btree.focus(22,true);
//Btree.currentNode.id = 22;
//Btree.expand(22,true);


//Btree.expandAll();
//Btree.currentNode=Btree.node[2];
// setTimeout( "Btree.focusClientNode('33'); ",10);
//setTimeout("Btree.focus(22, true); Btree.expand(22, true);",10);
 
 
