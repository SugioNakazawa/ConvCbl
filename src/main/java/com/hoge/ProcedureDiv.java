package com.hoge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcedureDiv extends BaseDiv {
	static Logger logger = LoggerFactory.getLogger(ProcedureDiv.class.getName());

	private static final String KEY_EXIT = "EXIT";
	private static final String KEY_PERFORM = "PERFORM";

	private static final String KEY_VARYONG = "VARYING";

	private static final String KEY_UNTIL = "UNTIL";

	private static final String KEY_READ = "READ";

	private static final String KEY_WRITE = "WRITE";

	List<ProcSec> secList;
	/** CmdTreeのルート **/
	ExecCmd rootCmd;
	/** CMD解析にてPERFORMジャンプの戻り場所を保持する **/
	private Deque<String[]> deque;

	public ProcedureDiv() {
		super();
		secList = new ArrayList<ProcSec>();
		deque = new ArrayDeque<>();
	}

	public void analyze() {
		// SECTION分け
		for (String[] cols : recList) {
//			logger.debug(String.join("|", cols));
			if ((cols.length == 2) && (Const.KEY_SECTION.equals(cols[1]))) {
				secList.add(new ProcSec(cols));
			}
		}
		// 呼び出し関係構築。READ/WRITEフラグ付与。
		ProcSec caller = null;
		for (String[] cols : recList) {
//			logger.debug(String.join(" ", cols)); // for debug see line
			if ((cols.length == 2) && (Const.KEY_SECTION.equals(cols[1]))) {
				caller = searchSec(cols[0]);
			} else if ((cols.length == 1) && (KEY_EXIT.equals(cols[0]))) {
				caller = null;
			} else {
				// PERFORM命令があるか
				for (Integer i : searchPerformSec(cols)) {
					caller.addCalledSec(searchSec(cols[i + 1]));
				}
				// READ/WRITEがあるか
				if (searchCol(cols, KEY_READ) >= 0) {
					caller.setRead(true);
				}
				if (searchCol(cols, KEY_WRITE) >= 0) {
					caller.setWrite(true);
				}
			}
		}
		// Cmd構成
		for (String[] cols : recList) {

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
		if (searchCol(cols, KEY_PERFORM) >= 0) {
			for (int i = 0; i < cols.length; i++) {
				if (KEY_PERFORM.equals(cols[i])) {
					if (searchSec(cols[i + 1]) != null) {
						// PERFORMの次がSECTIONの場合は呼び出しに追加
						retList.add(i);
					}
				}
			}
		}
		return retList;
	}

	/**
	 * 行内のワードの場所を取得。ないときは-1
	 * 
	 * @param sentence
	 * @param search
	 * @return
	 */
	private int searchCol(String[] sentence, String... searchs) {
		for (int i = 0; i < sentence.length; i++) {
			boolean match = true;
			for (int j = 0; j < searchs.length; j++) {
				if (!sentence[i + j].equals(searchs[j])) {
					match = false;
				}
			}
			if (match) {
				return i;
			}
		}
		return -1;
	}

	private Integer[] searchCols(String[] sentence, String... searchs) {
		List<Integer> retList = new ArrayList<Integer>();
		for (int i = 0; i < sentence.length; i++) {
			boolean match = true;
			for (int j = 0; j < searchs.length; j++) {
				if (!sentence[i + j].equals(searchs[j])) {
					match = false;
				}
			}
			if (match) {
				retList.add(i);
			}
		}
		return retList.toArray(new Integer[retList.size()]);
	}

	private boolean isInt(String val) {
		try {
			Integer.parseInt(val);
		} catch (NumberFormatException pe) {
			return false;
		}
		return true;
	}

	private ProcSec searchSec(String secName) {
		int counter = 0;
		for (ProcSec sec : secList) {
			counter++;
			if (secName.equals(sec.name)) {
				return sec;
			}
		}
		return null;
//		throw new RuntimeException("List<ProcSec> secList 異常 count " + counter + " " + secName);
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
		String txt = "";
		if (sec.isRead()) {
			txt += " " + KEY_READ;
		}
		if (sec.isWrite()) {
			txt += " " + KEY_WRITE;
		}
		int index = recList.indexOf(sec.cols);
		logger.info(prefix + sec.name + txt + " index=" + index);
		for (ProcSec child : sec.callSecList) {
			logoutTree(child, prefix + "\t");
		}
	}

	private void logoutCmdTree(ExecCmd cmd, String prefix) {
		String data = String.join(" ", cmd.execSentence);
		logger.info("data " + prefix + data);
		if (cmd.nextList.size() < 1) {
			return;
		} else if (cmd.nextList.size() == 1) {
			logoutCmdTree(cmd.nextList.get(0).nextCmd, prefix);
		} else {
			logger.debug("cmd.nextList.size()=" + cmd.nextList.size());
			for (NextCmd next : cmd.nextList) {
				logoutCmdTree(next.nextCmd, prefix + "\t" + next.condition + "\t");
			}
		}
	}

	public void createCmdTree() {
		String[] sentence = recList.get(0);
		rootCmd = new ExecCmd(null, sentence);
		Deque<String[]> locaQ = new ArrayDeque<>();
		rootCmd.addNextCmd(createCmd(rootCmd, recList.get(1), locaQ), "all");
		// 表示
//		logoutCmdTree(rootCmd, "");
	}

	private ExecCmd createCmd(ExecCmd prev, String[] sentence, Deque<String[]> locaQ) {
		ExecCmd exec = new ExecCmd(prev, sentence);
		// シーケンス実行時の次の行を事前検索
		String[] nextSentence = null;
		if ((recList.indexOf(sentence) > -1) && (recList.indexOf(sentence) < recList.size() - 1)) {
			nextSentence = recList.get(recList.indexOf(sentence) + 1);
		}
		if ("EXIT".equals(sentence[0])) {
			if ((sentence.length > 1) && ("PROGRAM".equals(sentence[1]))) {
				return exec;
			} else {
				// 戻るときの次行はスタックから
				String[] next = locaQ.pop();
				exec.addNextCmd(createCmd(exec, next, locaQ), "condition");
				return exec;
			}
		}
		// PERFORMジャンプ
		if (KEY_PERFORM.equals(sentence[0])) {
			if (searchSec(sentence[1]) != null) {
				// PERFORNの次がSECTION 次行をスタックへ
				locaQ.push(nextSentence);
				exec.addNextCmd(createCmd(exec, searchSec(sentence[1]).cols, locaQ), "condition");
				return exec;
			}
		}
		// 分岐コマンド
		if ("EVALUATE".equals(sentence[0])) {
			exec = doEvaluate(exec, sentence, locaQ, nextSentence);
			return exec;
		}
		if ("READ".equals(sentence[0])) {
			exec = doRead(exec, sentence, locaQ, nextSentence);
			return exec;
		}
		// sentenceから次の実行行を決定
		if ((recList.indexOf(sentence) > -1) && (recList.indexOf(sentence) < recList.size() - 1)) {
			exec.addNextCmd(createCmd(exec, nextSentence, locaQ), "condition");
		}
		return exec;
	}

	private ExecCmd doEvaluate(ExecCmd exec, String[] sentence, Deque<String[]> locaQ, String[] nextSentence) {
		Integer[] when_i = searchCols(sentence, "WHEN");
		int endEvaluate_i = searchCol(sentence, "END-EVALUATE");
		// 条件
		String cond0 = "";
		for (int i = 1; i < when_i[0]; i++) {
			cond0 += sentence[i] + " ";
		}
		logger.debug("cond0 = " + cond0);
		// WHEN分割
		for (int i = 1; i < when_i.length; i++) {
			String cond1 = "";
			String cond2 = "";
			if (isInt(sentence[when_i[i-1] + 1]) || "TRUE".equals(sentence[when_i[i-1] + 1])
					|| "FALSE".equals(sentence[when_i[i-1] + 1]) || "OTHER".equals(sentence[when_i[i-1] + 1])) {
				// 定数、BOOLEAN、OTHER
				cond1 = sentence[when_i[i] + 1];
				for (int j = when_i[i-1] + 2; j < when_i[i]; j++) {
					cond2 += sentence[j] + " ";
				}
			} else {
				for (int j = when_i[i-1] + 1; j < when_i[i]; j++) {
					cond1 += sentence[j] + " ";
				}
			}
			logger.debug("cond1 = " + cond1 + " cond2 = " + cond2);
			cond1 = "";
		}
		return exec;
	}

	private ExecCmd doRead(ExecCmd exec, String[] sentence, Deque<String[]> locaQ, String[] nextSentence) {
		int end_i = searchCol(sentence, "AT", "END");
		int notEnd_i = searchCol(sentence, "NOT", "AT", "END");
		int endRead_i = searchCol(sentence, "END-READ");
		{
			// AT END
			String[] subSentence = new String[notEnd_i - end_i - 2];
			for (int i = 0; i < subSentence.length; i++) {
				subSentence[i] = sentence[i + end_i + 2];
			}
//			logger.debug(String.join(" ", subSentence));
			ExecCmd endCmd = new ExecCmd(exec, subSentence);
			exec.addNextCmd(endCmd, "AT END");
			endCmd.addNextCmd(createCmd(endCmd, nextSentence, new ArrayDeque<>(locaQ)), "AT END");
		}
		{
			// NOT AT END
			String[] subSentence = new String[endRead_i - notEnd_i - 3];
			for (int i = 0; i < subSentence.length; i++) {
				subSentence[i] = sentence[i + notEnd_i + 3];
			}
//			logger.debug(String.join(" ", subSentence));
			ExecCmd endCmd = new ExecCmd(exec, subSentence);
			exec.addNextCmd(endCmd, "NOT AT END");
			endCmd.addNextCmd(createCmd(endCmd, nextSentence, new ArrayDeque<>(locaQ)), "AT END");
		}
		return exec;
	}

	class ExecCmd {
		ExecCmd prevCmd;
		List<NextCmd> nextList;
		String[] execSentence;

		ExecCmd(ExecCmd prev, String[] sentence) {
			this.prevCmd = prev;
			nextList = new ArrayList<NextCmd>();
			this.execSentence = sentence;
		}

		void addNextCmd(ExecCmd nextCmd, String cond) {
			this.nextList.add(new NextCmd(nextCmd, cond));
		}
	}

	class NextCmd {
		ExecCmd nextCmd;
		String condition;

		NextCmd(ExecCmd nextCmd, String condition) {
			this.nextCmd = nextCmd;
			this.condition = condition;
		}
	}

	class ProcSec {
		String name;
		String[] cols;
		boolean isRead = false;
		boolean isWrite = false;

		List<ProcSec> callSecList;

		ProcSec(String[] cols) {
			this.name = cols[0];
			this.cols = cols;
			this.callSecList = new ArrayList<ProcSec>();
		}

		void addCalledSec(ProcSec sec) {
			this.callSecList.add(sec);
		}

		public boolean isRead() {
			return isRead;
		}

		public void setRead(boolean isRead) {
			this.isRead = isRead;
		}

		public boolean isWrite() {
			return isWrite;
		}

		public void setWrite(boolean isWrite) {
			this.isWrite = isWrite;
		}
	}
}
