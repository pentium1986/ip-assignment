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

import java.net.URL;

/** 
 * Defines an ORCA VM Image
 * @author ibaldin
 *
 */
public class OrcaImage {
	private String shortName, hash;
	private URL url;
	
	public OrcaImage(String shortName, URL url, String hash) {
		this.shortName = shortName;
		this.url = url;
		this.hash = hash;
	}

	public URL getUrl() {
		return url;
	}
	
	public String getHash() {
		return hash;
	}
	
	public String getShortName() {
		return shortName;
	}
}
