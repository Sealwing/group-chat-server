package com.sealwing.server.data;

import java.util.ArrayList;

public class Group {
	String password = "";
	ArrayList<Integer> userIdList = new ArrayList<>();

	Group(String password, Integer creatorsId) {
		this.password = password;
		userIdList.add(creatorsId);
	}

	Group(String password) {
		this.password = password;
	}
}
