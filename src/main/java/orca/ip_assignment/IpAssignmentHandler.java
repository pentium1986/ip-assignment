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
	private int laneNo;
	private final int MAX_LANE_NUM = 4;
	boolean[] laneTags = {true, true, true, true};
//	private RequestLoader requestLoader;
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
	
	private static final Vector<Vector<Vector<IpRange>>> ipMatrix;
	static{
		ipMatrix = new Vector<Vector<Vector<IpRange>>>();
		int start = 167772161;
		int nm = 24;
		for (int i = 0; i < 8; i++) {
			Vector<Vector<IpRange>> row = new Vector<Vector<IpRange>>();
			for (int j = 0; j < 4; j++) {
				Vector<IpRange> column = new Vector<IpRange>();
				for (int k = 0; k < 20; k++) {
					IpRange cell = new IpRange(start,nm,10);
					start = start + 0x00000100;
					column.add(cell);
				}
				row.add(column);
			}
			ipMatrix.add(row);
		}
	}
	
	private static boolean[][][] ipMatrixTags;
	static{
		ipMatrixTags = new boolean[8][4][20];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
				for (int k = 0; k < 20; k++) {
					ipMatrixTags[i][j][k] = true;
				}
			}
		}
	}
	
	private IpAssignmentHandler() {
		
	}
	
	public static IpAssignmentHandler getInstance() {
		if (instance == null)
			instance = new IpAssignmentHandler();
		return instance;
	}
	
	public boolean processIpAssignment(File in, File out) {
    	RequestLoader RL = new RequestLoader();
    	inFile = in;
    	outFile = out;
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
		
		System.out.println("hey");
		
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
	}
	
	private IpRange getIpRange(String domain, int lane) {
		int index = domains.indexOf(domain);
		Vector<IpRange> pool = ipMatrix.get(index).get(lane);
		IpRange res = null;
		for (int i = 0; i < 20; i++) {
			if(ipMatrixTags[index][lane][i]) {
				ipMatrixTags[index][lane][i] = false;
				res = pool.get(i);
				break;
			}
		}
		return res;
	}
}
