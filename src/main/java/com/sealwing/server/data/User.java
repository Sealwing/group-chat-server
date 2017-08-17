package com.sealwing.server.data;

import java.net.Socket;

public class User {
	Socket socket;
	String nickname = "SomeUser";
	String groupName = "none";

	User(Socket soc, String nickname) {
		this.socket = soc;
		this.nickname = nickname;
	}
}
