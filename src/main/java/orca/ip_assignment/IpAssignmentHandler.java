package orca.ip_assignment;

import java.io.File;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.hp.hpl.jena.ontology.Individual;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;

import orca.ip_assignment.ndl.RequestLoader;
import orca.ip_assignment.ndl.RequestSaver;


public class IpAssignmentHandler {
	private File inFile;
	private File outFile;
	private String currentId;
	private int laneNo;
	private static final int MAX_LANE_NUM = 4;
	private static final int MAX_DOMAIN_NUM = 8;
	private static final int MAX_IP_CHUNK_NUM = 20;
	private static final int MAX_CHUNK_LEN = 255;
	boolean[] laneTags = {true, true, true, true};
	private static IpAssignmentHandler instance = null;
	
	private static final ArrayList<String> domains;
	static{
		domains = new ArrayList<String>();
		domains.add("RENCI XO Rack");
		domains.add("BBN/GPO XO Rack");
		domains.add("RENCI ACIS");
		domains.add("Duke CS");
		domains.add("UNC BEN");
		domains.add("UH minirack");
		domains.add("RENCI BEN (not a GENI resource)");
		domains.add("NERSC (not a GENI resource)");
	}
	
	private static final IpRange[][][] ipMatrix;
	static{
		ipMatrix = new IpRange[MAX_DOMAIN_NUM][MAX_LANE_NUM][MAX_IP_CHUNK_NUM];
		int start = 167772161;
		int nm = 24;
		for (int i = 0; i < MAX_DOMAIN_NUM; i++) {
			for (int j = 0; j < MAX_LANE_NUM; j++) {
				for (int k = 0; k < MAX_IP_CHUNK_NUM; k++) {
//					System.out.println("site " + i + " lane " + j + " chunk " + k + ":" + 
//							IpRange.convertAddrToStr(start));
					IpRange chunk = new IpRange(start, nm, MAX_CHUNK_LEN);
					start = start + 0x00000100;
					ipMatrix[i][j][k] = chunk;
				}
			}
		}
	}
	
	private static boolean[][][] ipMatrixTags;
	static{
		ipMatrixTags = new boolean[MAX_DOMAIN_NUM][MAX_LANE_NUM][MAX_IP_CHUNK_NUM];
		for (int i = 0; i < MAX_DOMAIN_NUM; i++) {
			for (int j = 0; j < MAX_LANE_NUM; j++) {
				for (int k = 0; k < MAX_IP_CHUNK_NUM; k++) {
					ipMatrixTags[i][j][k] = true;
				}
			}
		}
	}
	
	private static Map<String, Vector<int[]>> assignedIpMap;
	static{
		assignedIpMap = new HashMap<String, Vector<int[]>>();
	}
	
	private IpAssignmentHandler() {
		
	}
	
	public static IpAssignmentHandler getInstance() {
		if (instance == null)
			instance = new IpAssignmentHandler();
		return instance;
	}
	
	public boolean processIpAssignment(File in, File out, String id) {
    	RequestLoader RL = new RequestLoader();
    	inFile = in;
    	outFile = out;
    	currentId = id;
    	RL.loadGraph(inFile);
    	traverseGraph(RequestState.getInstance().getGraph());
    	RequestSaver.getInstance().saveGraph(outFile, RequestState.getInstance().getGraph());
		return true;
	}
	
	private int getAvailableLane() {
		int i;
		for (i = 0; i < MAX_LANE_NUM; i++) {
			if (laneTags[i]) {
				laneTags[i] = false;
				break;
			}
		}
		return i;
		
		//TODO:
		//throw exception when no lanes available
	}
	
	private void traverseGraph(SparseMultigraph <OrcaNode, OrcaLink> g) {
		
		assert(g != null);
	
		laneNo = getAvailableLane();
		
		for (OrcaLink e : RequestState.getInstance().getGraph().getEdges()) {
			Pair<OrcaNode> pn = RequestState.getInstance().getGraph().getEndpoints(e);
			IpRange assignedRange = getIpRange(pn.getFirst().getDomain(), laneNo);
			pn.getFirst().setIp(e, 
								IpRange.convertAddrToStr(assignedRange.getStartAddress()), 
								Integer.toString(assignedRange.getNetmask()));
			
			String secondStartAddress = null;
			if (pn.getFirst() instanceof OrcaNodeGroup) {
				secondStartAddress =
					IpRange.convertAddrToStr(assignedRange.getStartAddress()
											+ ((OrcaNodeGroup) pn.getFirst()).getNodeCount());
			} 
			else {
				secondStartAddress =
					IpRange.convertAddrToStr(assignedRange.getStartAddress() + 1);
			}
			pn.getSecond().setIp(e, 
								secondStartAddress, 
								Integer.toString(assignedRange.getNetmask()));
		}
		
		//internal VLAN	
		for (OrcaNode n : RequestState.getInstance().getGraph().getVertices()) {
			if (n instanceof OrcaNodeGroup && ((OrcaNodeGroup) n).getInternalVlan()) {
				IpRange assignedRange = getIpRange(n.getDomain(), laneNo);
				((OrcaNodeGroup) n).setInternalIp(
						IpRange.convertAddrToStr(assignedRange.getStartAddress()), 
						Integer.toString(assignedRange.getNetmask()));
			}
		}
	}
	
	private IpRange getIpRange(String domain, int lane) {
		int domainIndex = domains.indexOf(domain);
		IpRange res = null;
		for (int i = 0; i < MAX_IP_CHUNK_NUM; i++) {
			if(ipMatrixTags[domainIndex][lane][i]) {
				ipMatrixTags[domainIndex][lane][i] = false;
				int[] indices = {domainIndex, lane, i};
				if (assignedIpMap.containsKey(currentId)) {
					Vector<int[]> assignedIpRanges = assignedIpMap.get(currentId);
					assignedIpRanges.add(indices);
					assignedIpMap.put(currentId, assignedIpRanges);
				}
				else {
					Vector<int[]> assignedIpRanges = new Vector<int[]>();
					assignedIpRanges.add(indices);
					assignedIpMap.put(currentId, assignedIpRanges);
				}
				res = ipMatrix[domainIndex][lane][i];
				break;
			}
		}
		return res;
	}
	
	public boolean freeIpAddresses (String id) {
		if (assignedIpMap.containsKey(id)) {
			Vector<int[]> assignedIpRanges = assignedIpMap.get(id);
			for (int[] indices : assignedIpRanges) {
				ipMatrixTags[indices[0]][indices[1]][indices[2]] = true;
				laneTags[indices[1]] = true;
			}
			assignedIpRanges.clear();
			assignedIpMap.remove(id);
			return true;
		}
		return false;
	}
}
