package orca.ip_assignment;

import java.io.File;

import orca.ip_assignment.ndl.*;
/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        //System.out.println( "Hello World!" );
//    	RequestLoader RL = new RequestLoader();
    	File infile = new File("/home/kexu/workspace/ip-assignment/testinput.ndl");
    	File outfile = new File("/home/kexu/workspace/ip-assignment/testoutput.ndl");
    	File infile2 = new File("/home/kexu/workspace/ip-assignment/testinput2.ndl");
    	File outfile2 = new File("/home/kexu/workspace/ip-assignment/testoutput2.ndl");
//    	RL.loadGraph(infile);
//    	RequestSaver.getInstance().saveGraph(outfile, RequestState.getInstance().g);
    	IpAssignmentHandler.getInstance().processIpAssignment(infile, outfile);
    	IpAssignmentHandler.getInstance().processIpAssignment(infile2, outfile2);
    }
}
