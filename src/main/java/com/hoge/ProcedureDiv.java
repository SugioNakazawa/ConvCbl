package com.hoge;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcedureDiv extends BaseDiv {
	static Logger logger = LoggerFactory.getLogger(ProcedureDiv.class.getName());
	// 命令文
	private static final String KEY_EVALUATE = "EVALUATE";
	private static final String KEY_MOVE = "MOVE";
	private static final String KEY_PERFORM = "PERFORM";
	private static final String KEY_READ = "READ";
	private static final String KEY_WRITE = "WRITE";
	/** １行に記述できる命令単語の一覧 **/
	private static String[] EXEC_WORD_LIST = { KEY_EVALUATE, KEY_MOVE, KEY_PERFORM, KEY_READ, KEY_WRITE };
	// 補助
	private static final String KEY_AT = "AT";
	private static final String KEY_END = "END";
	private static final String KEY_END_EVALUATE = "END-EVALUATE";
	private static final String KEY_END_PERFORM = "END-PERFORM";
	private static final String KEY_END_READ = "END-READ";
	private static final String KEY_EXIT = "EXIT";
	private static final String KEY_FALSE = "FALSE";
	private static final String KEY_NOT = "NOT";
	private static final String KEY_OTHER = "OTHER";
	private static final String KEY_PROGRAM = "PROGRAM";
	private static final String KEY_TRUE = "TRUE";
	private static final String KEY_UNTIL = "UNTIL";
	private static final String KEY_VARYONG = "VARYING";
	private static final String KEY_WHEN = "WHEN";
	// メッセージ
	private static String NOT_FOUND_END_PERFORM = "対応するEND−PERFORMがありません。{0}";

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
		for (ProcSec sec : secList) {
			if (secName.equals(sec.name)) {
				return sec;
			}
		}
		return null;
	}

	public void logoutRecList() {
		for (String[] sentence : recList) {
			logger.info(String.join(" ", sentence));
		}
	}

	public void logoutSecTree() {
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

	/**
	 * CMDツリーを出力。
	 * 
	 * @param filePath ファイルパス。nullの場合はINFOログとして出力。
	 * @throws IOException
	 */
	public void logoutCmdTree(String filePath) throws IOException {
		FileWriter fw = null;
		if (filePath != null) {
			File file = new File(filePath);
			fw = new FileWriter(file);
		}
		logoutCmdTree(rootCmd, "", fw);
		if (fw != null) {
			fw.close();
		}
	}

	private void logoutCmdTree(ExecCmd cmd, String prefix, FileWriter fw) throws IOException {
		String data = String.join(" ", cmd.execSentence);
		if (fw == null) {
			logger.info(prefix + data);
		} else {
			fw.write(prefix + data + "\n");
		}
		if (cmd.nextList.size() < 1) {
			return;
		} else if (cmd.nextList.size() == 1) {
			logoutCmdTree(cmd.nextList.get(0).nextCmd, prefix, fw);
		} else {
			if (fw == null) {
				logger.debug("*** branch " + cmd.nextList.size() + " ***");
			} else {
				fw.write("*** branch " + cmd.nextList.size() + " ***" + "\n");

			}
			for (NextCmd next : cmd.nextList) {
				logoutCmdTree(next.nextCmd, prefix + "\t" + next.condition + "\t", fw);
//				logoutCmdTree(next.nextCmd, prefix + next.condition + "\t", fw);
			}
		}
	}

	public void createCmdTree() {
		String[] sentence = recList.get(0);
		rootCmd = new ExecCmd(null, sentence);
		Deque<String[]> localQue = new ArrayDeque<>();
		// EXIT PROGRAMまでのrecListをQueに登録。
//		for (String[] rec : recList) {
//			localQue.addLast(rec);
//			if ((rec.length > 1) && (KEY_EXIT.equals(rec[0]) && (KEY_PROGRAM.equals(rec[1])))) {
//				break;
//			}
//		}
		rootCmd.addNextCmd(createCmd(rootCmd, recList.get(1), localQue), "start");
	}

	/**
	 * ExecCmdを生成。次のCmdも追加し完了した状態とする。
	 * 
	 * @param prev     呼び出し元Cmd。
	 * @param sentence 解析する行。複数命令文がある場合あり。
	 * @param localQue Perform戻り場所のスタック。
	 * @return
	 */
	private ExecCmd createCmd(ExecCmd prev, String[] sentence, Deque<String[]> localQue) {
		logger.debug("in ExecCmd10 sentence:" + String.join(" ", sentence) + " que:" + localQue.size());
		String[] nextSentence = null;
		// 次の行の設定
		logger.debug("getExecIndex(sentence).size()=" + getExecIndex(sentence).size());
		if ((getExecIndex(sentence).size() > 1) && (!KEY_READ.equals(sentence[0]))
				&& (!KEY_EVALUATE.equals(sentence[0]))) {
			// 行に複数命令がある場合は２番め以降を次へ
			nextSentence = this.selectArray(sentence, getExecIndex(sentence).get(1));
			sentence = this.selectArray(sentence, 0, getExecIndex(sentence).get(1));
		} else {
			// recListの次の行
			int next_i = recList.indexOf(sentence);
			if ((next_i > -1) && (next_i < recList.size() - 1)) {
				nextSentence = recList.get(next_i + 1);
				logger.debug("***** check ***** " + String.join(" ", nextSentence));
				if ((getExecIndex(nextSentence).size() > 1) && (next_i < recList.size() - 2)
						&& (!KEY_READ.equals(nextSentence[0]))
						&& (!KEY_EVALUATE.equals(nextSentence[0]))
						) {
					// 次の行が複数命令の場合はスタックに積む。（READ以外）
					localQue.push(recList.get(next_i + 2));
				}
			} else {
				if (localQue.isEmpty()) {
					return new ExecCmd(prev, sentence);
				}
				nextSentence = localQue.pop();
				logger.debug("pop  : " + String.join(" ", nextSentence));
			}
		}
		logger.debug("in ExecCmd21 sentence:" + String.join(" ", sentence) + " que:" + localQue.size());
		logger.debug("in ExecCmd22 nextSentence:" + String.join(" ", nextSentence) + " que:" + localQue.size());
		ExecCmd exec = new ExecCmd(prev, sentence);
		if (KEY_EXIT.equals(sentence[0])) {
			return doExit(exec, sentence, localQue, nextSentence);
		}
		// PERFORMジャンプ
		if (KEY_PERFORM.equals(sentence[0])) {
			return doPerform(exec, sentence, localQue, nextSentence);
		}
		// 分岐コマンド
		if (KEY_EVALUATE.equals(sentence[0])) {
			exec = doEvaluate(exec, sentence, localQue, nextSentence);
			return exec;
		}
		if (KEY_READ.equals(sentence[0])) {
			return doRead(exec, sentence, localQue, nextSentence);
		}
		exec.addNextCmd(createCmd(exec, nextSentence, localQue), "all");
		return exec;
	}

	private ExecCmd doPerform(ExecCmd exec, String[] sentence, Deque<String[]> locaQ, String[] nextSentence) {
		logger.debug("inP sentence:" + String.join(" ", sentence));
		logger.debug("inP nextSentence:" + String.join(" ", nextSentence));
		if (searchSec(sentence[1]) != null) {
			// PERFORNの次がSECTION 次行をスタックへ
			locaQ.push(nextSentence);
			logger.debug("push : " + String.join(" ", nextSentence));
			exec.addNextCmd(createCmd(exec, searchSec(sentence[1]).cols, locaQ), "all");
		} else {
			exec.addNextCmd(createCmd(exec, nextSentence, locaQ), "all");

//		}else {
//			//	PERFORMの次がUNTILの場合。対応するEND-PERFORMまでがnext
//			if(KEY_UNTIL.equals(sentence[1])) {
//				//	END-PERFORMを探す
//				Integer[] endPs = searchCols(sentence,KEY_END_PERFORM);
//				if(endPs.length<1) {
//					throw new RuntimeException(MessageFormat.format(NOT_FOUND_END_PERFORM, String.join(" ", sentence)));
//				}
//				//	UNTIL以降には条件式があるため次の命令を探す。
//				List<Integer> execs = this.getExecIndex(sentence);
//				logger.debug("*this exec*"+String.join(" ", selectArray(sentence,0,execs.get(1))));
//				exec.setSentence(selectArray(sentence,0,execs.get(1)));
//				if(execs.size()>1) {
//					nextSentence = this.selectArray(sentence, execs.get(1), endPs[endPs.length -1]);
//					logger.debug("*next*"+String.join(" ", nextSentence));
//				}
//				exec.addNextCmd(createCmd(exec, nextSentence, locaQ), "all");
//			}
		}
		return exec;
	}

	private ExecCmd doEvaluate(ExecCmd exec, String[] sentence, Deque<String[]> locaQ, String[] nextSentence) {
		Integer[] when_i = searchCols(sentence, KEY_WHEN);
		int endEvaluate_i = searchCol(sentence, KEY_END_EVALUATE);
		exec.setSentence(selectArray(sentence, 0, 1));
		// 条件
		String cond0 = "";
		for (int i = 1; i < when_i[0]; i++) {
			cond0 += sentence[i] + " ";
		}
		// WHEN分割
		List<String> cond1 = new ArrayList<String>();
		locaQ.push(nextSentence);
		for (int i = 0; i < when_i.length; i++) {
			String next = "";
			if (isInt(sentence[when_i[i] + 1]) || KEY_TRUE.equals(sentence[when_i[i] + 1])
					|| KEY_FALSE.equals(sentence[when_i[i] + 1]) || KEY_OTHER.equals(sentence[when_i[i] + 1])) {
				// 定数、BOOLEAN、OTHER
				cond1.add(sentence[when_i[i] + 1]);
				int to_i = i < when_i.length - 1 ? when_i[i + 1] : endEvaluate_i;
				for (int j = when_i[i] + 2; j < to_i; j++) {
					next += sentence[j] + " ";
				}
			} else {
				int to_i = i < when_i.length - 1 ? when_i[i + 1] : endEvaluate_i;
				for (int j = when_i[i] + 1; j < to_i; j++) {
					cond1.add(sentence[j]);
				}
			}
			// next からは次のWHENと同じとなる。
			if (next != "") {
//				logger.debug("cond0 = " + cond0 + " cond1 = " + String.join(" OR ", cond1) + " next = " + next);
//				locaQ.push(nextSentence);
				exec.addNextCmd(createCmd(exec, next.split(" "), new ArrayDeque<>(locaQ)), cond0 + " = " + cond1);
				cond1.clear();
				;
			}
		}
		return exec;
	}

	private ExecCmd doRead(ExecCmd exec, String[] sentence, Deque<String[]> locaQ, String[] nextSentence) {
		logger.debug("read sentence:" + String.join(" ", sentence));
		logger.debug("read nextSentence:" + String.join(" ", nextSentence));
		int end_i = searchCol(sentence, KEY_AT, KEY_END);
		int notEnd_i = searchCol(sentence, KEY_NOT, KEY_AT, KEY_END);
		int endRead_i = searchCol(sentence, KEY_END_READ);
		// CMDにセットしたAT END 以降の文を削除
		exec.setSentence(selectArray(sentence, 0, end_i));
		{
			// AT END
			String[] subSentence = new String[notEnd_i - end_i - 2];
			for (int i = 0; i < subSentence.length; i++) {
				subSentence[i] = sentence[i + end_i + 2];
			}
//			logger.debug(String.join(" ", subSentence));
			ExecCmd endCmd = new ExecCmd(exec, subSentence);
			exec.addNextCmd(endCmd, KEY_AT + " " + KEY_END);
			endCmd.addNextCmd(createCmd(endCmd, nextSentence, new ArrayDeque<>(locaQ)), KEY_AT + " " + KEY_END);
		}
		{
			// NOT AT END
			String[] subSentence = new String[endRead_i - notEnd_i - 3];
			for (int i = 0; i < subSentence.length; i++) {
				subSentence[i] = sentence[i + notEnd_i + 3];
			}
//			logger.debug(String.join(" ", subSentence));
			ExecCmd endCmd = new ExecCmd(exec, subSentence);
			exec.addNextCmd(endCmd, KEY_NOT + " " + KEY_AT + " " + KEY_END);
			endCmd.addNextCmd(createCmd(endCmd, nextSentence, new ArrayDeque<>(locaQ)),
					KEY_NOT + " " + KEY_AT + " " + KEY_END);
		}
		return exec;
	}

	private ExecCmd doExit(ExecCmd exec, String[] sentence, Deque<String[]> locaQ, String[] nextSentence) {
		if ((sentence.length > 1) && (KEY_PROGRAM.equals(sentence[1]))) {
			// プログラム終了。最後のCmd。
		} else {
			// 戻るときの次行はスタックから
			String[] next = locaQ.pop();
			exec.addNextCmd(createCmd(exec, next, locaQ), "all");
		}
		return exec;
	}

	/**
	 * 命令予約後の場所を返す。
	 * 
	 * @param sentence
	 * @return
	 */
	private List<Integer> getExecIndex(String[] sentence) {
		List<Integer> ret = new ArrayList<Integer>();
		for (int i = 0; i < sentence.length; i++) {
			for (String search : EXEC_WORD_LIST) {
				if (search.equals(sentence[i])) {
					ret.add(i);
				}
			}
		}
		return ret;
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

		public void setSentence(String[] sentence) {
			this.execSentence = sentence;
		}

		void addNextCmd(ExecCmd nextCmd, String cond) {
			this.nextList.add(new NextCmd(nextCmd, cond));
		}
	}

	private String[] selectArray(String[] sentence, int start) {
		return selectArray(sentence, start, sentence.length);
	}

	private String[] selectArray(String[] sentence, int start, int end) {
		String[] newSentence = new String[end - start];
		for (int i = 0; i < newSentence.length; i++) {
			newSentence[i] = sentence[start + i];
		}
		return newSentence;
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
