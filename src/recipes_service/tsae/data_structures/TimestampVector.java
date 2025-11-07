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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;
import lsim.library.api.LSimLogger;

/**
 * @author Joan-Manuel Marques
 * December 2012
 *
 */
public class TimestampVector implements Serializable{
	// Only for the zip file with the correct solution of phase1.Needed for the logging system for the phase1. sgeag_2018p 
//	private transient LSimCoordinator lsim = LSimFactory.getCoordinatorInstance();
	// Needed for the logging system sgeag@2017
//	private transient LSimWorker lsim = LSimFactory.getWorkerInstance();
	
	private static final long serialVersionUID = -765026247959198886L;
	/**
	 * This class stores a summary of the timestamps seen by a node.
	 * For each node, stores the timestamp of the last received operation.
	 */
	
	private ConcurrentHashMap<String, Timestamp> timestampVector= new ConcurrentHashMap<String, Timestamp>();
	
	public TimestampVector (List<String> participants){
		// create and empty TimestampVector
		for (Iterator<String> it = participants.iterator(); it.hasNext(); ){
			String id = it.next();
			// when sequence number of timestamp < 0 it means that the timestamp is the null timestamp
			timestampVector.put(id, new Timestamp(id, Timestamp.NULL_TIMESTAMP_SEQ_NUMBER));
		}
	}
//AUX PARA UPDATE
	private long extractSeqNumber(Timestamp t) {
	    try {
	        String[] parts = t.toString().split(":");
	        return Long.parseLong(parts[1].trim());
	    } catch (Exception e) {
	        return Timestamp.NULL_TIMESTAMP_SEQ_NUMBER;
	    }
	}
	//AUX
	
	
	/**
	 * Updates the timestamp vector with a new timestamp. 
	 * @param timestamp
	 */
	public synchronized void updateTimestamp(Timestamp timestamp){
		LSimLogger.log(Level.TRACE, "Updating the TimestampVectorInserting with the timestamp: "+timestamp);

		// ... PR1
		//TODO problema al pasar el test,  user1-4 devuelven -1000, definido en null_timestamp
	    String hostId = timestamp.getHostid();
	    Timestamp current = timestampVector.get(hostId);
	    long newSeq = extractSeqNumber(timestamp);
	    long currentSeq = current == null ? Timestamp.NULL_TIMESTAMP_SEQ_NUMBER : extractSeqNumber(current);

	    if (current == null || currentSeq == Timestamp.NULL_TIMESTAMP_SEQ_NUMBER || newSeq > currentSeq) {
	        timestampVector.put(hostId, new Timestamp(hostId, newSeq));
	    }
	}
		
	
	/**
	 * merge in another vector, taking the elementwise maximum
	 * @param tsVector (a timestamp vector)
	 */
	public synchronized void updateMax(TimestampVector tsVector) {
		//.. PR1
		for (String host : tsVector.timestampVector.keySet()) {
			Timestamp other = tsVector.timestampVector.get(host);
			Timestamp current = timestampVector.get(host);
			if (current == null || other.compare(current) > 0) {
				timestampVector.put(host, new Timestamp(host, extractSeqNumber(other)));
			}
		}
	}

	
	/**
	 * 
	 * @param node
	 * @return the last timestamp issued by node that has been
	 * received.
	 */
	public synchronized Timestamp getLast(String node){
		//.. PR1
		// return generated automatically. Remove it when implementing your solution 
		return timestampVector.get(node);
	}
	
	/**
	 * merges local timestamp vector with tsVector timestamp vector taking
	 * the smallest timestamp for each node.
	 * After merging, local node will have the smallest timestamp for each node.
	 *  @param tsVector (timestamp vector)
	 */
	public synchronized void mergeMin(TimestampVector tsVector){
		//.. PR1
	    for (String host : tsVector.timestampVector.keySet()) {
	        Timestamp other = tsVector.timestampVector.get(host);
	        Timestamp current = timestampVector.get(host);
	        if (current == null || other.compare(current) < 0) {
	            timestampVector.put(host, new Timestamp(host, extractSeqNumber(other)));
	        }
	    }
	}
	
	/**
	 * clone
	 */
	public synchronized TimestampVector clone(){
		//.. PR1
	    TimestampVector copy = new TimestampVector(List.copyOf(timestampVector.keySet()));
	    for (String host : timestampVector.keySet()) {
	        Timestamp original = timestampVector.get(host);
	        copy.timestampVector.put(host, new Timestamp(host, extractSeqNumber(original)));
	    }
		// return generated automatically. Remove it when implementing your solution
	    return copy;
	}
	
	/**
	 * equals
	 */
	public synchronized boolean equals(Object obj){
		//.. PR1
	    if (this == obj) return true;
	    if (!(obj instanceof TimestampVector)) return false;
	    TimestampVector other = (TimestampVector) obj;
	    if (!timestampVector.keySet().equals(other.timestampVector.keySet())) return false;
	    for (String host : timestampVector.keySet()) {
	        if (!timestampVector.get(host).equals(other.timestampVector.get(host))) return false;
	    }
		// return generated automatically. Remove it when implementing your solution 
	    return true;
	}

	/**
	 * toString
	 */
	@Override
	public synchronized String toString() {
		String all = "";
		if (timestampVector == null) {
			return all;
		}
		for (Enumeration<String> en = timestampVector.keys(); en.hasMoreElements();) {
			String name = en.nextElement();
			if (timestampVector.get(name) != null)
				all += timestampVector.get(name) + "\n";
		}
		return all;
	}
}
