package com.techelevator.tenmo;

import java.util.ArrayList;
import java.util.List;

import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Balance;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;
import com.techelevator.tenmo.models.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.tenmo.services.TransferService;
import com.techelevator.view.ConsoleService;

public class App {

private static final String API_BASE_URL = "http://localhost:8080/";
    
    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	
    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private TransferService transferService;

    public static void main(String[] args) {
    	App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL), new TransferService(API_BASE_URL));
    	app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService, TransferService transferService) {
		this.transferService = transferService;
    	this.console = console;
		this.authenticationService = authenticationService;
	}

	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");
		
		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() {
		while(true) {
			String choice = (String)console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if(MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if(MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if(MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if(MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if(MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if(MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	private void viewCurrentBalance() {
		
		transferService.AUTH_TOKEN = currentUser.getToken();
		Balance balance = transferService.returnBalance();
		
		System.out.println(balance.toString());
	}

	private void viewTransferHistory() {
		transferService.AUTH_TOKEN = currentUser.getToken();
		System.out.println("-------------------------------------------");
		System.out.println("Transfers");
		System.out.println("ID" + "\t" + "From/To" + "\t" + "\t" + "Amount");
		System.out.println("-------------------------------------------");
		Transfer[] transferList = transferService.getTransfersByUserId(currentUser.getUser().getId());
		for (Transfer transfer : transferList) {
			String name = "";
			String fromTo = "";
			if (transfer.getAccount_from() == currentUser.getUser().getId()) {
				name = transferService.returnUsername(transfer.getAccount_to());
				fromTo = "To";
			}if (transfer.getAccount_to() == currentUser.getUser().getId()) {
				name = transferService.returnUsername(transfer.getAccount_from());
				fromTo += "From";
			}
			System.out.println(transfer.getTransfer_id()+ "\t" + fromTo + ": " + name + "\t" + "$ " + transfer.getAmount());
		}
		System.out.println("---------");
		int transferId = console.getUserInputInteger("Please enter transfer ID to view details (0 to cancel)");
		if (transferId != 0) {
			System.out.println("--------------------------------------------"); 
			System.out.println("Transfer Details");
			System.out.println("--------------------------------------------");
			Transfer transferToView = transferService.getTransferById(transferId);
			String from = transferService.returnUsername(transferToView.getAccount_from());
			String to = transferService.returnUsername(transferToView.getAccount_to());
			String type = transferService.returnTypeById(transferToView.getTransfer_type_id());
			String status = transferService.returnStatusById(transferToView.getTransfer_status_id());
			System.out.println("Id: " + transferToView.getTransfer_id() + "\n" + "From: " + from +"\n" + "To: " + to + "\n" + "Type: " + type + "\n" + "Status: " + status + "\n" + "Amount: $" + transferToView.getAmount());
		}else {
		mainMenu();
		}
	}

	private void viewPendingRequests() {
		transferService.AUTH_TOKEN = currentUser.getToken();
		System.out.println("-------------------------------------------");
		System.out.println("Pending Transfers");
		System.out.println("ID"+"\t"+"To"+"\t"+"Amount");
		System.out.println("-------------------------------------------");
		Transfer[] transferList = transferService.getPendingTransfersByUserId(currentUser.getUser().getId());
		if (transferList.length != 0) {
			for (Transfer transfer : transferList) {
				String to = transferService.returnUsername(transfer.getAccount_to());
				System.out.println(transfer.getTransfer_id()+"\t"+to+"\t"+"$ "+transfer.getAmount());
			}
			System.out.println("---------");
			int transferId = console.getUserInputInteger("Please enter transfer ID to approve/reject (0 to cancel)");
			if (transferId != 0) {
				System.out.println("1: Approve");
				System.out.println("2: Reject");
				System.out.println("0: Don't approve or reject");
				System.out.println("---------");
				int option = console.getUserInputInteger("Please choose an option");
				Transfer transferToUpdate = transferService.getTransferById(transferId);
				if (option == 1) {
					Balance balance = transferService.returnBalance();
					if (balance.getBalance() >= transferToUpdate.getAmount()) {
						balance.setBalance(balance.getBalance()-transferToUpdate.getAmount());
						transferService.updateBalance(balance, currentUser.getUser().getId());
						Balance balanceReceiver = transferService.returnBalanceById(transferToUpdate.getAccount_to());
						balanceReceiver.setBalance(balanceReceiver.getBalance()+transferToUpdate.getAmount());
						transferService.updateBalance(balanceReceiver, transferToUpdate.getAccount_to());
				
						transferToUpdate.setTransfer_status_id(2);
						transferService.updateTransferStatus(transferToUpdate.getTransfer_id(), transferToUpdate);
					}
					else {
						System.out.println("Sorry, you do not have enough funds to complete the request.");
					}
				}
				if (option == 0) {
					mainMenu();
				}
				if (option == 2) {
					transferToUpdate.setTransfer_status_id(3);
					transferService.updateTransferStatus(transferToUpdate.getTransfer_id(), transferToUpdate);
				}
			}else {
				mainMenu();
			}
		}else {
			System.out.println("You have no pending transfers at this time.");
		}
	}

	private void sendBucks() {
		transferService.AUTH_TOKEN = currentUser.getToken();
		System.out.println("-------------------------------------------");
		System.out.println("Users");
		System.out.println("ID"+"\t"+"Name");
		System.out.println("-------------------------------------------");
		User[] userList = transferService.returnUsers();
		for (User user : userList) {
			if (user.getId() != currentUser.getUser().getId()) {
			System.out.println(user.getId() + "\t" + user.getUsername());
			}
		}
		System.out.println("---------");
		int userId = console.getUserInputInteger("Enter ID of user you are sending to (0 to cancel)");
		if (userId == 0) {
			mainMenu();
		}
		if (userId == currentUser.getUser().getId()) {
			System.out.println("Sorry, you cannot send money to yourself.");
			mainMenu();
		}
		double amount = Double.parseDouble(console.getUserInput("Enter amount"));
		Balance balance = transferService.returnBalance();
		if (balance.getBalance() >= amount) {
			balance.setBalance(balance.getBalance()-amount);
			transferService.updateBalance(balance, currentUser.getUser().getId());
			Balance balanceReceiver = transferService.returnBalanceById(userId);
			balanceReceiver.setBalance(balanceReceiver.getBalance()+amount);
			transferService.updateBalance(balanceReceiver, userId);
			
			Transfer transfer = new Transfer();
			transfer.setAccount_from(currentUser.getUser().getId());
			transfer.setAccount_to(userId);
			transfer.setAmount(amount);
			transfer.setTransfer_status_id(2);
			transfer.setTransfer_type_id(2);
			
			transferService.createTransfer(transfer);
			
		}
		else {
			System.out.println("Sorry, you only have $" + balance.getBalance() + " in your account.");
		}
	}

	private void requestBucks() {
		transferService.AUTH_TOKEN = currentUser.getToken();
		System.out.println("-------------------------------------------");
		System.out.println("Users");
		System.out.println("ID"+"\t"+"Name");
		System.out.println("-------------------------------------------");
		User[] userList = transferService.returnUsers();
		for (User user : userList) {
			if (user.getId() != currentUser.getUser().getId()) {
			System.out.println(user.getId() + "\t" + user.getUsername());
			}
		}
		System.out.println("---------");
		int userId = console.getUserInputInteger("Enter ID of user you are requesting from (0 to cancel)");
		if (userId == 0) {
			mainMenu();
		}
		if (userId == currentUser.getUser().getId()) {
			System.out.println("Sorry, you cannot request money from yourself.");
			mainMenu();
		}
		double amount = Double.parseDouble(console.getUserInput("Enter amount"));
		Transfer transfer = new Transfer();
		transfer.setAccount_to(currentUser.getUser().getId());
		transfer.setAccount_from(userId);
		transfer.setAmount(amount);
		transfer.setTransfer_status_id(1);
		transfer.setTransfer_type_id(1);
		
		transferService.createTransfer(transfer);		
	}
	
	private void exitProgram() {
		System.exit(0);
	}

	private void registerAndLogin() {
		while(!isAuthenticated()) {
			String choice = (String)console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}


	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
            	authenticationService.register(credentials);
            	isRegistered = true;
            	System.out.println("Registration successful. You can now login.");
            } catch(AuthenticationServiceException e) {
            	System.out.println("REGISTRATION ERROR: "+e.getMessage());
				System.out.println("Please attempt to register again.");
            }
        }
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) //will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
		    try {
				currentUser = authenticationService.login(credentials);
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: "+e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}
	
	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}
}
