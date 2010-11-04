package erco.activations.hedc;
/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * @version $Id: MetaSearchRequest.java 3342 2003-07-31 09:36:46Z praun $
 * @author Christoph von Praun
 */


import java.util.*;
import java.io.*;

import xsched.Activation;

public class MetaSearchRequest {
    
    private long size_ = -1;
    private Writer wrt_ = null;
    private Hashtable params_ = null;
    private MetaSearchImpl msi_ = null;
    public List results = null;
    private int counter_ = 0;
    private Thread thread_ = null;

    public MetaSearchRequest(Writer w, MetaSearchImpl msi, Hashtable params) {
	wrt_ = w;
	msi_ = msi;
	params_ = params;
    }

    public void registerInterrupt(Thread t, int ctr) {
	counter_ = ctr;
	thread_ = t;
    }
    
    public synchronized void countDownInterrupt() {
	if (thread_ != null && --counter_ == 0)
	    thread_.interrupt();
    }

    public void go() throws Exception {
	if (wrt_ != null) {
	    Activation<Long> result = msi_.search(params_, wrt_, this);
	    Activation<Void> writeBack = Activation.schedule(this, "go_2(Lxsched/Activation;)V;", result);
	    result.hb(writeBack);
	} else { 
	    Activation<List> result = msi_.search(params_, this);
	    Activation<Void> writeBack = Activation.schedule(this, "go_3(Lxsched/Activation;)V;", result);
	    result.hb(writeBack);
	}
    }
    
    public void go_2(Activation<Long> result) {
    	System.out.println("MetaSearchRequest: writing back size result");
    	size_ = result.result();
    }
    
    public void go_3(Activation<List> result) {
    	System.out.println("MetaSearchRequest: writing back lists result");
    	results = result.result();
    }
    
    public String printResults() {
	String ret;
	if (results != null) {
	    StringBuffer sb = new StringBuffer();
	    sb.append("[");
	    for (Iterator it = results.iterator(); it.hasNext(); ) {
		sb.append(it.next());
		if (it.hasNext())
		    sb.append(",");
	    }
	    sb.append("]");
	    ret = sb.toString();
	} else
	    ret = "none";
	return ret;
    }
}





