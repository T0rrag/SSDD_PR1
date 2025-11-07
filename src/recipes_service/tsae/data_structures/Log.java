/*
* Copyright (c) Joan-Manuel Marques 2013. All rights reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
* This file is part of the practical assignment of Distributed Systems course.
*
* This code is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This code is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this code.  If not, see <http://www.gnu.org/licenses/>.
*/

package recipes_service.tsae.data_structures;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import recipes_service.data.Operation;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;
import lsim.library.api.LSimLogger;

/**
 * @author Joan-Manuel Marques, Daniel Lázaro Iglesias
 * December 2012
 *
 */
public class Log implements Serializable {
	private static final long serialVersionUID = -4864990265268259700L;

	/**
	 * This class implements a log that stores the operations
	 * received by a client.
	 * They are stored in a ConcurrentHashMap (a hash table),
	 * that stores a list of operations for each member of the group.
	 */
	private ConcurrentHashMap<String, List<Operation>> log = new ConcurrentHashMap<String, List<Operation>>();

	//AUX
	private long extractSeqNumber(Timestamp t) {
		try {
			String[] parts = t.toString().split(":");
			return Long.parseLong(parts[1].trim());
		} catch (Exception e) {
			return Timestamp.NULL_TIMESTAMP_SEQ_NUMBER;
		}
	}
	//AUX

	public Log(List<String> participants) {
		//hacer el log vacío para participantes
		for (Iterator<String> it = participants.iterator(); it.hasNext();) {
			log.put(it.next(), new Vector<Operation>());
		}
	}

	/**
	 * Inserts an operation into the log. Operations are inserted in order.
	 * If the last operation for the user is not the previous operation than the one
	 * being inserted, the insertion will fail.
	 * 
	 * @param op
	 * @return true if op is inserted, false otherwise.
	 */
	public synchronized boolean add(Operation op) {
		//.. PR1
		String hostid = op.getTimestamp().getHostid();
		List<Operation> ops = log.get(hostid);

		if (ops.isEmpty()) {
			//operación obligatoria
			if (extractSeqNumber(op.getTimestamp()) == 0) {
				ops.add(op);
			}
		} else {
			//si se necesitase 2ª operación
			long lastSeq = extractSeqNumber(ops.get(ops.size() - 1).getTimestamp());
			if (extractSeqNumber(op.getTimestamp()) == lastSeq + 1) {
				ops.add(op);
			}
		}

		LSimLogger.log(Level.TRACE, "Log updated:\n" + this);
		return true;
	}

	/**
	 * Checks the received summary (sum) and determines the operations
	 * contained in the log that have not been seen by
	 * the proprietary of the summary.
	 * Returns them in an ordered list.
	 * 
	 * @param sum
	 * @return list of operations
	 */
	public synchronized List<Operation> listNewer(TimestampVector sum) {
		//.. PR1
		List<Operation> newerOps = new Vector<>();
		for (String host : log.keySet()) {
			List<Operation> hostOps = log.get(host);
			if (hostOps == null)
				continue;

			long knownSeq = extractSeqNumber(sum.getLast(host));

			for (Operation op : hostOps) {
				if (extractSeqNumber(op.getTimestamp()) > knownSeq)
					newerOps.add(op);
			}
		}
		return newerOps;
	}

	/**
	 * Removes from the log the operations that have
	 * been acknowledged by all the members
	 * of the group, according to the provided ackSummary.
	 * 
	 * @param ack: ackSummary.
	 */
	public void purgeLog(TimestampMatrix ack) {
		// intentionally left empty for PR1 phase 1
	}

	/**
	 * equals
	 */
	@Override
	public synchronized boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Log))
			return false;
		Log other = (Log) obj;
		if (!log.keySet().equals(other.log.keySet()))
			return false;
		for (String host : log.keySet()) {
			List<Operation> l1 = log.get(host);
			List<Operation> l2 = other.log.get(host);
			if (l1.size() != l2.size())
				return false;
			for (int i = 0; i < l1.size(); i++)
				if (!l1.get(i).equals(l2.get(i)))
					return false;
		}
		return true;
	}

	/**
	 * toString
	 */
	@Override
	public synchronized String toString() {
		String name = "";
		for (Enumeration<List<Operation>> en = log.elements(); en.hasMoreElements();) {
			List<Operation> sublog = en.nextElement();
			for (ListIterator<Operation> en2 = sublog.listIterator(); en2.hasNext();) {
				name += en2.next().toString() + "\n";
			}
		}
		return name;
	}
}
