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

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.LayeredIcon;
import edu.uci.ics.jung.visualization.renderers.Checkmark;

public class OrcaNode {

	private static final String NOT_SPECIFIED = "Not specified";
	public static final String NODE_NETMASK="32";
	protected String name;
	protected String image = null;
	protected String domain = null;
	// Pair<String> first is IP, second is Netmask
	protected HashMap<OrcaLink, Pair<String>> addresses;
	
	protected List<String> managementAccess = null;
	
//	protected final LayeredIcon icon;

	// specific node type 
	protected String nodeType = null;
	// post-boot script
	protected String postBootScript = null;
	// reservation state
	protected String state = null;
	// reservation notice
	protected String resNotice = null;
	// list of open ports
	protected String openPorts = null;
	
	protected Set<OrcaNode> dependencies = new HashSet<OrcaNode>();
	
	// mapping from links to interfaces on those links (used for manifests)
	protected Map<OrcaLink, String> interfaces = new HashMap<OrcaLink, String>();
	
	interface INodeCreator {
		public OrcaNode create();
		public void reset();
	}

	public String toStringLong() {
		String ret =  name;
		if (domain != null) 
			ret += " in domain " + domain;
		if (image != null)
			ret += " with image " + image;
		return ret;
	}
	
	public String toString() {
		return name;
	}
	
	public OrcaNode(String name) {
		this.name = name;
		this.addresses = new HashMap<OrcaLink, Pair<String>>();
	}

	// inherit some properties from parent
	public OrcaNode(String name, OrcaNode parent) {
		this.name = name;
		this.addresses = new HashMap<OrcaLink, Pair<String>>();
		this.domain = parent.getDomain();
		this.image = parent.getImage();
		this.nodeType = parent.getNodeType();
		this.dependencies = parent.getDependencies();
		this.state = parent.state;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public void setDomain(String d) {
		domain = d;
	}
	
	public void setNodeType(String t) {
		nodeType = t;
	}
	
	public String getNodeType() {
		return nodeType;
	}
	
	public void setIp(OrcaLink e, String addr, String nm) {
		if (e == null)
			return;
		if (addr == null)
			return;
		if (nm == null)
			nm = NODE_NETMASK;
		addresses.put(e, new Pair<String>(addr, nm));
	}
	
	public String getIp(OrcaLink e) {
		if ((e == null) || (addresses.get(e) == null))
			return null;
		return addresses.get(e).getFirst();
	}
	
	public String getNm(OrcaLink e) {
		if ((e == null) || (addresses.get(e) == null))
			return null;
		return addresses.get(e).getSecond();
	}
	
	public void removeIp(OrcaLink e) {
		if (e == null)
			return;
		addresses.remove(e);
	}
	
	public void addDependency(OrcaNode n) {
		if (n != null) 
			dependencies.add(n);
	}
	
	public void removeDependency(OrcaNode n) {
		if (n != null)
			dependencies.remove(n);
	}
	
	public void clearDependencies() {
		dependencies = new HashSet<OrcaNode>();
	}
	
	public boolean isDependency(OrcaNode n) {
		if (n == null)
			return false;
		return dependencies.contains(n);
	}
	
	/**
	 * returns empty set if no dependencies
	 * @return
	 */
	public Set<String> getDependencyNames() { 
		Set<String> ret = new HashSet<String>();
		for(OrcaNode n: dependencies) 
			ret.add(n.getName());
		return ret;
	}
	
	public Set<OrcaNode> getDependencies() {
		return dependencies;
	}
	
	public void setPostBootScript(String s) {
		postBootScript = s;
	}
	
	public String getPostBootScript() {
		return postBootScript;
	}
	
	public String getInterfaceName(OrcaLink l) {
		if (l != null)
			return interfaces.get(l);
		return null;
	}
	
	public void setInterfaceName(OrcaLink l, String ifName) {
		if ((l == null) || (ifName == null))
			return;
		
		interfaces.put(l, ifName);
	}
	
	public void setManagementAccess(List<String> s) {
		managementAccess = s;
	}
	
	// all available access options
	public List<String> getManagementAccess() {
		return managementAccess;
	}
	
	// if ssh is available
	public String getSSHManagementAccess() {
		for (String service: managementAccess) {
			if (service.startsWith("ssh://")) {
				return service;
			}
		}
		return null;
	}
	
	public void setState(String s) {
		state = s;
	}
	
	public void setReservationNotice(String n) {
		resNotice = n;
	}
	
	public String getPortsList() {
		return openPorts;
	}
	
	public boolean setPortsList(String list) {
		
		if ((list == null) || (list.trim().length() == 0))
			return true;
		
		String chkRegex = "(\\s*\\d+\\s*)(,(\\s*\\d+\\s*))*";
		
		if (list.matches(chkRegex)) { 
			for(String port: list.split(",")) {
				int portI = Integer.decode(port.trim());
				if (portI > 65535)
					return false;
			}
			openPorts = list;
			return true;
		}
		return false;
	}
	
	/** 
	 * Create a detailed printout of properties
	 * @return
	 */
	public String getViewerText() {
		String viewText = "";
		viewText += "Node name: " + name;
		viewText += "\nNode reservation state: " + state;
		viewText += "\nReservation notice: " + resNotice;
//		viewText += "\nNode Type: " + node.getNodeType();
//		viewText += "\nImage: " + node.getImage();
//		viewText += "\nDomain: " + domain;
		viewText += "\n\nPost Boot Script: \n" + (postBootScript == null ? NOT_SPECIFIED : postBootScript);
		viewText += "\n\nManagement access: \n";
		for (String service: getManagementAccess()) {
			viewText += service + "\n";
		}
		if (getManagementAccess().size() == 0) {
			viewText += NOT_SPECIFIED + "\n";
		}
		return viewText;
	}
	
	/**
	 * Node factory for requests
	 * @author ibaldin
	 *
	 */
 /*   public static class OrcaNodeFactory implements Factory<OrcaNode> {
        private INodeCreator inc = null;
        
        public OrcaNodeFactory(INodeCreator i) {
        	inc = i;
        }
        
        /**
         * Create a node or a cloud based on some setting
         */
  /*      public OrcaNode create() {
        	if (inc == null)
        		return null;
        	synchronized(inc) {
        		return inc.create();
        	}
        }       
    }*/

}