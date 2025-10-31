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
import java.util.List;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Vector;

import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;

/**
 * @author Joan-Manuel Marques, Daniel Lázaro Iglesias
 * December 2012
 *
 */
public class TimestampMatrix implements Serializable{
	
	private static final long serialVersionUID = 3331148113387926667L;
        ConcurrentHashMap<String, TimestampVector> timestampMatrix = new ConcurrentHashMap<String, TimestampVector>();
        private final List<String> participants;

        public TimestampMatrix(List<String> participants){
                // create and empty TimestampMatrix
                this.participants = new Vector<String>();
                for (Iterator<String> it = participants.iterator(); it.hasNext(); ){
                        String id = it.next();
                        this.participants.add(id);
                        timestampMatrix.put(id, new TimestampVector(participants));
                }
        }
	
	/**
	 * @param node
	 * @return the timestamp vector of node in this timestamp matrix
	 */
        synchronized TimestampVector getTimestampVector(String node){
                if (node == null){
                        return null;
                }
                return timestampMatrix.get(node);
        }
	
	/**
	 * Merges two timestamp matrix taking the elementwise maximum
	 * @param tsMatrix
	 */
        public synchronized void updateMax(TimestampMatrix tsMatrix){
                if (tsMatrix == null){
                        return;
                }

                for (String node : tsMatrix.timestampMatrix.keySet()){
                        TimestampVector remoteVector = tsMatrix.timestampMatrix.get(node);
                        if (remoteVector == null){
                                continue;
                        }
                        TimestampVector localVector = timestampMatrix.get(node);
                        if (localVector == null){
                                if (!participants.contains(node)){
                                        participants.add(node);
                                }
                                timestampMatrix.put(node, remoteVector.clone());
                        } else {
                                localVector.updateMax(remoteVector);
                        }
                }
        }
	
	/**
	 * substitutes current timestamp vector of node for tsVector
	 * @param node
	 * @param tsVector
	 */
        public synchronized void update(String node, TimestampVector tsVector){
                if (node == null){
                        return;
                }

                if (tsVector == null){
                        timestampMatrix.remove(node);
                        participants.remove(node);
                } else {
                        if (!participants.contains(node)){
                                participants.add(node);
                        }
                        timestampMatrix.put(node, tsVector.clone());
                }
        }
	
	/**
	 * 
	 * @return a timestamp vector containing, for each node, 
	 * the timestamp known by all participants
	 */
        public synchronized TimestampVector minTimestampVector(){
                TimestampVector result = null;

                for (String participant : participants){
                        TimestampVector vector = timestampMatrix.get(participant);
                        if (vector == null){
                                continue;
                        }
                        if (result == null){
                                result = vector.clone();
                        } else {
                                result.mergeMin(vector);
                        }
                }

                return result;
        }
	
	/**
	 * clone
	 */
        public synchronized TimestampMatrix clone(){
                TimestampMatrix clone = new TimestampMatrix(new Vector<String>(participants));

                for (String node : participants){
                        TimestampVector vector = timestampMatrix.get(node);
                        if (vector != null){
                                clone.timestampMatrix.put(node, vector.clone());
                        }
                }

                return clone;
        }
	
	/**
	 * equals
	 */
	@Override
        public synchronized boolean equals(Object obj) {

                if (this == obj)
                        return true;
                if (obj == null)
                        return false;
                if (getClass() != obj.getClass())
                        return false;
                TimestampMatrix other = (TimestampMatrix) obj;
                return timestampMatrix.equals(other.timestampMatrix);
        }

        @Override
        public synchronized int hashCode() {
                return timestampMatrix.hashCode();
        }

	
	/**
	 * toString
	 */
	@Override
	public synchronized String toString() {
		String all="";
		if(timestampMatrix==null){
			return all;
		}
		for(Enumeration<String> en=timestampMatrix.keys(); en.hasMoreElements();){
			String name=en.nextElement();
			if(timestampMatrix.get(name)!=null)
				all+=name+":   "+timestampMatrix.get(name)+"\n";
		}
		return all;
	}
}
