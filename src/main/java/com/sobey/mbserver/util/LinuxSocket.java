package com.sobey.mbserver.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class LinuxSocket {

	int type = 0;
	/** 1 = TCP, 2 = UDP, 3 = RAW, 4 = UNIX(stream) **/

	/**
	 * TCP_ESTABLISHED = 1, TCP_SYN_SENT = 2, TCP_SYN_RECV = 3, TCP_FIN_WAIT1 = 4, TCP_FIN_WAIT2 = 5, TCP_TIME_WAIT = 6,
	 * TCP_CLOSE = 7, TCP_CLOSE_WAIT = 8, TCP_LAST_ACK = 9, TCP_LISTEN = 10, TCP_CLOSING = 11,
	 * 
	 * unix_LISTENING = 01, unix_CONNECTED = 03,
	 **/
	int st;
	String state = null;
	int local_address0, local_address1, local_address2, local_address3;
	String local_address = "0.0.0.0";
	int local_port;
	int rem_address0, rem_address1, rem_address2, rem_address3;
	String rem_address = "0.0.0.0";
	int rem_port;
	int tx_queue;
	int rx_queue;
	int tr;
	int tm_when;
	int retrnsmt;
	int uid;
	int timeout;
	int inode;

	int Num;
	int RefCount;
	int Flags;
	String Path;

	public LinuxSocket() { // create a blank socket..just for the hell of it ie debugging

	}

	public LinuxSocket(int inode) { // create a socket when we only know the inode
		this.inode = inode;
		update(); // find itself in the networking tables and update it's info
	}

	public LinuxSocket(int inode, int type) { // create a socket when we know both the inode and the type
		this.inode = inode;
		this.type = type;
		update(); // same as above
	}

	private void parseStream(String line) { // this parses the unix stream file
		if (line.length() > 50) // make sure that a stream is connect to something otherwise don't look to see what it's
								// connected to
			Path = line.substring(51).trim(); // get the path and trim off all the trailing spaces
		st = Integer.valueOf(line.substring(43, 45).trim()).intValue();
		// get the state number, trim it, and convert it to an int
		switch (st) { // set the state string to the correct text, can't find any docs as to what state is which, but
						// this looks right
		case 1:
			state = "LISTENING";
			break;
		case 3:
			state = "CONNECTED";
			break;
		}
	}

	private void parseSocket(String line) { // parse socket entries

		local_address0 = Integer.valueOf(line.substring(6, 8).trim(), 16).intValue(); // get the local address in two
																						// charecter chunks
		local_address1 = Integer.valueOf(line.substring(8, 10).trim(), 16).intValue(); // convert them from hex to an
																						// int
		local_address2 = Integer.valueOf(line.substring(10, 12).trim(), 16).intValue();
		local_address3 = Integer.valueOf(line.substring(12, 14).trim(), 16).intValue();
		local_address = local_address3 + "." + local_address2 + "." + local_address1 + "." + local_address0; // then
																												// reverse
																												// them
																												// into
																												// a
																												// human
																												// readable
																												// string

		local_port = Integer.valueOf(line.substring(15, 19).trim(), 16).intValue(); // get the local port and convert
																					// from hex

		rem_address0 = Integer.valueOf(line.substring(20, 22).trim(), 16).intValue(); // same as above except for the
																						// remoe addresses
		rem_address1 = Integer.valueOf(line.substring(22, 24).trim(), 16).intValue();
		rem_address2 = Integer.valueOf(line.substring(24, 26).trim(), 16).intValue();
		rem_address3 = Integer.valueOf(line.substring(26, 28).trim(), 16).intValue();
		rem_address = rem_address3 + "." + rem_address2 + "." + rem_address1 + "." + rem_address0;

		rem_port = Integer.valueOf(line.substring(29, 33).trim(), 16).intValue(); // remote port

		st = Integer.valueOf(line.substring(34, 36).trim(), 16).intValue(); // get the state number and convert to int
																			// from hex

		switch (st) { // based on the state number set the state string //can't find string for UDP or RAW

		case 0:
			state = null;
			break;
		case 1:
			state = "ESTABLISHED";
			break;
		case 2:
			state = "SYN_SENT";
			break;
		case 3:
			state = "SYN_RECV";
			break;
		case 4:
			state = "FIN_WAIT1";
			break;
		case 5:
			state = "FIN_WAIT2";
			break;
		case 6:
			state = "TIME_WAIT";
			break;
		case 7:
			state = "CLOSE";
			break;
		case 8:
			state = "CLOSE_WAIT";
			break;
		case 9:
			state = "LAST_ACK";
			break;
		case 10:
			state = "LISTEN";
			break;
		case 11:
			state = "CLOSING";
			break;

		}
		uid = Integer.valueOf(line.substring(75, 81).trim()).intValue(); // get the user id number
		timeout = Integer.valueOf(line.substring(82, 90).trim()).intValue(); // get the timout value
	}

	// this searches through the networking tables and determines the type by which table it's in
	// it also fills up the information on a socket by passing it's table entry to the proper parser
	// this searches by inode number
	public void update() {

		FileReader tcp = null; // set up some file readers
		FileReader unix = null;
		FileReader udp = null;
		FileReader raw = null;
		LineNumberReader lnr = null; // make a line number reader
		String line = null; // make a couple of temp strings
		String temp = null;
		try {
			if (type == 0 || type == 1) { // if the type is tcp or hasn't been set run this
				tcp = new FileReader("/proc/net/tcp"); // open the tcp table
				lnr = new LineNumberReader(tcp); // wrap it in a line number reader
				try {
					while (true) { // keep reading until something causes us to jump out EOF or found inode
						try {
							line = lnr.readLine(); // read a line
							if (Integer.valueOf(line.substring(90, 100).trim()).intValue() == inode) {
								// check o see if its inode matches our own, trimming and converting
								type = 1; // if it does then set the type
								parseSocket(line); // send the line to be parsed
								System.out.println("\ttype: " + type + " \tinode: " + inode + " \tlocal address:"
										+ local_address + ":" + local_port + " \tremote address:" + rem_address + ":"
										+ rem_port + "\t" + state); // print a debug line
								break; // break out of the loop
							}
						} catch (NumberFormatException nfe) {
							// the inode we read in wasn't a number, must not be an inode
						}
					}
				} catch (IOException ioe) {
					// reached the end of the file
				} catch (NullPointerException npe) {
					// same here, so do nothing but break the loop
				}
			} // finished with TCP
			if (type == 0 || type == 2) {
				udp = new FileReader("/proc/net/udp"); // this is the same as above except of the UDP table
				lnr = new LineNumberReader(udp);
				try {
					while (true) {
						try {
							line = lnr.readLine();
							if (Integer.valueOf(line.substring(90, 100).trim()).intValue() == inode) {
								type = 2; // set type to UDP
								parseSocket(line);
								System.out.println("\ttype: " + type + " \tinode: " + inode + " \tlocal address:"
										+ local_address + ":" + local_port + " \tremote address:" + rem_address + ":"
										+ rem_port + "\t" + state);
								break;
							}
						} catch (NumberFormatException nfe) {
						}
					}
				} catch (IOException ioe) {
				} catch (NullPointerException npe) {
				}
			} // finished with UDP
			if (type == 0 || type == 3) {
				raw = new FileReader("/proc/net/raw"); // same as above except for the RAW table
				lnr = new LineNumberReader(raw);
				try {
					while (true) {
						try {
							line = lnr.readLine();
							if (Integer.valueOf(line.substring(90, 100).trim()).intValue() == inode) {
								type = 3; // set type to RAW
								parseSocket(line);
								System.out.println("\ttype: " + type + " \tinode: " + inode + " \tlocal address:"
										+ local_address + ":" + local_port + " \tremote address:" + rem_address + ":"
										+ rem_port + "\t" + state);
								break;
							}
						} catch (NumberFormatException nfe) {
						}
					}
				} catch (IOException ioe) {
				} catch (NullPointerException npe) {
				}
			} // finished with RAW table
			if (type == 0 || type == 4) {
				unix = new FileReader("/proc/net/unix"); // same as above except for stream/unix table
				lnr = new LineNumberReader(unix);
				try {
					while (true) {
						try {
							line = lnr.readLine();
							if (Integer.valueOf(line.substring(45, 50).trim()).intValue() == inode) {
								type = 4;
								parseStream(line); // we send it to the stream parser instaed of the socket parser
								System.out.println("\ttype: " + type + " \tinode: " + inode + " \tstate:" + state
										+ " \tpath:" + Path);
								break;
							}
						} catch (NumberFormatException nfe) {
						}
					}
				} catch (IOException ioe) {
				} catch (NullPointerException npe) {
				}
			}
		} catch (FileNotFoundException fnfe) { // catch any foulup's involving missing files
		}
	}
}
