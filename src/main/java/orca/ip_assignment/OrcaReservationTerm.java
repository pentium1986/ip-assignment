/*
* Copyright (c) 2011 RENCI/UNC Chapel Hill 
*
* @author Ilia Baldine
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
* and/or hardware specification (the "Work") to deal in the Work without restriction, including 
* without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
* sell copies of the Work, and to permit persons to whom the Work is furnished to do so, subject to 
* the following conditions:  
* The above copyright notice and this permission notice shall be included in all copies or 
* substantial portions of the Work.  
*
* THE WORK IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
* OUT OF OR IN CONNECTION WITH THE WORK OR THE USE OR OTHER DEALINGS 
* IN THE WORK.
*/
package orca.ip_assignment;

import java.util.Calendar;
import java.util.Date;

/**
 * ORCA reservation term is described by a start time/date and duration (d,hr,min)
 * @author ibaldin
 *
 */
public class OrcaReservationTerm {
	// null start date means immediate reservation
	protected Date start = null;
	protected int dDays, dHours, dMins;
	
	/**
	 * Default is starting now for 24 hours
	 */
	public OrcaReservationTerm() {
		dDays = 0;
		dHours = 24;
		dMins = 0;
	}
	
	public OrcaReservationTerm(int d, int h, int m) {
		dDays = d;
		dHours = h;
		dMins = m;
		if (durationInMinutes(d, h, m) == 0)
			dHours = 24;
	}
	
	public OrcaReservationTerm(Date s, int d, int h, int m) {
		start = s;
		dDays = d;
		dHours = h;
		dMins = m;
		if (durationInMinutes(d, h, m) == 0)
			dHours = 24;
	}
	
	private int durationInMinutes(int d, int h, int m) {
		return d*24*60 + h *60 + m;
	}
	
	public void setStart(Date s) {
		start = s;
	}
	
	public Date getStart() {
		return start;
	}
	
	public boolean isImmediate() {
		if (start == null)
			return true;
		return false;
	}
	
	/**
	 * Set the duration. normalization must be performed explicitly (if desired)
	 * @param d
	 * @param h
	 * @param m
	 */
	public void setDuration(int d, int h, int m) {
		if ((d < 0) || (h < 0) || (m < 0))
			return;
		dDays = d;
		dHours = h;
		dMins = m;
	}
	
	/**
	 * Normalizes the duration values (hours < 24, minutes < 60)
	 */
	public void normalizeDuration() {
		if (durationInMinutes(dDays, dHours, dMins) == 0)
			dHours = 24;
		
		int tmpMins = dMins;
		dMins = tmpMins % 60;
		int tmpHours = (int)Math.floor((double)tmpMins / 60.0) + dHours;
		dHours = tmpHours % 24;
		dDays += (int)Math.floor((double)tmpHours / 24.0);
	}
	
	public int getDurationDays() {
		return dDays;
	}
	
	public int getDurationHours() {
		return dHours;
	}
	
	public int getDurationMins() {
		return dMins;
	}
	
	@Override
	public String toString() {
		return "start: " + start + " duration: " + dDays + " days " + dHours + " hours " + dMins + "minutes";
	}
}
