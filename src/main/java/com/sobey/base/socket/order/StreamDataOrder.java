package com.sobey.base.socket.order;

import com.sobey.base.socket.SCHandler;
import com.sobey.base.util.HasThread;

//接收处理流式数据，如大文件上传
public abstract class StreamDataOrder extends HasThread {
	public abstract void processData(SCHandler hand);

}
