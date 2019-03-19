package com.sobey.base.socket;

import java.io.Serializable;

public enum Order implements Serializable {
	ping, connect, locate, message, login, close, sms, remoteAccess, verify, setLinkType;

	public byte toByte() {
		return OrderToInt(this);
	}

	static byte OrderToInt(Order o) {
		switch (o) {
		case ping:
			return 0;
		case login:
			return 1;
		case locate:
			return 2;
		case message:
			return 3;
		case sms:
			return 4;
		case verify:
			return 70;
		case remoteAccess:
			return 80;
		case connect:
			return 90;
		case setLinkType:
			return 91;
		case close:
			return 100;
		default:
			return 0;
		}
	}

	public static Order parse(byte o) {
		switch (o) {
		case 0:
			return Order.ping;
		case 1:
			return Order.login;
		case 2:
			return Order.locate;
		case 3:
			return Order.message;
		case 4:
			return Order.sms;
		case 70:
			return Order.verify;
		case 80:
			return Order.remoteAccess;
		case 90:
			return Order.connect;
		case 91:
			return Order.setLinkType;
		case 100:
			return Order.close;
		default:
			return null;
		}
	}

}
