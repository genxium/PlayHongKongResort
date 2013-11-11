package model;

import model.BasicUser;

public class Guest extends BasicUser {

	public Guest(int userId, String email, String password, String name,
			boolean emailIdentity, boolean photoIdentity, boolean isAdmin) {
		super(userId, email, password, name, emailIdentity, photoIdentity, isAdmin);
		// TODO Auto-generated constructor stub
	}
	
};