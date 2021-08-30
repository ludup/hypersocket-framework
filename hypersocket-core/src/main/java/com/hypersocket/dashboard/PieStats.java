package com.hypersocket.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PieStats implements Serializable {

	private static final long serialVersionUID = -5820654449052497973L;
	
	List<UsageStat> stats = new ArrayList<UsageStat>();
	
	public PieStats() {
	}

	public List<UsageStat> getStats() {
		return stats;
	}
}
