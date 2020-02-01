package com.hoge;

import java.util.ArrayList;
import java.util.List;

/**
 * 各DIVISIONのベースクラス。
 * 
 * @author nakazawasugio
 *
 */
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

	public String getStat(String title) {
		return title + " : " + recList.size() + " lines";
	}
}
