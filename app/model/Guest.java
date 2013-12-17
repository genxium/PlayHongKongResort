package model;

import model.BasicUser;

public class Guest extends BasicUser {

	public Guest(int userId, String email, String password, String name,
			boolean emailIdentity, boolean photoIdentity, boolean isAdmin) {
		super(userId, email, password, name, emailIdentity, photoIdentity, isAdmin);
	}
	
	public static Guest create(String email, String password, String name) {
		Guest guest=new Guest(0, email, password, name, false, false, false);
		return guest;
	}
	
};