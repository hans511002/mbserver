package com.sobey.mbserver.db.dao;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sobey.jcg.support.jdbc.AsciiStream;
import com.sobey.jcg.support.jdbc.BinaryStream;
import com.sobey.jcg.support.jdbc.CharacterStream;
import com.sobey.jcg.support.jdbc.DataTable;
import com.sobey.jcg.support.jdbc.IParamsSetter;
import com.sobey.jcg.support.jdbc.JdbcException;
import com.sobey.jcg.support.jdbc.mapper.BeanMapper;
import com.sobey.jcg.support.sys.podo.BaseDAO;

public class TestDAO extends BaseDAO {

	public List<TestPO> queryForList() {
		return getDataAccess().queryForBeanList("SELECT * FROM META_MAG_ROLE", TestPO.class);
	}

	public TestPO[] queryForArray() {
		return getDataAccess().queryForBeanArray("SELECT * FROM META_MAG_ROLE_BAK ", TestPO.class);
	}

	public TestPO queryForBean() {
		return getDataAccess().queryForBean("SELECT * FROM META_MAG_ROLE_BAK WHERE ROLE_ID IS NULL", TestPO.class);
	}

	public String getSQLMessage(JdbcException e) {
		String msg = null;
		Exception exception = e.getException();
		if (exception instanceof SQLException) {
			msg = exception.getMessage();
		}
		return msg;
	}

	public void test() {
		try {
			super.getDataAccess("config1");
		} catch (JdbcException e) {
			System.out.println(getSQLMessage(e));
		}

	}

	public int queryForInt() {
		String sql = "select -1 from dual";
		return super.getDataAccess("config1").queryForInt(sql);
	}

	public String queryForString() {
		return super.getDataAccess().queryForString("SELECT CODE_ITEM  FROM  META_SYS_CODE A WHERE A.CODE_ID =null AND 1=?", 1);
	}

	public Map<String, Object> queryForMap() {
		String sql = "SELECT M.DIM_TABLE_ID,M.DIM_COL_ID FROM META.META_TABLE_COLS M WHERE M.COL_ID = 5288";
		return super.getDataAccess().queryForMap(sql);
	}

	public void getDBMetaData() throws SQLException {
		DatabaseMetaData metaData = super.getDataAccess().getConnection().getMetaData();
		ResultSet rs = metaData.getTables(null, null, null, null);
		System.out.println(rs.getMetaData().getColumnCount());
		while (rs.next()) {
			System.out.println(rs.getObject(1) + "-->" + rs.getObject(2) + "-->" + rs.getObject(3) + "-->" + rs.getObject(4) + "-->" + rs.getObject(5));
		}
		rs.close();
	}

	/**
	 * 测试方法，取得角色列表
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getRoleList(int roleId) {
		String sql = "SELECT * FROM META_MAG_ROLE";
		return super.getDataAccess("config1").queryForList(sql);
	}

	public DataTable queryForDataTable() {
		String sql = "SELECT * FROM META_MAG_ROLE_BAK";
		return super.getDataAccess("config1").queryForDataTable(sql);
	}

	public void exeUpdate() {

		// String sql = "insert into META_REPORT(REPORT_ID,REPORT_NAME,SHOW_NAME_FLAG,REPORT_DESC,USER_ID," +
		// " QUERY_TERM_IDS,TERM_ROW_SIZE,USER_DEFINE_CSS,STATE,CLIENT_JS) values(?,?,?,?,?, ?,?,?,1,?)";
		// BinaryStream js = new BinaryStream();
		// String cjs = "11111111111111111111111111111111111";
		// byte[] bytes = cjs.getBytes();
		// java.io.ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		// js.setInputStream(bais,bytes.length);
		// this.getDataAccess().execUpdate(sql, 1
		// , "sssssssssssssss"
		// , 1
		// , "sssssssssssssss"
		// , 1
		// , "sssssssssssssss"
		// , 500
		// , "sssssssssssssss"
		// , js
		// );

		String sql = "INSERT INTO META_MAG_ROLE_BAK(ROLE_ID, ROLE_NAME, ROLE_DESC, ROLE_STATE, CREATE_DATE, FILE_BLOB, TEXT_CLOB,TEXT_NCLOB) VALUES(?,?,?,?,? ,?,?,?)";
		try {
			Object[] params = new Object[] { 1, 2, 3, 4, new Date(System.currentTimeMillis()), new BinaryStream(new ByteArrayInputStream("ABCDEFG".getBytes())), // blob
			                                                                                                                                                     // 二进制
			        new AsciiStream(new FileInputStream("D:\\tt.txt")), // clob ASCII字符
			        new CharacterStream(new BufferedReader(new InputStreamReader(new ByteArrayInputStream("测试测试".getBytes()), "UTF-8"))) // nclob,可自定义字符集
			};
			getDataAccess("config1").execUpdate(sql, params);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 测试批量更新
	 * 
	 * @return
	 */
	public int[] insertBatch() {
		String sql = "INSERT INTO META_MAG_ROLE_BAK(ROLE_ID, ROLE_NAME, ROLE_DESC, ROLE_STATE, CREATE_DATE, FILE_BLOB) VALUES(?,?,?,?,?,?)";

		return getDataAccess("config1").execUpdateBatch(sql, 100, new IParamsSetter() {
			java.sql.Date date = new java.sql.Date(System.currentTimeMillis());

			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setObject(1, i);
				ps.setObject(2, i);
				ps.setObject(3, i);
				ps.setObject(4, i);
				ps.setDate(5, date);
				try {
					ps.setBinaryStream(6, new FileInputStream("D:\\tt.rar"));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				if (i % 100 == 0) {
					System.out.println(i);
				}
			}

			public int batchSize() {
				return 1000;
			}
		});
	}

	// public int execQueryCall() {
	// int rtn = 0;
	// String sql = "call my_pack.getMyCursor(?)";
	// ResultSet rs = null;
	// try {
	// CallableStatement statement = super.getDataAccess().execQueryCall(sql, new DBOutParameter(OracleTypes.CURSOR));
	// rs = (ResultSet) statement.getObject(1);
	// if (rs.next()) {
	// Clob clob = rs.getClob(8);
	// System.out.println(clob.getSubString(0, (int) clob.length()));
	// }
	// } catch (SQLException e) {
	// e.printStackTrace();
	// } finally {
	// super.getDataAccess().close(rs);
	// }
	// return rtn;
	// }

	/************************ 测试DAO方法 start *************************/
	public Object[][] queryForArrary(boolean bool) {
		String sql = "SELECT A.USER_ID,LOG_ID,CHANGE_TIME,CHANGE_TYPE FROM META_MAG_TEST A LEFT JOIN META_MAG_USER B "
		        + "ON A.USER_ID = B.USER_ID WHERE B.STATION_ID=? ";
		return getDataAccess().queryForArray(sql, bool, "22");
	}

	public String[] queryForPrimitiveArray(int userId) {
		String sql = "SELECT A.USER_ID,LOG_ID,CHANGE_TIME,CHANGE_TYPE,EDITOR_TYPE,EDITOR_ID FROM META_MAG_TEST A WHERE USER_ID= ? ";
		return getDataAccess().queryForPrimitiveArray(sql, String.class, userId);
	}

	public MeteTestPO[] queryForBeanArray(int userId) {
		String sql = "SELECT A.USER_ID,LOG_ID,CHANGE_TIME,CHANGE_TYPE,EDITOR_TYPE,EDITOR_ID FROM META_MAG_TEST A WHERE A.USER_ID = ? ";
		return getDataAccess().queryForBeanArray(sql, MeteTestPO.class, userId);
	}

	public Map<String, Object>[] queryForArray(int loginId) {
		String sql = "SELECT A.USER_ID,LOG_ID,CHANGE_TIME,CHANGE_TYPE,EDITOR_TYPE,EDITOR_ID FROM META_MAG_TEST A WHERE A.LOG_ID = ? ";
		return getDataAccess().queryForArrayMap(sql, loginId);
	}

	public List<Map<String, Object>> queryForList1() {
		String sql = "SELECT A.USER_ID,LOG_ID,CHANGE_TIME,CHANGE_TYPE,EDITOR_TYPE,EDITOR_ID FROM META_MAG_TEST A ";
		return getDataAccess().queryForList(sql);
	}

	public List<String> queryForPrimitiveList() {
		String sql = "SELECT A.USER_ID,LOG_ID,CHANGE_TIME,CHANGE_TYPE,EDITOR_TYPE,EDITOR_ID FROM META_MAG_TEST A ";
		return getDataAccess().queryForPrimitiveList(sql, String.class);
	}

	public List<MeteTestPO> queryForBeanList() {
		String sql = "SELECT A.USER_ID,LOG_ID,CHANGE_TIME,CHANGE_TYPE,EDITOR_TYPE,EDITOR_ID FROM META_MAG_TEST A ";
		return getDataAccess().queryForBeanList(sql, MeteTestPO.class);
	}

	public long queryForLong(int userId) {
		String sql = "SELECT LOG_ID FROM META_MAG_TEST A WHERE A.USER_ID = ? ";
		return getDataAccess().queryForLong(sql, userId);
	}

	public long queryForLongByNvl(long defaul, int userId) {
		String sql = "SELECT LOG_ID FROM META_MAG_TEST A WHERE A.USER_ID = ? ";
		return getDataAccess().queryForLongByNvl(sql, defaul, userId);
	}

	public int queryForInt(int userId) {
		String sql = "SELECT LOG_ID FROM META_MAG_TEST A WHERE A.USER_ID = ? ";
		return getDataAccess().queryForInt(sql, userId);
	}

	public int queryForIntByNvl(int defa, int userId) {
		String sql = "SELECT LOG_ID FROM META_MAG_TEST A WHERE A.USER_ID = ? ";
		return getDataAccess().queryForIntByNvl(sql, defa, userId);
	}

	public String queryForString(int userId) {
		String sql = "SELECT LOG_ID FROM META_MAG_TEST A WHERE A.USER_ID = ? ";
		return getDataAccess().queryForString(sql, userId);
	}

	public int queryForObject(int userId) {
		String sql = "SELECT A.USER_ID FROM META_MAG_TEST A WHERE A.USER_ID = ? ";
		return getDataAccess().queryForObject(sql, int.class, userId);
	}

	public int queryForObjectByNvl(int delfault, int userId) {
		String sql = "SELECT A.USER_ID FROM META_MAG_TEST A WHERE A.USER_ID = ? ";
		return getDataAccess().queryForObjectByNvl(sql, int.class, delfault, userId);
	}

	public Map<String, Object> queryForMap(int userId) {
		String sql = "SELECT A.USER_ID,LOG_ID,CHANGE_TIME,CHANGE_TYPE,EDITOR_TYPE,EDITOR_ID FROM META_MAG_TEST A WHERE A.USER_ID = ? ";
		return getDataAccess().queryForMap(sql, userId);
	}

	public MeteTestPO queryForBean(int userId) {
		String sql = "SELECT A.USER_ID,LOG_ID,CHANGE_TIME,CHANGE_TYPE,EDITOR_TYPE,EDITOR_ID FROM META_MAG_TEST A WHERE A.USER_ID = ? ";
		return getDataAccess().queryForBean(sql, MeteTestPO.class, userId);
	}

	public MeteTestPO queryByRowMapper(int userId) {
		String sql = "SELECT A.USER_ID,LOG_ID,CHANGE_TIME,CHANGE_TYPE,EDITOR_TYPE,EDITOR_ID FROM META_MAG_TEST A WHERE A.USER_ID = ? ";
		return getDataAccess().queryByRowMapper(sql, new BeanMapper<MeteTestPO>(MeteTestPO.class), new Object[] { userId });
	}

	public DataTable queryForDataTable(int userId) {
		String sql = "SELECT A.USER_ID,LOG_ID,CHANGE_TIME,CHANGE_TYPE,EDITOR_TYPE,EDITOR_ID FROM META_MAG_TEST A WHERE A.USER_ID = ? ";
		return getDataAccess().queryForDataTable(sql, userId);
	}
	/************************ 测试DAO方法 end *************************/
	// public
}
