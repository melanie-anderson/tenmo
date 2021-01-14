package com.techelevator.tenmo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.tenmo.dao.TransferSqlDAO;
import com.techelevator.tenmo.model.Balance;
import com.techelevator.tenmo.model.Transfer;

class TheIntegrationTest {

	private static SingleConnectionDataSource dataSource;
	private TransferSqlDAO dao;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		
		dataSource = new SingleConnectionDataSource();
		dataSource.setUrl("jdbc:postgresql://localhost:5432/tenmo");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres1");
		dataSource.setAutoCommit(false);
		
		
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		
		dataSource.destroy();
	}

	@BeforeEach
	void setUp() throws Exception {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		dao = new TransferSqlDAO(dataSource);
	}

	@AfterEach
	void tearDown() throws Exception {
		
		dataSource.getConnection().rollback();
		
	}

	@Test
	public void update_balance_for_customer1() {
		
		double expectedResult = 300;
		Balance balanceToUpdate = new Balance();
		balanceToUpdate.setBalance(300);
		
		double actualResult = dao.updateBalance(1, balanceToUpdate).getBalance();
		
		assertEquals(expectedResult, actualResult, 0);
		
	}
	
	@Test
	public void get_balance_returns_correct_balance() {
		
		Balance expectedResult = new Balance();
		expectedResult.setBalance(1000);
		double expected = expectedResult.getBalance();
		
		Balance actualResult = dao.getBalance(2);
		double actual = actualResult.getBalance();
		
		
		assertEquals(expected, actual, 0);
		
	}
	
	@Test
	public void create_new_transfer_and_read_back() {
		
		Transfer test = new Transfer();
		test.setAccount_from(1);
		test.setAccount_to(3);
		test.setAmount(200);
		test.setTransfer_status_id(2);
		test.setTransfer_type_id(2);
		
		dao.createNewTransfer(test);
		
		Transfer actualTransfer = dao.getTransferById(test.getTransfer_id());
		
		double actualAmount = actualTransfer.getAmount();
		
		double expectedAmount = 200;
		
		assertEquals(expectedAmount, actualAmount, 0);
		
	}
	
	@Test
	public void can_get_transfer_type_by_id() {
		
		Transfer test = new Transfer();
		test.setAccount_from(1);
		test.setAccount_to(3);
		test.setAmount(200);
		test.setTransfer_status_id(2);
		test.setTransfer_type_id(2);
		
		dao.createNewTransfer(test);
		
		String type = dao.getTransferTypeById(test.getTransfer_type_id());
		
		String expected = "Send";
		
		assertEquals(expected, type);
		
	}
	
	@Test
	public void can_get_transfer_status_by_id() {
		
		Transfer test = new Transfer();
		test.setAccount_from(1);
		test.setAccount_to(3);
		test.setAmount(200);
		test.setTransfer_status_id(2);
		test.setTransfer_type_id(2);
		
		dao.createNewTransfer(test);
		
		String status = dao.getTransferStatusById(test.getTransfer_status_id());
		
		String expected = "Approved";
		
		assertEquals(expected, status);
		
	}


}
