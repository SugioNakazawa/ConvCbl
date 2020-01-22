package com.hoge;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcedureDiv extends BaseDiv {
	static Logger logger = LoggerFactory.getLogger(CblProgram.class.getName());

	private static final String KEY_EXIT = "EXIT";
	private static final String KEY_PERFORM = "PERFORM";

	private static final String KEY_VARYONG = "VARYING";

	private static final String KEY_UNTIL = "UNTIL";

	List<ProcSec> secList;

	public ProcedureDiv() {
		super();
		secList = new ArrayList<ProcSec>();
	}

	public void analyze() {
		for (String[] cols : recList) {
			logger.debug(String.join("|", cols));
			if ((cols.length == 2) && (Const.KEY_SECTION.equals(cols[1]))) {
				secList.add(new ProcSec(cols[0]));
			}
		}
//		if(true)return;
		ProcSec caller = null;
		for (String[] cols : recList) {
			if ((cols.length == 2) && (Const.KEY_SECTION.equals(cols[1]))) {
				caller = searchSec(cols[0]);
			} else if ((cols.length == 1) && (KEY_EXIT.equals(cols[0]))) {
				caller = null;
			} else {
				for (Integer i : searchPerformSec(cols)) {
					caller.addCalledSec(searchSec(cols[i + 1]));
				}
			}
		}
	}

	/**
	 * SECTIONを呼び出すPERFORMの位置を返す。 PERFORMの次が数値,UNTIL,VARYINGの場合は対象外。
	 * 
	 * @param cols
	 * @return
	 */
	private List<Integer> searchPerformSec(String[] cols) {
		List<Integer> retList = new ArrayList<Integer>();
		if (searchCol(cols, KEY_PERFORM)) {
			// PERFORMがあり、UNTILが無いレコードが対象。
			for (int i = 0; i < cols.length; i++) {
				if (KEY_PERFORM.equals(cols[i])) {
					if (!isInt(cols[i + 1]) && !KEY_VARYONG.equals(cols[i + 1])
							&& !KEY_UNTIL.equals(cols[i + 1])) {
						retList.add(i);
					}
				}
			}
		}
		return retList;
	}

	private boolean searchCol(String[] cols, String search) {
		for (String col : cols) {
			if (search.equals(col)) {
				return true;
			}
		}
		return false;
	}

	private boolean isInt(String val) {
		try {
			Integer.parseInt(val);
		} catch (NumberFormatException pe) {
			return false;
		}
		return false;
	}

	private ProcSec searchSec(String secName) {
		int counter = 0;
		for (ProcSec sec : secList) {
			counter++;
			if (secName.equals(sec.name)) {
				return sec;
			}
		}
		throw new RuntimeException("List<ProcSec> secList 異常 count " + counter + " " + secName);
	}

	public void logoutContent() {
		for (ProcSec sec : secList) {
			logger.info("SECTION : " + sec.name);
			for (ProcSec called : sec.callSecList) {
				logger.info("\tcalled : " + called.name);
			}
		}
	}

	public void logoutTree(ProcSec sec, String prefix) {
		logger.info(prefix + sec.name);
		for (ProcSec child : sec.callSecList) {
			logoutTree(child, prefix + "\t");
		}
	}

	class ProcSec {
		String name;
		List<ProcSec> callSecList;

		ProcSec(String name) {
			this.name = name;
			this.callSecList = new ArrayList<ProcSec>();
		}

		void addCalledSec(ProcSec sec) {
			this.callSecList.add(sec);
		}
	}
}
