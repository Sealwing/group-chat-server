package com.sealwing.server.user;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sealwing.server.data.ServerState;
import com.sealwing.server.protocol.InProtocol;
import com.sealwing.server.protocol.OutProtocol;

public class UserMessageReciever extends Thread implements InProtocol, OutProtocol {

	private Socket socket;
	private boolean running;

	private InputStream input;
	private Lock locker = new ReentrantLock();

	private String nickname = "Smsr";
	private Integer id = -1;

	public UserMessageReciever(Socket socket) {
		this.socket = socket;
		this.running = true;
		try {
			input = socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (running && ServerState.up()) {
			try {
				if (input.available() > 0) {
					executeCommand(input.read());
				}
			} catch (IOException e) {
				System.out.println("message error with " + id);
			}
		}
	}

	private void executeCommand(int command) throws IOException {
		if (command == CONNECT) {
			connect();
		} else {
			if (command == DISCONNECT) {
				disconnect();
			} else {
				if (command == JOIN_GROUP) {
					joinGroup();
				} else {
					if (command == LEAVE_GROUP) {
						leaveGroup();
					} else {
						if (command == MESSAGE) {
							messaging();
						} else {
							if (command == CREATE_GROUP) {
								createGroup();
							}
						}
					}
				}
			}
		}
	}

	// READ AND WRITE

	private void connect() throws IOException {
		String nick = read();
		Integer userId = ServerState.userNew(this.socket, nick);
		if (userId != -1) {
			this.id = userId;
			this.nickname = nick;
			writeTo(this.socket, CONNECTION_ACCEPTED, ServerState.allGroups().getBytes(SET));
		} else {
			writeTo(this.socket, CONNECTION_DENIED, "wrong".getBytes(SET));
		}
	}

	private void disconnect() throws IOException {
		read();
		for (Socket soc : ServerState.groupSockets(id)) {
			writeTo(soc, USER_MISSED_GROUP, (this.nickname).getBytes(SET));
		}
		if (ServerState.userOut(id)) {
			writeTo(this.socket, DISCONNECTION_ACCEPTED, "ok".getBytes(SET));
			this.running = false;
			socket.close();
		}
	}

	private void joinGroup() throws IOException {
		String[] groupStats = read().split(":");
		String groupName = groupStats[0];
		String groupPassword = "";
		if (groupStats.length > 1) {
			groupPassword = groupStats[1];
		}
		if (ServerState.inGroup(groupName, groupPassword, id)) {
			writeTo(this.socket, JOINING_ACCEPTED,
					(groupName + ":" + ServerState.groupUsers(groupName, this.id)).getBytes(SET));
			byte[] outMessage = (this.nickname).getBytes(SET);
			for (Socket soc : ServerState.groupSockets(id)) {
				writeTo(soc, USER_JOINED_GROUP, outMessage);
			}
		} else {
			writeTo(this.socket, JOINING_DENIED, "err".getBytes(SET));
		}
	}

	private void leaveGroup() throws IOException {
		String groupName = read();
		if (ServerState.outGroup(groupName, id)) {
			byte[] outMessage = (this.nickname).getBytes(SET);
			for (Socket soc : ServerState.groupSockets(id)) {
				writeTo(soc, USER_MISSED_GROUP, outMessage);
			}
		}
	}

	private void messaging() throws IOException {
		String income = read();
		byte[] message = (nickname + ":" + income).getBytes(SET);
		for (Socket soc : ServerState.group(id)) {
			writeTo(soc, MESSAGE_SENT, message);
		}
	}

	private void createGroup() throws IOException {
		String[] info = read().split(":");
		if (info.length > 1) {
			if (ServerState.newGroup(info[0], info[1])) {
				byte[] outMessage = info[0].getBytes(SET);
				for (Socket soc : ServerState.users()) {
					writeTo(soc, GROUP_CREATED, outMessage);
				}
			}
		} else {
			if (ServerState.newGroup(info[0], "")) {
				byte[] outMessage = info[0].getBytes(SET);
				for (Socket soc : ServerState.users()) {
					writeTo(soc, GROUP_CREATED, outMessage);
				}
			}
		}
	}

	private String read() throws IOException {
		int len = input.read();
		byte[] buffer = new byte[len];
		input.read(buffer);
		return new String(buffer, "UTF-8");
	}

	private void writeTo(Socket soc, int command, byte[] message) {
		locker.lock();
		try {
			OutputStream out = soc.getOutputStream();
			out.write(command);
			out.write(message.length);
			out.write(message);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			locker.unlock();
		}
	}
}