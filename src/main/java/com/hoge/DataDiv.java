package com.hoge;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoge.AsakusaDmdl.DmdlModel;

/**
 * DATA DIVISION 内容を保持。 COBOLソースコードから生成。 Asakusa DMDL を生成。
 * 
 * @author nakazawasugio
 *
 */
public class DataDiv extends BaseDiv {
	private static Logger logger = LoggerFactory.getLogger(DataDiv.class.getName());
	//
	private static final String KEY_FILE = "FILE";
	private static final String KEY_FD = "FD";
	private static final String KEY_WORKING_STORAGE = "WORKING-STORAGE";
	private static final String KEY_LINKAGE = "LINKAGE";
	/** 01 はレコードを示す **/
	private static final String KEY_01 = "01";
	/** COPY 句 **/
	private static final String KEY_COPY = "COPY";
	private static final String KEY_REPLACING = "REPLACING";
	private static final String KEY_BY = "BY";

	/** COBOLソースのフルパス **/
	private Path sourcePath;
	/** FILE SECTION の定義内容 **/
	private List<FdRec> fdList;
	/** WORKING-STORAGE SECTION の定義内容 **/
	private List<WsRec> wsList;
	/** LINKAGE SECTION の定義内容 **/
	private List<LkRec> lkList;

	/**
	 * コンストラクタ。
	 * 
	 * @param fileId COBOLソースファイルのフルパス。COPY句が同じディレクトrにあると想定。
	 */
	public DataDiv(Path sourcePath) {
		super();
		this.sourcePath = sourcePath;
		this.fdList = new ArrayList<FdRec>();
		this.wsList = new ArrayList<WsRec>();
		this.lkList = new ArrayList<LkRec>();
	}

	public List<FdRec> getFdList() {
		return fdList;
	}

	public List<WsRec> getWsList() {
		return wsList;
	}

	public List<LkRec> getLkList() {
		return lkList;
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
			boolean inLinkage = false;
			for (String[] cols : recList) {
				if (Const.KEY_DATA.equals(cols[0]) && Const.KEY_DIVISION.equals(cols[1])) {
				} else if (KEY_FILE.equals(cols[0]) && Const.KEY_SECTION.equals(cols[1])) {
				} else if (KEY_WORKING_STORAGE.equals(cols[0]) && Const.KEY_SECTION.equals(cols[1])) {
					inWork = true;
					inLinkage = false;
				} else if (KEY_LINKAGE.equals(cols[0]) && Const.KEY_SECTION.equals(cols[1])) {
					inWork = false;
					inLinkage = true;
				} else if (KEY_FD.equals(cols[0])) {
					fd = new FdRec(cols[1]);
					this.fdList.add((FdRec) fd);
				} else if (KEY_01.equals(cols[0])) {
					if (inWork) {
						fd = new WsRec();
						this.wsList.add((WsRec) fd);
					} else if (inLinkage) {
						fd = new LkRec();
						this.lkList.add((LkRec) fd);
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
			logger.info("recName=" + fd.recName + " fdName=" + fd.fdName);
			for (FdCol col : fd.fdColList) {
				logger.info("\t" + col.colName + " : " + col.colType);
			}
		}
		for (WsRec fd : wsList) {
			logger.info("recName=" + fd.recName);
			for (FdCol col : fd.fdColList) {
				logger.info("\t" + col.colName + " : " + col.colType);
			}
		}
		for (LkRec fd : lkList) {
			logger.info("recName=" + fd.recName);
			for (FdCol col : fd.fdColList) {
				logger.info("\t" + col.colName + " : " + col.colType);
			}
		}
	}

	public void createDmdl(Path outFile) throws IOException {
		AsakusaDmdl target = new AsakusaDmdl();
		for (FdRec fd : fdList) {
			DmdlModel model = target.new DmdlModel(fd.recName, fd.recName, "file", "UTF-8");
			for (FdCol col : fd.getFdColList()) {
				model.addColumn(target.new DmdlColumn(col.colName, convType(col.colType), col.colName));
			}
			target.addModel(model);
		}
		target.createDmdl(outFile);
	}

	/**
	 * COBOL変数型からAsakusa型へ変換。
	 * 
	 * @param colType
	 * @return
	 */
	private String convType(String colType) {
		// TODO DATE,DATETIMEは使用できるか。
		if (colType.startsWith("X")) {
			return "TEXT";
		}
		return "DECIMAL";
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

	/**
	 * FILE SECTION
	 * 
	 * @author nakazawasugio
	 *
	 */
	public class FdRec extends BaseSec {
		String fdName;

		public FdRec(String fdFileName) {
			super();
			this.fdName = fdFileName;
		}
	}

	/**
	 * WORKING=STRAGE SECTION
	 * 
	 * @author nakazawasugio
	 *
	 */
	public class WsRec extends BaseSec {
		public WsRec() {
			super();
		}
	}

	/**
	 * LINKAGE SECTION
	 * 
	 * @author nakazawasugio
	 *
	 */
	public class LkRec extends BaseSec {
		public LkRec() {
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
	 * @param sentence
	 * @return
	 */
	private List<String[]> doCopy(String[] sentence) {
		String copyFile = sourcePath.toAbsolutePath().getParent() + "/" + sentence[1];
		try {
			CblSourceReader reader = new CblSourceReader(Paths.get(copyFile));
			List<String[]> retList = reader.read();
			if (sentence.length == 2) {
				return retList;
			} else if (KEY_REPLACING.equals(sentence[2])) {
				return replace(retList, sentence);
			}
			logger.error(Const.MSG_NO_SUPPORT);
			throw new RuntimeException(Const.MSG_NO_SUPPORT);
		} catch (IOException e) {
			String msg = MessageFormat.format(Const.MSG_NOT_FOUND_COPY, copyFile);
			logger.error(msg);
			throw new RuntimeException(msg);
		}
	}

	/**
	 * COPY句内のREPLACE処理
	 * 
	 * @param retList
	 * @param copyRec
	 * @return
	 */
	private List<String[]> replace(List<String[]> retList, String[] copyRec) {
		String regex = Pattern.quote(copyRec[ComUtil.search(copyRec, KEY_REPLACING) + 1].replaceAll("=", ""));
		String replacement = copyRec[ComUtil.search(copyRec, KEY_BY) + 1].replaceAll("=", "");
		for (int i = 0; i < retList.size(); i++) {
			for (int j = 0; j < retList.get(i).length; j++) {
				retList.get(i)[j] = retList.get(i)[j].replaceAll(regex, replacement);
			}
		}
		return retList;
	}
}
