package com.techelevator.tenmo.models;

public class Balance {

private double balance;
	

	@Override
public String toString() {
	return "Your current account balance is: $" + balance;
}


	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}
	
	

}
