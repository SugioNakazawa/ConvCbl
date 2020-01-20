package com.hoge;

import java.util.ArrayList;
import java.util.List;

abstract public class BaseDiv {
	List<String[]> recList;

	public BaseDiv() {
		recList = new ArrayList<String[]>();
	}

	public void addRec(String[] rec) {
		recList.add(rec);
	}

	public String[] getRec(int i) {
		return recList.get(i);
	}

	public List<String[]> getRecList() {
		return recList;
	}

	public void setRecList(List<String[]> recList) {
		this.recList = recList;
	}

	public String getStat(String title) {
		StringBuilder sb = new StringBuilder(title + "\n");
		sb.append("lines " + this.recList.size() + "\n");
		return sb.toString();
	}
}
