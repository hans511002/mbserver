package com.sobey.mbserver.push;

import com.sobey.base.BaseDao;
import com.sobey.base.exception.POException;

/**
 * 针对M_COMPANY的操作，需要序列 SEQ_COMPANY_ID
 * 
 * @author hans
 * 
 */
public class PushMsgDAO extends BaseDao<PushMsgPO> {

	public PushMsgDAO() throws POException {
		super(PushMsgPO.class);
	}

}
