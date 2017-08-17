package com.sealwing.server.data;

import java.util.Hashtable;

public class Data {

	private static final Object sync = new Object();
	private static volatile Data data = null;

	boolean isUp = true;

	Hashtable<Integer, User> users = new Hashtable<>();
	Hashtable<String, Group> groups = new Hashtable<>();

	private Data() {
	}

	public static Data state() {
		if (data == null) {
			synchronized (sync) {
				if (data == null) {
					data = new Data();
				}
			}
		}
		return data;
	}

}
