package com.techelevator.tenmo.dao;

import java.util.List;

import com.techelevator.tenmo.model.Balance;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

public interface TransferDAO {
	
	public Balance getBalance(int userId);
	
	public Balance updateBalance (int userId, Balance balance);
	
	public Transfer createNewTransfer (Transfer newTransfer);
	
	public List<Transfer> getTransfersByUserId (int id);
	
	public Transfer getTransferById(int id);

	public String getTransferTypeById(int id);
	
	public String getTransferStatusById(int id);
	
	public List<Transfer> getPendingTransfersByUserId (int id);
	
	public Transfer updateTransferStatus (Transfer transferToUpdate, int id);
}
