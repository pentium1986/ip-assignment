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

import org.apache.commons.collections15.Factory;

public class OrcaLink {
    protected long bandwidth;
    protected long latency;
    protected String name;
	// reservation state
	protected String state = null;
	// reservation notice
	protected String resNotice = null;
    
    public OrcaLink(String name) {
        this.name = name;
    }

    interface ILinkCreator {
    	public OrcaLink create();
    	public void reset();
    }
    
    public void setBandwidth(long bw) {
    	bandwidth = bw;
    }

    public void setLatency(long l) {
    	latency = l;
    }

    public long getBandwidth() {
    	return bandwidth;
    }
    
    public long getLatency() {
    	return latency;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }       
    
	public void setState(String s) {
		state = s;
	}
	
	public void setReservationNotice(String n) {
		resNotice = n;
	}
    
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Get text for GUI viewer
     * @return
     */
    public String getViewerText() {
    	String viewText = "Link name: " + name;
    	if (bandwidth == 0)
    		viewText += "\nBandwidth: unspecified";
    	else 
    		viewText += "\nBandwidth: " + bandwidth;
    	
    	if (latency == 0) 
    		viewText += "\nLatency: unspecified";
    	else
    		viewText += "\nLatency: " + latency;
 
    	if (state == null)
    		viewText += "\nLink reservation state: unspecified";
    	else
    		viewText += "\nLink reservation state: " + state;
    		
    	if (resNotice == null)
    		viewText += "\nReservation notice: unspecified";
    	else
    		viewText += "\nReservation notice: " + resNotice;
    	
    	return viewText;
    }
    
    public static class OrcaLinkFactory implements Factory<OrcaLink> {
       private ILinkCreator inc = null;
        
        public OrcaLinkFactory(ILinkCreator i) {
        	inc = i;
        }
        
        public OrcaLink create() {
        	if (inc == null)
        		return null;
        	synchronized(inc) {
        		return inc.create();
        	}
        }    
    }
}
