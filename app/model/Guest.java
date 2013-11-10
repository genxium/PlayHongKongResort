package model;

import model.BasicUser;

public class Guest extends BasicUser {
	
	public Guest(int userId, String email, boolean emailIdentity,
			boolean photoIdentity, boolean isAdmin) {
		super(userId, email, emailIdentity, photoIdentity, isAdmin);
		// TODO Auto-generated constructor stub
	}
	
};