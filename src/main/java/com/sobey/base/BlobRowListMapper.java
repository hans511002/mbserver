package com.sobey.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.sobey.jcg.support.jdbc.mapper.AbstractListMapper;

/**
 * Copyrights @ 2011,Tianyuan DIC Information Co.,Ltd. All rights reserved.<br>
 *
 * @author 张伟
 * @description 作用
 * @date 2012-08-29
 */
public class BlobRowListMapper extends AbstractListMapper<Map<String, Object>> {

	/**
	 * @param clazz
	 * @param mapper
	 */
	public BlobRowListMapper() {
		super(null, new BlobRowMapper());
	}

	public BlobRowListMapper(String encode) {
		super(null, new BlobRowMapper(encode));
	}

	@Override
	public Map<String, Object> convertRow(ResultSet rs) throws SQLException {
		return mapper.convertRow(rs);
	}
}
