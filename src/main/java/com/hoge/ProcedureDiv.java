package com.hoge;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoge.ProcedureDiv.BranchCmd.BranchElm;

/**
 * PROCEDURE DIVISION 解析。
 * 
 * @author nakazawasugio <br>
 *         課題１：以下のような場合にCLOSE文が１つにならない。 030800 CLOSE IN01-FILE 030900
 *         OT01-FILE.<br>
 *         課題２：EVALUATE内の複数PERFORMで１つ目のPERFORMしか処理していない。 <br>
 *         課題３： PERFORM 手続き名 UNTIL の戻りペアの設定をしていない。<br>
 * 
 */
public class ProcedureDiv extends BaseDiv {
	static Logger logger = LoggerFactory.getLogger(ProcedureDiv.class.getName());
	static boolean LONG_LABEL = true;
	// 命令文
	private static final String KEY_EVALUATE = "EVALUATE";
	private static final String KEY_IF = "IF";
	private static final String KEY_READ = "READ";
	/** 分岐命令 **/
	private static String[] BRANCH_WORD_LIST = { KEY_EVALUATE, KEY_IF, KEY_READ };

	private static final String KEY_COMPUTE = "COMPUTE";
	private static final String KEY_EXIT = "EXIT";
	private static final String KEY_MOVE = "MOVE";
	private static final String KEY_PERFORM = "PERFORM";
	private static final String KEY_END_PERFORM = "END-PERFORM";
	private static final String KEY_WRITE = "WRITE";
	/** 命令単語の一覧 **/
	private static String[] EXEC_WORD_LIST = { KEY_COMPUTE, KEY_EVALUATE, KEY_EXIT, KEY_IF, KEY_MOVE, KEY_PERFORM,
			KEY_END_PERFORM, KEY_READ, KEY_WRITE };
	private static final String KEY_END_EVALUATE = "END-EVALUATE";
	private static final String KEY_END_IF = "END-IF";
	private static final String KEY_END_READ = "END-READ";
	// 補助
	private static final String KEY_AT = "AT";
	private static final String KEY_END = "END";
	private static final String KEY_FALSE = "FALSE";
	private static final String KEY_NOT = "NOT";
	private static final String KEY_OTHER = "OTHER";
	private static final String KEY_PROGRAM = "PROGRAM";
	private static final String KEY_TRUE = "TRUE";
	private static final String KEY_UNTIL = "UNTIL";
	private static final String KEY_VARYONG = "VARYING";
	private static final String KEY_WHEN = "WHEN";
	private static final String KEY_THEN = "THEN";
	private static final String KEY_ELSE = "ELSE";
	/** DOTファイルに出力するCmdType。ここに指定したCmdのみを出力する。 **/
	private static CmdType[] DOT_CMD_FILTER = { CmdType.BRANCH, CmdType.EXEC, CmdType.DEFINE, CmdType.LABEL };
	/** ソース上にあらわれない場合に作成するコマンド **/
	private static final String[] KEY_CONTINUE = { "continue" };

	/** DOT作成時にネストするたびに加算し、条件違いのノードを別ノードとする。 **/
	private int nestCounter = 1;
	/** DOT作成時に分岐を展開=true、合流=falseするかを指定する。 **/
	private boolean isNextExpand = false;
	/** DOTファイルを出力するWriter **/
	private FileWriter dotFw = null;

	public void setNextExpand(boolean isNextExpand) {
		this.isNextExpand = isNextExpand;
	}

	List<ProcSec> secList;
	/** Cmdフローの先頭 **/
	ExecCmd rootCmd;

	public ProcedureDiv() {
		super();
		secList = new ArrayList<ProcSec>();
	}

	public void analyze() {
		// SECTION分け
		String[] defineSentence = null;
		for (String[] cols : recList) {
			if ((cols.length == 2) && (Const.KEY_SECTION.equals(cols[1]))) {
				defineSentence = cols;
			} else if (KEY_EXIT.equals(cols[0])) {
				secList.add(new ProcSec(defineSentence, cols));
			}
		}
		// 呼び出し関係構築。READ/WRITEフラグ付与。
		ProcSec caller = null;
		for (String[] cols : recList) {
			if ((cols.length == 2) && (Const.KEY_SECTION.equals(cols[1]))) {
				caller = searchSec(cols[0]);
			} else if ((cols.length == 1) && (KEY_EXIT.equals(cols[0]))) {
				caller = null;
			} else {
				// PERFORM命令があるか
				for (Integer i : searchAllPerformSec(cols)) {
					caller.addCalledSec(searchSec(cols[i + 1]));
				}
				// READ/WRITEがあるか
				if (searchFirstCol(cols, KEY_READ) >= 0) {
					caller.setRead(true);
				}
				if (searchFirstCol(cols, KEY_WRITE) >= 0) {
					caller.setWrite(true);
				}
			}
		}
		// コマンド作成
		{
			rootCmd = new ExecCmd(null, recList.get(0), CmdType.LABEL);
			Deque<String[]> localQue = new ArrayDeque<>();
			// コマンドリストを作成。分岐なし
			rootCmd.addNextCmd(createCmd(rootCmd, recList.get(1), localQue, null), "start");
			// 分岐文を展開。１回目。
			expandFlatToBranch(rootCmd);
			// 分岐内分岐。２回目。
			expandInBranch(rootCmd);
		}
	}

	/**
	 * 分岐内のコマンドの拡張（分岐、PERFORMジャンプ）を行う。
	 * 
	 * @param cmd
	 */
	private void expandInBranch(ExecCmd cmd) {
		if ((cmd.type == CmdType.BRANCH) && (searchExecAllIndex(cmd.execSentence).size() > 1)) {
			createExpandBranch(cmd);
		}
		for (NextCmd next : cmd.nextList) {
			expandInBranch(next.nextCmd);
		}
	}

	/**
	 * フラット（分岐なし）リストを分岐付きに拡張。
	 * 
	 * @param cmd
	 */
	private void expandFlatToBranch(ExecCmd cmd) {
		while (cmd.nextList.size() > 0) {
			if (cmd.type == CmdType.BRANCH) {
				ExecCmd endCmd = cmd.nextList.get(0).nextCmd;
				createExpandBranch(cmd);
//				ExecCmd prevCmd = cmd.prevCmd;
//				cmd = createExpandBranch(cmd);
//				NextCmd repCmd = new NextCmd(cmd, "all");
//				prevCmd.nextList.set(0, repCmd);
				cmd = endCmd;
			} else {
				cmd = cmd.nextList.get(0).nextCmd;
			}
		}
	}

	/**
	 * 分岐コマンドの分岐を展開する。
	 * 
	 * @param cmd
	 * @return
	 */
	private ExecCmd createExpandBranch(ExecCmd cmd) {
		BranchCmd branchCmd = null;
		if (KEY_IF.equals(cmd.execSentence[0])) {
			branchCmd = expandIf(cmd.execSentence);
		} else if (KEY_EVALUATE.equals(cmd.execSentence[0])) {
			branchCmd = expandEvaluate(cmd.execSentence);
		} else if (KEY_READ.equals(cmd.execSentence[0])) {
			branchCmd = expandRead(cmd.execSentence);
		}
		// 分岐の合流ポイント
		ExecCmd endCmd = cmd.nextList.get(0).nextCmd;
		// 現在のnextをクリア
		cmd.clearNextCmd();
		for (BranchElm elm : branchCmd.branchList) {
//			logger.debug("branch:" + elm.cond + " sentence : " + String.join(" ", elm.sentence));
			ExecCmd b1 = createCmd(cmd, elm.sentence, new ArrayDeque<>(), endCmd);
			cmd.addNextCmd(b1, elm.cond);
		}
		// 分岐文を先頭のみに変更
		shrinkBranchSentence(cmd);
		return cmd;
	}

	void shrinkBranchSentence(ExecCmd cmd) {
		if (KEY_IF.equals(cmd.execSentence[0])) {
			if (searchFirstCol(cmd.execSentence, KEY_THEN) > 0) {
				cmd.execSentence = selectArray(cmd.execSentence, 0, searchFirstCol(cmd.execSentence, KEY_THEN));
			} else {
				cmd.execSentence = selectArray(cmd.execSentence, 0, searchExecAllIndex(cmd.execSentence).get(1));
			}
		} else if (KEY_EVALUATE.equals(cmd.execSentence[0])) {
			cmd.execSentence = selectArray(cmd.execSentence, 0, searchFirstCol(cmd.execSentence, KEY_WHEN));
		} else if (KEY_READ.equals(cmd.execSentence[0])) {
			cmd.execSentence = selectArray(cmd.execSentence, 0, searchFirstCol(cmd.execSentence, KEY_AT));
		}
	}

	/**
	 * IF文解析。
	 * 
	 * @param sentence
	 * @return
	 */
	BranchCmd expandIf(String[] sentence) {
		BranchCmd ret = new BranchCmd(sentence);
		int then_i = searchFirstCol(sentence, KEY_THEN);
		int else_i = searchFirstCol(sentence, KEY_ELSE);
		int endIf_i = searchFirstCol(sentence, KEY_END_IF);
		int condEnd = 0;
		int thenStart = 0;
		int thenEnd = 0;
		int elseStart = 0;
		int elseEnd = 0;
		if (then_i < 0) {
			if (searchExecAllIndex(sentence).size() < 1) {
				String msg = "internal error";
				throw new RuntimeException(msg);
			} else if (searchExecAllIndex(sentence).size() < 2) {
				String msg = "IF文内に実行命令がありません。";
				throw new RuntimeException(msg);
			} else {
				// 最初はIFなので次を取得
				thenStart = searchExecAllIndex(sentence).get(1);
				if (endIf_i < 0) {
					thenEnd = sentence.length;
				} else {
					thenEnd = endIf_i;
				}
			}
			condEnd = thenStart - 1;
		} else {
			thenStart = then_i + 1;
			condEnd = then_i - 1;
			if (else_i < 0) {
				if (endIf_i < 0) {
					thenEnd = sentence.length;
				} else {
					thenEnd = endIf_i;
				}
			} else {
				thenEnd = else_i;
				elseStart = else_i + 1;
				if (endIf_i < 0) {
					elseEnd = sentence.length;
				} else {
					elseEnd = endIf_i;
				}
			}
		}
		ret.addBranchElm(String.join(" ", selectArray(sentence, 1, condEnd + 1)) + " TRUE",
				selectArray(sentence, thenStart, thenEnd));

		if (elseStart > 0) {
			ret.addBranchElm(String.join(" ", selectArray(sentence, 1, condEnd + 1)) + " FALSE",
					selectArray(sentence, elseStart, elseEnd));
		} else {
			ret.addBranchElm(String.join(" ", selectArray(sentence, 1, condEnd + 1)) + " FALSE", KEY_CONTINUE);
		}
//		ret.logout();
		return ret;
	}

	BranchCmd expandEvaluate(String[] sentence) {
		BranchCmd ret = new BranchCmd(sentence);
		Integer[] when_i = searchAllCols(sentence, KEY_WHEN);
		// 最終がEND-EVALUATEのときはカットしておく。
		if (searchFirstCol(sentence, KEY_END_EVALUATE) == sentence.length - 1) {
			sentence = selectArray(sentence, 0, sentence.length - 1);
		}
		// 条件 EVALUATEと最初のWHENの間
		String cond0 = "";
		for (int i = 1; i < when_i[0]; i++) {
			cond0 += sentence[i] + " ";
		}
		// 条件の値部分はWHENを連続して記述する場合はOR条件になるので１つのListに格納。
		List<String> cond1 = new ArrayList<String>();
		// WHEN分割
		for (int i = 0; i < when_i.length; i++) {
			// 条件値＋実行文 WHENiの次から次WHENi+1（もしくは最後)
			int start_i = when_i[i] + 1;
			int end_i = (i == when_i.length - 1) ? sentence.length : when_i[i + 1];
			String[] target = selectArray(sentence, start_i, end_i);
//			logger.debug("target " + i + " " + String.join(" ", target));
			String next = "";
			// 条件値：最初の命令の手前まで、実行分：最初の命令以降
			if (searchExecAllIndex(target).size() > 0) {
				cond1.add(String.join(" ", selectArray(target, 0, searchExecAllIndex(target).get(0))));
				next = String.join(" ", selectArray(target, searchExecAllIndex(target).get(0)));
			} else {
				cond1.add(String.join(" ", target));
				next = "";
			}
			// next からは次のWHENと同じとなる。
			if (next != "") {
				// 分岐
				ret.addBranchElm(cond0 + cond1, next.split(" "));
				cond1.clear();
				;
			}
		}
		return ret;
	}

	BranchCmd expandRead(String[] sentence) {
		BranchCmd ret = new BranchCmd(sentence);
		int atEnd_i = searchFirstCol(sentence, KEY_AT, KEY_END);
		int notAtEnd_i = searchFirstCol(sentence, KEY_NOT, KEY_AT, KEY_END);
		int endRead_i = searchFirstCol(sentence, KEY_END_READ);
		int condEnd = 0;
		int atEndStart = 0;
		int atEndEnd = 0;
		int notAtEndStart = 0;
		int notAtEndEnd = 0;
		if (atEnd_i < 0) {
			// AT ENDが存在しない場合は条件なしでよいのではないか。
			return ret;
		} else {
			atEndStart = atEnd_i + 2;
			condEnd = atEnd_i - 1;
			if (notAtEnd_i < 0) {
				if (endRead_i < 0) {
					atEndEnd = sentence.length - 1;
				} else {
					atEndEnd = endRead_i - 1;
				}
			} else {
				atEndEnd = notAtEnd_i - 1;
				notAtEndStart = notAtEnd_i + 3;
				if (endRead_i < 0) {
					notAtEndEnd = sentence.length - 1;
				} else {
					notAtEndEnd = endRead_i - 1;
				}
			}
		}
		ret.addBranchElm(String.join(" ", selectArray(sentence, 1, condEnd + 1)) + " AT END",
				selectArray(sentence, atEndStart, atEndEnd + 1));

		if (notAtEndStart > 0) {
			ret.addBranchElm(String.join(" ", selectArray(sentence, 1, condEnd + 1)) + " NOT AT END",
					selectArray(sentence, notAtEndStart, notAtEndEnd + 1));
		} else {
			ret.addBranchElm(String.join(" ", selectArray(sentence, 1, condEnd + 1)) + " NOT AT END", KEY_CONTINUE);
		}
		return ret;
	}

	/**
	 * SECTIONを呼び出しているPERFORMの位置をすべて返す。 PERFORMの次の単語がSECTION名であること。
	 *
	 * @param sentence
	 * @return
	 */
	private List<Integer> searchAllPerformSec(String[] sentence) {
		List<Integer> retList = new ArrayList<Integer>();
		if (searchFirstCol(sentence, KEY_PERFORM) >= 0) {
			for (int i = 0; i < sentence.length; i++) {
				if (KEY_PERFORM.equals(sentence[i])) {
					if (searchSec(sentence[i + 1]) != null) {
						// PERFORMの次がSECTIONの場合は呼び出しに追加
						retList.add(i);
					}
				}
			}
		}
		return retList;
	}

	/**
	 * 行内の単語（複数あり）の最初の場所を取得。ないときは-1
	 *
	 * @param sentence 被検索対象文
	 * @param search   検索対象単語。複数指定の場合は順序通りにすべてを含むものを検索。
	 * @return
	 */
	private int searchFirstCol(String[] sentence, String... searchs) {
		for (int i = 0; i < sentence.length - searchs.length + 1; i++) {
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

	/**
	 * 行内の単語（複数あり）のすべての場所を取得。ないときは-1
	 * 
	 * @param sentence
	 * @param searchs
	 * @return
	 */
	private Integer[] searchAllCols(String[] sentence, String... searchs) {
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

	/**
	 * SECTIONリストから指定した名前のProSecを取得する。ないときはnull。
	 * 
	 * @param secName
	 * @return
	 */
	private ProcSec searchSec(String secName) {
		for (ProcSec sec : secList) {
			if (secName.equals(sec.sectionName)) {
				return sec;
			}
		}
		return null;
	}

	/**
	 * ExecCmdを生成。次のCmdも追加し完了した状態とする。
	 *
	 * @param prev     呼び出し元Cmd。
	 * @param sentence 解析する行。複数命令文がある場合あり。
	 * @param localQue Perform戻り場所のスタック。
	 * @return
	 */
	private ExecCmd createCmd(ExecCmd prev, String[] sentence, Deque<String[]> localQue, ExecCmd endCmd) {
		String[] nextSentence = null;
		// 次の行の設定
		if ((searchExecAllIndex(sentence).size() > 1) && (!KEY_READ.equals(sentence[0]))
				&& (!KEY_EVALUATE.equals(sentence[0])) && (!KEY_IF.equals(sentence[0]))) {
			// 行に複数命令がある場合は２番め以降を次へ
			nextSentence = this.selectArray(sentence, searchExecAllIndex(sentence).get(1));
			sentence = this.selectArray(sentence, 0, searchExecAllIndex(sentence).get(1));
		} else {
			// recListの次の行
			int next_i = recList.indexOf(sentence);
			if ((next_i > -1) && (next_i < recList.size())) {
				if (next_i < recList.size() - 1) { // 最後の行はEXITのはずなのでここではスキップ。
					nextSentence = recList.get(next_i + 1);
					if ((searchExecAllIndex(nextSentence).size() > 1) && (next_i < recList.size() - 2)
							&& (!KEY_READ.equals(nextSentence[0])) && (!KEY_EVALUATE.equals(nextSentence[0]))
							&& (!KEY_IF.equals(nextSentence[0]))) {
						// 次の行が複数命令の場合はスタックに積む。（READ以外）
						localQue.push(recList.get(next_i + 2));
					}
				}
			} else if (sentence.length > 0 && KEY_PERFORM.equals(sentence[0]) && searchSec(sentence[1]) != null) {
				// 飛び先が存在するPERFORMはここでは次の行は空のまま。設定しない。
			} else {
				if (localQue.isEmpty()) {
					ExecCmd ret = new ExecCmd(prev, sentence);
					if (endCmd != null) {
						ret.addNextCmd(endCmd, "exit");
					}
					return ret;
				}
				// 発生しないはずなのでコメントアウト。
				nextSentence = localQue.pop();
			}
		}
		ExecCmd exec = new ExecCmd(prev, sentence);
		if (KEY_EXIT.equals(sentence[0])) {
			return doExit(exec, sentence, localQue, nextSentence, endCmd);
		}
		// PERFORMジャンプ
		if (KEY_PERFORM.equals(sentence[0])) {
			return doPerform(exec, sentence, localQue, nextSentence, endCmd);
		}
		// END_PERFORMの場合には遡ってPERFORM UNTILを検索
		if (KEY_END_PERFORM.equals(sentence[0])) {
			doEndPerform(exec);
		}
		exec.addNextCmd(createCmd(exec, nextSentence, localQue, endCmd), "all");
		return exec;
	}

	/**
	 * END-PERFORM コマンドには対応するPERFORM UNTIL へのペアを設定する。
	 * 
	 * @param exec
	 */
	private void doEndPerform(ExecCmd exec) {
		ExecCmd prev = exec.prevCmd;
		while (prev != null) {
			if (prev.execSentence.length > 1) {
				if (KEY_PERFORM.equals(prev.execSentence[0]) && KEY_UNTIL.equals(prev.execSentence[1])
						&& prev.getPair() == null) {
					exec.setPair(prev);
					prev.setPair(exec);
					return;
				}
			}
			prev = prev.prevCmd;
		}
	}

	/**
	 * PERFORM 処理
	 * 
	 * @param exec         処理するコマンド。
	 * @param sentence     このコマンドの実行文。
	 * @param locaQ        ジャンプ時のスタック。
	 * @param nextSentence 次の処理する実行文。
	 * @param endCmd       作成済みリストの展開で使用。展開された最後のコマンドの次のコマンド。呼ぶ魔炎に作成済であること。
	 * @return
	 */
	private ExecCmd doPerform(ExecCmd exec, String[] sentence, Deque<String[]> locaQ, String[] nextSentence,
			ExecCmd endCmd) {
		if (searchSec(sentence[1]) != null) {
			// PERFORNの次がSECTION 次行をスタックへ
			if (nextSentence != null) {
				locaQ.push(nextSentence);
			}
			exec.addNextCmd(createCmd(exec, searchSec(sentence[1]).defineSentence, locaQ, endCmd), "all");
		} else {
			exec.addNextCmd(createCmd(exec, nextSentence, locaQ, endCmd), "all");
		}
		return exec;
	}

	class BranchCmd {
		String[] orgSentence;
		List<BranchElm> branchList;

		class BranchElm {
			String cond;
			String[] sentence;

			BranchElm(String cond, String[] sentence) {
				this.cond = cond;
				this.sentence = sentence;
			}
		}

		BranchCmd(String[] org) {
			this.orgSentence = org;
			this.branchList = new ArrayList<BranchElm>();
		}

		void addBranchElm(String cond, String[] sentence) {
			this.branchList.add(new BranchElm(cond, sentence));
		}
	}

	private ExecCmd doExit(ExecCmd exec, String[] sentence, Deque<String[]> locaQ, String[] nextSentence,
			ExecCmd endCmd) {
		if ((sentence.length > 1) && (KEY_PROGRAM.equals(sentence[1]))) {
			// プログラム終了。最後のCmd。
		} else {
			if (locaQ.isEmpty()) {
				if (endCmd != null) {
					exec.addNextCmd(endCmd, "exit");
				}
			} else {
				String[] next = locaQ.pop();
				exec.addNextCmd(createCmd(exec, next, locaQ, endCmd), "all");
			}
			// 戻るときの次行はスタックから
//			if (endCmd != null) {
//				exec.addNextCmd(endCmd, "exit");
//			} else {
//				String[] next = locaQ.pop();
//				exec.addNextCmd(createCmd(exec, next, locaQ, endCmd), "all");
//			}
		}
		return exec;

	}

	/**
	 * 実行文がEXITの場合には[SECTION名]を返す。それ以外は""を返す。
	 * 
	 * @param sentence
	 * @return
	 */
	private String getSecNameByExit(String[] sentence) {
		if (KEY_EXIT.equals(sentence[0])) {
			for (ProcSec sec : this.secList) {
				if (sentence.equals(sec.exitSentence)) {
					return "[" + sec.sectionName + "]";
				}
			}
		}
		return "";
	}

	/**
	 * 命令（予約語）のすべての場所を返す。 複数の予約語がある場合にはすべての場所を返す。
	 * 
	 * @param sentence
	 * @return
	 */
	private List<Integer> searchExecAllIndex(String[] sentence) {
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

	public void logoutRecList() {
		for (String[] sentence : recList) {
			logger.info(String.join(" ", sentence));
		}
	}

	public void logoutSecTree() {
		for (ProcSec sec : secList) {
			String txt = "";
			if (sec.isRead || sec.isWrite) {
				txt = "[ ";
				if (sec.isRead()) {
					txt += KEY_READ + " ";
				}
				if (sec.isWrite()) {
					txt += KEY_WRITE + " ";
				}
				txt += "]";
			}
			logger.info("SECTION : " + sec.sectionName + txt);
			for (ProcSec called : sec.callSecList) {
				logger.info("\tcalled : " + called.sectionName);
			}
		}
	}

	/**
	 * 処理フロー出力。Graphvizファイル出力。
	 * 
	 * @param filePath ファイルパス。nullのときはINFOログへ出力。
	 * @throws IOException
	 */
	public void outputDataDot(String filePath) throws IOException {
		if (filePath != null) {
			File file = new File(filePath);
			dotFw = new FileWriter(file);
		}
		writeDotRec("strict digraph {");
		{
			writeDotRec(addDotNode(this.rootCmd, 1));
		}
		writeDotRec("}\n");
		if (dotFw != null) {
			dotFw.close();
		}
	}

	/**
	 * 処理フローのコマンドノードの追加。再帰的に自ノードの次ノードを作成して連結。
	 * 
	 * @param cmd    対象コマンド
	 * @param fw     出力ファイル。nullのときはINFOログに出力
	 * @param branch ブランチ番号。分岐を展開するときに加算されて同一コマンドを別ノードとして出力する。
	 * @return 後続が追加されたノードを返す。次ノードがないとき、分岐が始まったときにDOTスクリプトを返す。。
	 * @throws IOException
	 */
	private String addDotNode(ExecCmd cmd, int branch) throws IOException {
		// CMDの出力
		String execStr = cmd.execSentence[0];
		if (LONG_LABEL) {
			execStr = String.join(" ", cmd.execSentence);
			execStr = execStr.replaceAll("\"", "");
		}
		writeDotNode(cmd, branch);
		switch (cmd.nextList.size()) {
		case 0:
			return Integer.toString(cmd.hashCode()) + Integer.toString(branch);
		case 1:
			return Integer.toString(cmd.hashCode()) + Integer.toString(branch) + " -> "
					+ addDotNode(nextValid(cmd.nextList.get(0).nextCmd), branch);
		default:
			for (NextCmd next : cmd.nextList) {
				if (isNextExpand) {
					nestCounter++;
				}
				writeDotRelation(cmd, branch, next);
				writeDotRec(addDotNode(nextValid(next.nextCmd), nestCounter));
			}
			return Integer.toString(cmd.hashCode()) + Integer.toString(branch);
		}
	}

	/**
	 * DOT ノードの書き出し。
	 * 
	 * @param cmd
	 * @param branch
	 * @throws IOException
	 */
	private void writeDotNode(ExecCmd cmd, int branch) throws IOException {
		String execStr = cmd.execSentence[0];
		if (LONG_LABEL) {
			execStr = String.join(" ", cmd.execSentence);
			execStr = execStr.replaceAll("\"", "");
		}
		// getSecNameByExitにてExitのときにはSECTION名を付与する。
		writeDotRec(Integer.toString(cmd.hashCode()) + Integer.toString(branch) + " [label=\"" + execStr
				+ getSecNameByExit(cmd.execSentence) + " " + cmd.type + "\"];");
		// READ/WRITEのときはIOファイルを出力。
		if (KEY_READ.equals(cmd.execSentence[0]) || KEY_WRITE.equals(cmd.execSentence[0])) {
			// ファイルはボックスで出力
			writeDotRec("io" + Integer.toString(cmd.hashCode()) + Integer.toString(branch) + " [label=\""
					+ cmd.execSentence[1] + "\", shape = box ];");
			if (KEY_READ.equals(cmd.execSentence[0])) {
				// ファイルー＞コマンド
				writeDotRec("io" + Integer.toString(cmd.hashCode()) + Integer.toString(branch) + " -> "
						+ Integer.toString(cmd.hashCode()) + Integer.toString(branch));
			} else {
				// コマンドー＞ファイル
				writeDotRec(Integer.toString(cmd.hashCode()) + Integer.toString(branch) + " -> " + "io"
						+ Integer.toString(cmd.hashCode()) + Integer.toString(branch));
			}
		}
		// pairノードがある場合には接続線を出力。
		if ((cmd.getPair() != null) && (KEY_END_PERFORM.equals(cmd.execSentence[0]))) {
			writeDotRec(Integer.toString(cmd.hashCode()) + Integer.toString(branch) + " -> "
					+ Integer.toString(cmd.getPair().hashCode()) + Integer.toString(branch));
		}
	}

	/**
	 * DOT ラインの書き出し。
	 * 
	 * @param cmd
	 * @param branch
	 * @param next
	 * @throws IOException
	 */
	private void writeDotRelation(ExecCmd cmd, int branch, NextCmd next) throws IOException {
		writeDotRec(cmd.hashCode() + Integer.toString(branch) + " -> " + nextValid(next.nextCmd).hashCode()
				+ Integer.toString(nestCounter) + " [label=\"" + next.condition + "\"]");

	}

	private ExecCmd nextValid(ExecCmd cmd) {
		ExecCmd next = cmd;// .nextList.get(0).nextCmd;
		while (true) {
			for (CmdType fil : DOT_CMD_FILTER) {
				if (next.type == fil)
					return next;
			}
			if (next.nextList.size() < 1)
				return next;
			next = next.nextList.get(0).nextCmd;
		}
	}

	/**
	 * DOT Writerへの書き出し。
	 * 
	 * @param msg
	 * @throws IOException
	 */
	private void writeDotRec(String msg) throws IOException {
		if (dotFw == null) {
			logger.info(msg);
		} else {
			dotFw.write(msg + "\n");
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
			} else {
				fw.write("*** branch " + cmd.nextList.size() + " ***" + "\n");

			}
			for (NextCmd next : cmd.nextList) {
				logoutCmdTree(next.nextCmd, prefix + "\t" + next.condition + "\t", fw);
//				logoutCmdTree(next.nextCmd, prefix + next.condition + "\t", fw);
			}
		}
	}

	class ExecCmd {
		ExecCmd prevCmd;
		List<NextCmd> nextList;
		String[] execSentence;
		CmdType type;
		private ExecCmd pair;

		ExecCmd(ExecCmd prev, String[] sentence) {
			this.prevCmd = prev;
			this.nextList = new ArrayList<NextCmd>();
			this.execSentence = sentence;
			this.type = convType(sentence);
		}

		public String getSentenceStr() {
			return String.join(" ", execSentence);
		}

		public ExecCmd getPair() {
			return this.pair;
		}

		public void setPair(ExecCmd pair) {
			this.pair = pair;
		}

		ExecCmd(ExecCmd prev, String[] sentence, CmdType type) {
			this(prev, sentence);
			this.type = type;
		}

		CmdType convType(String[] sentence) {
			for (String key : BRANCH_WORD_LIST) {
				if (sentence[0].equals(key))
					return CmdType.BRANCH;
			}
			for (String key : EXEC_WORD_LIST) {
				if (sentence[0].equals(key))
					return CmdType.EXEC;
			}
			if (sentence.length > 1) {
				if (Const.KEY_SECTION.equals(sentence[1])) {
					return CmdType.DEFINE;
				}
			}
			return CmdType.LABEL;
		}

		void addNextCmd(ExecCmd nextCmd, String cond) {
			this.nextList.add(new NextCmd(nextCmd, cond));
		}

		void clearNextCmd() {
			this.nextList.clear();
		}
	}

	enum CmdType {
		EXEC, BRANCH, DEFINE, SECTION, LABEL, OTHER
	}

	class NextCmd {
		ExecCmd nextCmd;
		String condition;

		NextCmd(ExecCmd nextCmd, String condition) {
			this.nextCmd = nextCmd;
			this.condition = condition;
		}
	}

	/**
	 * PROCEDURE DIVISION 内の SECTION を表す。
	 * 
	 * @author nakazawasugio
	 *
	 */
	class ProcSec {
		String sectionName;
		String[] defineSentence;
		String[] exitSentence;
		boolean isRead = false;
		boolean isWrite = false;

		List<ProcSec> callSecList;

		ProcSec(String[] defineSentence, String[] exitSentence) {
			this.sectionName = defineSentence[0];
			this.defineSentence = defineSentence;
			this.exitSentence = exitSentence;
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
