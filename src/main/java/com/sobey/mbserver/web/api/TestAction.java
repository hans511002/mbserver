package com.sobey.mbserver.web.api;

import java.util.List;
import java.util.Map;

import com.sobey.jcg.support.jdbc.DataTable;
import com.sobey.mbserver.db.dao.MeteTestPO;
import com.sobey.mbserver.db.dao.TestDAO;
import com.sobey.mbserver.db.dao.TestPO;

public class TestAction {
	private TestDAO testDAO;

	public void test() {
		testDAO.test();
	}

	public void setTestDAO(TestDAO testDAO) {
		this.testDAO = testDAO;
	}

	public TestPO[] queryForArray() {
		return testDAO.queryForArray();
	}

	public DataTable queryForDataTable() {
		return testDAO.queryForDataTable();
	}

	public List<TestPO> queryForList() {
		return testDAO.queryForList();
	}

	public TestPO queryForBean() {
		return testDAO.queryForBean();
	}

	/**
	 * 测试方法，返回角色列表
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getRoleList(int roleId) {
		List<Map<String, Object>> l = testDAO.getRoleList(roleId);
		return l;
	}

	public int queryForInt() {
		return testDAO.queryForInt();
	}

	public Map<String, Object> queryForMap() {
		return testDAO.queryForMap();
	}

	public String queryForString() {
		return testDAO.queryForString();
	}

	public int[] insertBatch() {
		return testDAO.insertBatch();
	}

	public void exeUpdate() {
		testDAO.exeUpdate();
	}

	// public int execQueryCall() {
	// return testDAO.execQueryCall();
	// }

	/***************** 测试方法aciton start **********************/
	public Object[][] queryForArrary(boolean bool) {
		return testDAO.queryForArrary(bool);
	}

	public String[] queryForPrimitiveArray(int userId) {
		return testDAO.queryForPrimitiveArray(userId);
	}

	public MeteTestPO[] queryForBeanArray(int userId) {
		return testDAO.queryForBeanArray(userId);
	}

	public Map<String, Object>[] queryForArray(int loginId) {
		return testDAO.queryForArray(loginId);
	}

	public List<Map<String, Object>> queryForList1() {
		return testDAO.queryForList1();
	}

	public List<String> queryForPrimitiveList() {
		return testDAO.queryForPrimitiveList();
	}

	public List<MeteTestPO> queryForBeanList() {
		return testDAO.queryForBeanList();
	}

	public long queryForLong(int userId) {
		return testDAO.queryForLong(userId);
	}

	public long queryForLongByNvl(long defaul, int userId) {
		return testDAO.queryForLongByNvl(defaul, userId);
	}

	public int queryForInt(int userId) {
		return testDAO.queryForInt(userId);
	}

	public int queryForIntByNvl(int defa, int userId) {
		return testDAO.queryForIntByNvl(defa, userId);
	}

	public String queryForString(int userId) {
		return testDAO.queryForString(userId);
	}

	public int queryForObject(int userId) {
		return testDAO.queryForObject(userId);
	}

	public int queryForObjectByNvl(int defaultVal, int userId) {
		return testDAO.queryForObjectByNvl(defaultVal, userId);
	}

	public Map<String, Object> queryForMap(int userId) {
		return testDAO.queryForMap(userId);
	}

	public MeteTestPO queryForBean(int userId) {
		return testDAO.queryForBean(userId);
	}

	public MeteTestPO queryByRowMapper(int userId) {
		return testDAO.queryByRowMapper(userId);
	}

	public DataTable queryForDataTable(int userId) {
		return testDAO.queryForDataTable(userId);
	}

	public Object aaaaaaa() {
		System.out.println("测试");
		return "sssssssssss";
	}

	/***************** 测试方法aciton end **********************/
}
