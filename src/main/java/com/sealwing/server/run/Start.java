package com.sealwing.server.run;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.sealwing.server.data.ServerState;
import com.sealwing.server.user.UserMessageReciever;

public class Start {

	public static void main(String[] args) throws IOException {
		ServerSocket ss = new ServerSocket(6666);
		while (ServerState.up()) {
			Socket socket = ss.accept();
			UserMessageReciever userListener = new UserMessageReciever(socket);
			userListener.setDaemon(true);
			userListener.start();
		}
		ss.close();
	}

}
