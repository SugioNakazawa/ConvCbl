package com.hoge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.activation.UnsupportedDataTypeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataDiv extends BaseDiv {
	private static Logger logger = LoggerFactory.getLogger(DataDiv.class.getName());
	private static final String KEY_DIVISION = "DIVISION";
	private static final String KEY_DATA = "DATA";
	private static final String KEY_SECTION = "SECTION";
	private static final String KEY_FILE = "FILE";
	private static final String KEY_FD = "FD";
	private static final String KEY_WORKING_STORAGE = "WORKING-STORAGE";
	/** 01 はレコードを示す **/
	private static final String KEY_01 = "01";
	/** COPY 句 **/
	private static final String KEY_COPY = "COPY";
	private static final String KEY_REPLACING = "REPLACING";
	private static final String KEY_BY = "BY";

	/** COBOLソースのフルパス **/
	private String fileName;
	/** FD SECTION の定義内容 **/
	private List<FdRec> fdList;
	/** WORKING-STORAGE SECTION の定義内容 **/
	private List<WsRec> wsList;

	/**
	 * コンストラクタ。
	 * 
	 * @param fileName COBOLソースファイルのフルパス。COPY句が同じディレクトrにあると想定。
	 */
	public DataDiv(String fileName) {
		super();
		this.fileName = fileName;
		this.fdList = new ArrayList<FdRec>();
		this.wsList = new ArrayList<WsRec>();
	}

	public List<FdRec> getFdList() {
		return fdList;
	}

	public List<WsRec> getWsList() {
		return wsList;
	}

	/**
	 * ソースコードを構成。
	 * 
	 * @throws IOException COPY句が存在しない。
	 */
	public void analyze() {
		List<String[]> tmp = new ArrayList<String[]>();
		for (String[] cols : recList) {
			if (KEY_COPY.equals(cols[0])) {
				tmp.addAll(doCopy(cols));
			} else {
				tmp.add(cols);
			}
		}
		recList = tmp;
		{
			BaseSec fd = null;
			boolean inWork = false;
			for (String[] cols : recList) {
//				logger.debug(String.join(" ", cols));
				if (KEY_DATA.equals(cols[0]) && KEY_DIVISION.equals(cols[1])) {
//					logger.debug("found DATA DIVISION");
				} else if (KEY_FILE.equals(cols[0]) && KEY_SECTION.equals(cols[1])) {
//					logger.debug("found FILE SECTION");
				} else if (KEY_WORKING_STORAGE.equals(cols[0]) && KEY_SECTION.equals(cols[1])) {
					inWork = true;
//					logger.debug("found WORKING-STORAGE SECTION");
				} else if (KEY_FD.equals(cols[0])) {
//					logger.debug("FD " + cols[1]);
					fd = new FdRec(cols[1]);
					this.fdList.add((FdRec) fd);
				} else if (KEY_01.equals(cols[0])) {
//					logger.debug("01 " + cols[1]);
					if (inWork) {
						fd = new WsRec();
						this.wsList.add((WsRec) fd);
					}
					fd.setRecName(cols[1]);
				} else {
					int check = Integer.parseInt(cols[0]);
					if ((cols.length > 2) && (check > 1)) {
						FdCol col = new FdCol(cols[1], cols[3]);
						fd.addFdCol(col);
					}
				}
			}
		}
	}

	/**
	 * FILE SECTION と WORKING-STORAGE SECTION の構成をログアウト。
	 */
	public void logoutContent() {
		for (FdRec fd : fdList) {
			logger.debug("***" + fd.fdFileName + ":" + fd.recName);
			for (FdCol col : fd.fdColList) {
				logger.debug("\t" + col.colName + " : " + col.colType);
			}
		}
		for (WsRec fd : wsList) {
			logger.debug("***" + fd.fdFileName + ":" + fd.recName);
			for (FdCol col : fd.fdColList) {
				logger.debug("\t" + col.colName + " : " + col.colType);
			}
		}
	}

	public abstract class BaseSec {
		String recName;
		List<FdCol> fdColList;

		public BaseSec() {
			this.fdColList = new ArrayList<FdCol>();
		}

		public void setRecName(String recName) {
			this.recName = recName;
		}

		public void addFdCol(FdCol col) {
			this.fdColList.add(col);
		}

		public List<FdCol> getFdColList() {
			return this.fdColList;
		}
	}

	public class FdRec extends BaseSec {
		String fdFileName;

		public FdRec(String fdFileName) {
			super();
			this.fdFileName = fdFileName;
		}
	}

	public class WsRec extends BaseSec {
		String fdFileName;

		public WsRec() {
			super();
		}
	}

	public class FdCol {
		String colName;
		String colType;

		public FdCol(String colName, String colType) {
			this.colName = colName;
			this.colType = colType;
		}
	}

	/**
	 * COPY句処理。
	 * 
	 * @param string
	 * @throws IOException
	 */
	private List<String[]> doCopy(String[] cols) {
		File dir = new File(fileName);
		try {
			List<String[]> retList = CblSource.read(dir.getParent() + "/" + cols[1]);
			if (cols.length == 2) {
				return retList;
			} else if (KEY_REPLACING.equals(cols[2])) {
				return replace(retList, cols);
			}
			String msg = "COPY句ではREPLACING以外はサポートしていません。";
			throw new UnsupportedDataTypeException(msg);
		} catch (IOException e) {
			// COPY句が見つからない。
			String msg = "コピー句 " + cols[1] + " がありません。";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
	}

	private List<String[]> replace(List<String[]> retList, String[] copyRec) {
		String regex = Pattern.quote(copyRec[search(copyRec, KEY_REPLACING) + 1].replaceAll("=", ""));
		String replacement = copyRec[search(copyRec, KEY_BY) + 1].replaceAll("=", "");
		for (int i = 0; i < retList.size(); i++) {
			for (int j = 0; j < retList.get(i).length; j++) {
				retList.get(i)[j] = retList.get(i)[j].replaceAll(regex, replacement);
			}
		}
		return retList;
	}

	private int search(String[] cols, String key) {
		for (int i = 0; i < cols.length; i++) {
			if (key.equals(cols[i])) {
				return i;
			}
		}
		return -1;
	}
}
