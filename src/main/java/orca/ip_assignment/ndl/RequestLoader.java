package orca.ip_assignment.ndl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import orca.ip_assignment.RequestState;
import orca.ip_assignment.OrcaImage;
import orca.ip_assignment.OrcaLink;
import orca.ip_assignment.OrcaNode;
import orca.ip_assignment.OrcaNodeGroup;
import orca.ip_assignment.OrcaReservationTerm;
import orca.ndl.INdlRequestModelListener;
import orca.ndl.NdlCommons;
import orca.ndl.NdlRequestParser;

import org.apache.commons.lang.StringUtils;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hyperrealm.kiwi.ui.dialog.ExceptionDialog;

import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class RequestLoader implements INdlRequestModelListener {

	private OrcaReservationTerm term = new OrcaReservationTerm();
	private String reservationDiskImage = null, reservationDomain = null;
	private Map<String, OrcaNode> nodes = new HashMap<String, OrcaNode>();
	private Map<String, OrcaLink> links = new HashMap<String, OrcaLink>();
	private Map<String, OrcaNode> interfaceToNode = new HashMap<String, OrcaNode>();

	public boolean loadGraph(File f) {
		BufferedReader bin = null; 
		try {
			FileInputStream is = new FileInputStream(f);
			bin = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			StringBuilder sb = new StringBuilder();
			String line = null;
			while((line = bin.readLine()) != null) {
				sb.append(line);
				// re-add line separator
				sb.append(System.getProperty("line.separator"));
			}
			
			bin.close();
			
			NdlRequestParser nrp = new NdlRequestParser(sb.toString(), this);
			nrp.processRequest();
			
		} catch (Exception e) {
			System.out.println("loadGraph:exception" + e);
//			ExceptionDialog ed = new ExceptionDialog(GUI.getInstance().getFrame(), "Exception");
//			ed.setLocationRelativeTo(GUI.getInstance().getFrame());
//			ed.setException("Exception encountered while loading file " + f.getName() + ":", e);
//			ed.setVisible(true);
			return false;
		} 
		
		return true;
	}
	
	// sometimes getLocalName is not good enough
	private String getTrueName(Resource r) {
		if (r == null)
			return null;
		
		return StringUtils.removeStart(r.getURI(), NdlCommons.ORCA_NS);
	}

	public void ndlReservation(Resource i, final OntModel m) {
		System.out.println("ndlReservation");

		if (i != null) {
			reservationDomain = RequestSaver.reverseLookupDomain(NdlCommons.getDomain(i));
			RequestState.getInstance().setOFVersion(NdlCommons.getOpenFlowVersion(i));
			Resource di = NdlCommons.getDiskImage(i);
			if (di != null) {
				String imageURL = NdlCommons.getIndividualsImageURL(i);
				String imageHash = NdlCommons.getIndividualsImageHash(i);
				if ((imageURL != null) && (imageHash != null)) {
					try {
//						RequestState.getInstance().addImage(new OrcaImage(di.getLocalName(), new URL(imageURL), imageHash), null);
						// assign image to reservation
						reservationDiskImage = di.getLocalName();
					} catch (Exception e) {
						// FIXME: ?
						;
					}
				}
			}
			
		}
	}

	public void ndlReservationEnd(Literal e, OntModel m, Date end) {
		// Nothing to do
		System.out.println("ndlReservationEnd");
	}

	public void ndlReservationStart(Literal s, OntModel m, Date start) {
		System.out.println("ndlReservationStart");
		term.setStart(start);
	}

	public void ndlReservationTermDuration(Resource t, OntModel m, int years, int months, int days,
			int hours, int minutes, int seconds) {
		System.out.println("ndlReservationTermDuration");
		term.setDuration(days, hours, minutes);
	}

	public void ndlNode(Resource ce, OntModel om, Resource ceClass, List<Resource> interfaces) {
		System.out.println("ndlNode");

		if (ce == null)
			return;
		OrcaNode newNode;
		
		if (ceClass.equals(NdlCommons.computeElementClass))
			newNode = new OrcaNode(ce.getLocalName());
		else { 
			if (ceClass.equals(NdlCommons.serverCloudClass)) {
				OrcaNodeGroup newNodeGroup = new OrcaNodeGroup(ce.getLocalName());
				int ceCount = NdlCommons.getNumCE(ce);
				if (ceCount > 0)
					newNodeGroup.setNodeCount(ceCount);
				newNodeGroup.setSplittable(NdlCommons.isSplittable(ce));
				newNode = newNodeGroup;
			} else // default just a node
				newNode = new OrcaNode(ce.getLocalName());
		}

		Resource domain = NdlCommons.getDomain(ce);
		if (domain != null)
			newNode.setDomain(RequestSaver.reverseLookupDomain(domain));
		
		Resource ceType = NdlCommons.getSpecificCE(ce);
		if (ceType != null)
			newNode.setNodeType(RequestSaver.reverseNodeTypeLookup(ceType));

		// get proxied ports
		List<NdlCommons.ProxyFields> portList = NdlCommons.getNodeProxiedPorts(ce);
		String portListString = "";
		for (NdlCommons.ProxyFields pf: portList) {
			portListString += pf.proxiedPort + ",";
		}
		if (portListString.length() > 0) {
			portListString = portListString.substring(0, portListString.length() - 1);
			newNode.setPortsList(portListString);
		}
		
		// process interfaces
		for (Iterator<Resource> it = interfaces.iterator(); it.hasNext();) {
			Resource intR = it.next();
			interfaceToNode.put(intR.getURI(), newNode);
		}

		// disk image
		Resource di = NdlCommons.getDiskImage(ce);
		if (di != null) {
			try {
				String imageURL = NdlCommons.getIndividualsImageURL(ce);
				String imageHash = NdlCommons.getIndividualsImageHash(ce);
				RequestState.getInstance().addImage(new OrcaImage(di.getLocalName(), 
						new URL(imageURL), imageHash), null);
				newNode.setImage(di.getLocalName());
			} catch (Exception e) {
				// FIXME: ?
				;
			}
		}
		
		// post boot script
		String script = NdlCommons.getPostBootScript(ce);
		if ((script != null) && (script.length() > 0)) {
			newNode.setPostBootScript(script);
		}
		
		nodes.put(ce.getURI(), newNode);
		
		// add nodes to the graph
		RequestState.getInstance().getGraph().addVertex(newNode);
	}

	/**
	 * For now deals only with p-to-p connections
	 */
	public void ndlNetworkConnection(Resource l, OntModel om, 
			long bandwidth, long latency, List<Resource> interfaces) {
		System.out.println("ndlNetworkConnection");

		if (l == null)
			return;
		
		// find what nodes it connects (should be two)
		Iterator<Resource> it = interfaces.iterator(); 
		
		if (interfaces.size() == 2) {
			OrcaLink ol = new OrcaLink(l.getLocalName());
			ol.setBandwidth(bandwidth);
			ol.setLatency(latency);
			
			// point-to-point link
			// the ends
			Resource if1 = it.next(), if2 = it.next();
			
			if ((if1 != null) && (if2 != null)) {
				OrcaNode if1Node = interfaceToNode.get(if1.getURI());
				OrcaNode if2Node = interfaceToNode.get(if2.getURI());
				
				// have to be there
				if ((if1Node != null) && (if2Node != null)) {
					RequestState.getInstance().getGraph().addEdge(ol, new Pair<OrcaNode>(if1Node, if2Node), EdgeType.UNDIRECTED);
				}
			}
			// for now save only p-to-p links
			links.put(l.getURI(), ol);
		} else {
			// multi-point link or internal vlan of a node group
			if (interfaces.size() == 1) {
				// node group w/ internal vlan
				OrcaNode ifNode = interfaceToNode.get(it.next().getURI());
				if (ifNode instanceof OrcaNodeGroup) {
					OrcaNodeGroup ong = (OrcaNodeGroup)ifNode;
					ong.setInternalVlan(true);
					ong.setInternalVlanBw(bandwidth);
				}
			}
		}

	}

	public void ndlInterface(Resource intf, OntModel om, Resource conn, Resource node, String ip, String mask) {
		System.out.println("ndlInterface");

		if (intf == null)
			return;
		OrcaNode on = null;
		OrcaLink ol = null;
		if (node != null)
			on = nodes.get(node.getURI());
		if (conn != null)
			ol = links.get(conn.getURI());
		
		if (on != null) {
			if (ol != null) {
				on.setIp(ol, ip, "" + RequestSaver.netmaskStringToInt(mask));
				on.setInterfaceName(ol, getTrueName(intf));
			}
			else {
				// this could be a disconnected node group
				if (on instanceof OrcaNodeGroup) {
					OrcaNodeGroup ong = (OrcaNodeGroup)on;
					if (ong.getInternalVlan())
						ong.setInternalIp(ip, "" + RequestSaver.netmaskStringToInt(mask));
				}
			}
				
		}
	}
	
	public void ndlSlice(Resource sl, OntModel m) {
		System.out.println("ndlSlice");

		// check that this is an OpenFlow slice and get its details
		if (sl.hasProperty(NdlCommons.RDF_TYPE, NdlCommons.ofSliceClass)) {
			Resource ofCtrl = NdlCommons.getOfCtrl(sl);
			if (ofCtrl == null)
				return;
			RequestState.getInstance().setOfCtrlUrl(NdlCommons.getURL(ofCtrl));
			RequestState.getInstance().setOfUserEmail(NdlCommons.getEmail(sl));
			RequestState.getInstance().setOfSlicePass(NdlCommons.getSlicePassword(sl));
			if ((RequestState.getInstance().getOfUserEmail() == null) ||
					(RequestState.getInstance().getOfSlicePass() == null) ||
					(RequestState.getInstance().getOfCtrlUrl() == null)) {
					// disable OF if invalid parameters
					RequestState.getInstance().setNoOF();
					RequestState.getInstance().setOfCtrlUrl(null);
					RequestState.getInstance().setOfSlicePass(null);
					RequestState.getInstance().setOfUserEmail(null);
			}
		}	
	}

	public void ndlReservationResources(List<Resource> res, OntModel m) {
		// nothing to do here in this case
		System.out.println("ndlReservationResources");
	}
	
	public void ndlParseComplete() {
		System.out.println("ndlParseComplete");

		// set term etc
		RequestState.getInstance().setTerm(term);
		RequestState.getInstance().setDomainInReservation(reservationDomain);
		RequestState.getInstance().setVMImageInReservation(reservationDiskImage);
	}

	public void ndlNodeDependencies(Resource ni, OntModel m, Set<Resource> dependencies) {
		System.out.println("ndlNodeDependencies");
		OrcaNode mainNode = nodes.get(ni.getURI());
		if ((mainNode == null) || (dependencies == null))
			return;
		for(Resource r: dependencies) {
			OrcaNode depNode = nodes.get(r.getURI());
			if (depNode != null)
				mainNode.addDependency(depNode);
		}
	}
}
