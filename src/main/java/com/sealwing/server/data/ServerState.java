package com.sealwing.server.data;

import java.net.Socket;
import java.util.ArrayList;

public class ServerState {

	public static boolean up() {
		return Data.state().isUp;
	}

	public static Integer userNew(Socket socket, String nick) {
		int id = Data.state().users.size();
		Data.state().users.put(id, new User(socket, nick));
		return id;
	}

	public static Socket[] groupSockets(Integer userId) {
		try {
			if (Data.state().users.containsKey(userId)) {
				ArrayList<Integer> usersIn = Data.state().groups
						.get((Data.state().users.get(userId).groupName)).userIdList;
				Socket[] sockets = new Socket[usersIn.size()];
				int index = 0;
				for (Integer id : usersIn) {
					sockets[index++] = Data.state().users.get(id).socket;
				}
				return sockets;
			} else {
				return new Socket[0];
			}
		} catch (Exception e) {
			return new Socket[0];
		}
	}

	public static boolean userOut(Integer userId) {
		// CHECK IT !!!
		if (Data.state().users.containsKey(userId)) {
			Data.state().users.remove(userId);
			return true;
		} else {
			return false;
		}
	}

	public static boolean outGroup(String groupName, Integer userId) {
		if (Data.state().groups.containsKey(groupName)) {
			Data.state().groups.get(groupName).userIdList.remove(userId);
			return true;
		} else {
			return false;
		}
	}

	public static Socket[] group(Integer groupId) {
		String groupName = Data.state().users.get(groupId).groupName;
		if (Data.state().groups.containsKey(groupName)) {
			ArrayList<Integer> usersIn = Data.state().groups.get(groupName).userIdList;
			Socket[] sockets = new Socket[usersIn.size()];
			int index = 0;
			for (Integer id : usersIn) {
				sockets[index++] = Data.state().users.get(id).socket;
			}
			return sockets;
		}
		return new Socket[0];
	}

	public static boolean inGroup(String groupName, String groupPassword, Integer id) {
		if (Data.state().groups.containsKey(groupName)
				&& Data.state().groups.get(groupName).password.equals(groupPassword)) {
			Data.state().groups.get(groupName).userIdList.add(id);
			Data.state().users.get(id).groupName = groupName;
			return true;
		} else {
			return false;
		}
	}

	public static boolean newGroup(String groupName, String password) {
		if (!Data.state().groups.containsKey(groupName)) {
			Data.state().groups.put(groupName, new Group(password));
			return true;
		} else {
			return false;
		}
	}

	public static Socket[] users() {
		Socket[] sockets = new Socket[Data.state().users.size()];
		int index = 0;
		for (User user : Data.state().users.values()) {
			sockets[index++] = user.socket;
		}
		return sockets;
	}

	public static String allGroups() {
		if (Data.state().groups.size() > 0) {
			StringBuilder builder = new StringBuilder();
			for (String groupName : Data.state().groups.keySet()) {
				builder.append(groupName + ":");
			}
			return (builder.substring(0, builder.length() - 1)).toString();
		} else {
			return "none:none";
		}
	}

	public static String groupUsers(String groupName, Integer id) {
		if (Data.state().groups.containsKey(groupName) && Data.state().groups.get(groupName).userIdList.size() > 1) {
			StringBuilder builder = new StringBuilder();
			for (Integer userId : Data.state().groups.get(groupName).userIdList) {
				if (!userId.equals(id)) {
					builder.append(Data.state().users.get(userId).nickname + ":");
				}
			}
			return (builder.substring(0, builder.length() - 1)).toString();
		} else {
			return groupName + ":none";
		}
	}

	public static String[] groupUsers(Integer id) {
		String groupName = Data.state().users.get(id).groupName;
		if (Data.state().groups.contains(groupName)) {
			String[] usersNicknames = new String[Data.state().groups.get(groupName).userIdList.size()];
			int index = 0;
			for (Integer userId : Data.state().groups.get(Data.state().users.get(id).groupName).userIdList) {
				usersNicknames[index++] = Data.state().users.get(userId).nickname;
			}
			return usersNicknames;
		} else {
			return new String[0];
		}
	}

}
