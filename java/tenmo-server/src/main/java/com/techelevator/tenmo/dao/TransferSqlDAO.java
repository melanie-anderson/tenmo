package com.techelevator.tenmo.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.techelevator.tenmo.model.Balance;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

@Component
public class TransferSqlDAO implements TransferDAO {

	private JdbcTemplate jdbcTemplate;
	
	public TransferSqlDAO(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public Balance getBalance(int userId) {
		
		String sql = "select balance from accounts where user_id = ?";
		
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
		
		Balance userBalance = new Balance();
		
		if (results.next()) {
			userBalance.setBalance(results.getDouble("balance"));
			
		}
		
		return userBalance;
	}

	@Override
	public Balance updateBalance(int userId, Balance balance) {
		
		String sql = "UPDATE accounts SET balance = ? WHERE user_id = ?";
		
		jdbcTemplate.update(sql, balance.getBalance(), userId);
		
		return balance;
	}

	@Override
	public Transfer createNewTransfer(Transfer newTransfer) {
		
		String sql = "INSERT into transfers (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (?,?,?,?,?,?)";
		newTransfer.setTransfer_id(getNextTransferId());
		
		jdbcTemplate.update(sql, newTransfer.getTransfer_id(), newTransfer.getTransfer_type_id(), newTransfer.getTransfer_status_id(), newTransfer.getAccount_from(), newTransfer.getAccount_to(), newTransfer.getAmount());
		
		
		return newTransfer;
	}
	

	public int getNextTransferId() {
		SqlRowSet nextIdResult = jdbcTemplate.queryForRowSet("SELECT nextval('seq_transfer_id')");
		if (nextIdResult.next()) {
			return nextIdResult.getInt(1);
		} else {
			throw new RuntimeException("Something went wrong while getting an id for the new transfer");
		}
	}
	@Override
	public List<Transfer> getTransfersByUserId (int id) {
		List<Transfer> transferList = new ArrayList<Transfer>();
	
		String sql = "SELECT * FROM transfers WHERE account_from = ? OR account_to = ?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id, id);
		while (results.next()) {
			Transfer newTransfer = null;
			newTransfer = mapRowToTransfer(results);
			transferList.add(newTransfer);
		}
		
		return transferList;
	}
	
	@Override
	public Transfer getTransferById (int id) {
		Transfer transfer = null;
		
		String sql = "SELECT * FROM transfers WHERE transfer_id = ?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
		
		if (results.next()) {
			transfer = mapRowToTransfer(results);
		}
		return transfer;
	}
	
	@Override
	public String getTransferTypeById(int id) {
		String type = "";
		String sql = "SELECT transfer_type_desc FROM transfer_types WHERE transfer_type_id = ?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
		
		if (results.next()) {
			type = results.getString("transfer_type_desc");
		}
		return type;
	}
	
	@Override
	public String getTransferStatusById(int id) {
		
		String status = "";
		String sql = "SELECT transfer_status_desc FROM transfer_statuses WHERE transfer_status_id = ?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
		
		if (results.next()) {
			status = results.getString("transfer_status_desc");
		}
		return status;
		
	}

	@Override
	public List<Transfer> getPendingTransfersByUserId(int id) {
		List<Transfer> transferList = new ArrayList<Transfer>();
		
		String sql = "SELECT * FROM transfers WHERE transfer_status_id = ? AND account_from = ?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, 1, id);
		while (results.next()) {
			Transfer newTransfer = null;
			newTransfer = mapRowToTransfer(results);
			transferList.add(newTransfer);
		}
		return transferList;
	}
	
	@Override
	public Transfer updateTransferStatus(Transfer transferToUpdate, int id) {
		
		String sql = "UPDATE transfers SET transfer_status_id = ? WHERE transfer_id = ?";
		jdbcTemplate.update(sql, transferToUpdate.getTransfer_status_id(), id);
		return transferToUpdate;
	}
	
	public Transfer mapRowToTransfer (SqlRowSet results) {
		
		Transfer newTransfer = new Transfer();
		newTransfer.setAccount_from(results.getInt("account_from"));
		newTransfer.setAccount_to(results.getInt("account_to"));
		newTransfer.setAmount(results.getDouble("amount"));
		newTransfer.setTransfer_type_id(results.getInt("transfer_type_id"));
		newTransfer.setTransfer_status_id(results.getInt("transfer_status_id"));
		newTransfer.setTransfer_id(results.getInt("transfer_id"));
		
		return newTransfer;
	}

}
