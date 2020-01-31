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
 * @author nakazawasugio
 * 改善予定：以下のような場合にCLOSE文が１つにならない。
 * 030800 CLOSE IN01-FILE
 * 030900 OT01-FILE.
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
	private static final String KEY_MOVE = "MOVE";
	private static final String KEY_PERFORM = "PERFORM";
	private static final String KEY_WRITE = "WRITE";
	/** 命令単語の一覧 **/
	private static String[] EXEC_WORD_LIST = { KEY_COMPUTE, KEY_EVALUATE, KEY_IF, KEY_MOVE, KEY_PERFORM, KEY_READ,
			KEY_WRITE };
	private static final String KEY_END_EVALUATE = "END-EVALUATE";
	private static final String KEY_END_IF = "END-IF";
	private static final String KEY_END_READ = "END-READ";
//				, KEY_END_EVALUATE, KEY_END_IF };
	// 補助
	private static final String KEY_AT = "AT";
	private static final String KEY_END = "END";
	private static final String KEY_END_PERFORM = "END-PERFORM";
	private static final String KEY_EXIT = "EXIT";
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

	private static final String[] KEY_CONTINUE = { "continue" };

	private static final boolean isExpand = false;
	/** DOT作成時にネストするたびに加算し、条件違いのノードを別ノードとする。 **/
	private int nestCounter = 1;
	/** DOT作成時に分岐を展開=true、合流=falseするかを指定する。 **/
	private boolean isNextExpand = false;;

	List<ProcSec> secList;
	/** CmdTreeのルート **/
	ExecCmd rootCmd;

	public ProcedureDiv() {
		super();
		secList = new ArrayList<ProcSec>();
	}

	public void analyze() {
		// SECTION分け
		for (String[] cols : recList) {
			if ((cols.length == 2) && (Const.KEY_SECTION.equals(cols[1]))) {
				secList.add(new ProcSec(cols));
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
		// コマンド作成
		{
			rootCmd = new ExecCmd(null, recList.get(0), CmdType.LABEL);
			Deque<String[]> localQue = new ArrayDeque<>();
			// コマンドリストを作成。分岐なし
			rootCmd.addNextCmd(createCmd(rootCmd, recList.get(1), localQue, null), "start");
			// 分岐文を展開
			expandBranch(rootCmd);
		}
	}

	/**
	 * 分岐拡張。
	 * 
	 * @param cmd
	 */
	private void expandBranch(ExecCmd cmd) {
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
			ExecCmd b1 = createCmd(cmd, elm.sentence, new ArrayDeque<>(), endCmd);
			cmd.addNextCmd(b1, elm.cond);
		}
		// 分岐文を先頭のみに変更
		shrinkBranchSentence(cmd);
		return cmd;
	}

	private void shrinkBranchSentence(ExecCmd cmd) {
		if (KEY_IF.equals(cmd.execSentence[0])) {
			if (searchCol(cmd.execSentence, KEY_THEN) > 0) {
				cmd.execSentence = selectArray(cmd.execSentence, 0, searchCol(cmd.execSentence, KEY_THEN));
			} else {
				cmd.execSentence = selectArray(cmd.execSentence, 0, searchExecIndexList(cmd.execSentence).get(1));
			}
		} else if (KEY_EVALUATE.equals(cmd.execSentence[0])) {
			cmd.execSentence = selectArray(cmd.execSentence, 0, searchCol(cmd.execSentence, KEY_WHEN));
		} else if (KEY_READ.equals(cmd.execSentence[0])) {
			cmd.execSentence = selectArray(cmd.execSentence, 0, searchCol(cmd.execSentence, KEY_AT));
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
		int then_i = searchCol(sentence, KEY_THEN);
		int else_i = searchCol(sentence, KEY_ELSE);
		int endIf_i = searchCol(sentence, KEY_END_IF);
		int condEnd = 0;
		int thenStart = 0;
		int thenEnd = 0;
		int elseStart = 0;
		int elseEnd = 0;
		if (then_i < 0) {
			if (searchExecIndexList(sentence).size() < 1) {
				// エラー
				String msg = "IF文内に実行命令がありません。";
				throw new RuntimeException(msg);
			} else {
				// 最初はIFなので次を取得
				thenStart = searchExecIndexList(sentence).get(1);
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
		Integer[] when_i = searchCols(sentence, KEY_WHEN);
		// 最終がEND-EVALUATEのときはカットしておく。
		if (searchCol(sentence, KEY_END_EVALUATE) == sentence.length - 1) {
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
			if (searchExecIndexList(target).size() > 0) {
				cond1.add(String.join(" ", selectArray(target, 0, searchExecIndexList(target).get(0))));
				next = String.join(" ", selectArray(target, searchExecIndexList(target).get(0)));
			} else {
				cond1.add(String.join(" ", target));
				next = "";
			}
			// next からは次のWHENと同じとなる。
			if (next != "") {
				// 分岐
				ret.addBranchElm(cond0 + cond1, next.split(" "));
//				logger.debug("cond0=" + cond0 + " cond1=" + cond1);
//				logger.debug("sentence=" + next);
				cond1.clear();
				;
			}
		}
		return ret;
	}

	BranchCmd expandRead(String[] sentence) {
		BranchCmd ret = new BranchCmd(sentence);
		int atEnd_i = searchCol(sentence, KEY_AT, KEY_END);
		int notAtEnd_i = searchCol(sentence, KEY_NOT, KEY_AT, KEY_END);
		int endRead_i = searchCol(sentence, KEY_END_READ);
		int condEnd = 0;
		int atEndStart = 0;
		int atEndEnd = 0;
		int notAtEndStart = 0;
		int notAtEndEnd = 0;
		if (atEnd_i < 0) {
			if (searchExecIndexList(sentence).size() < 1) {
				// エラー
				String msg = "IF文内に実行命令がありません。";
				throw new RuntimeException(msg);
			} else {
				// 最初はREADなので次を取得
				atEndStart = searchExecIndexList(sentence).get(1);
				if (endRead_i < 0) {
					atEndEnd = sentence.length;
				} else {
					atEndEnd = endRead_i;
				}
			}
			condEnd = atEndStart - 1;
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
//		ret.logout();
		return ret;
	}

	private ExecCmd createCmdInExpand(ExecCmd prev, String[] sentence, ExecCmd lastExec, Deque<String[]> localQue) {
		// 複数コマンドの判定
		String[] execSentence = null;
		String[] nextSentence = null;
		if (searchExecIndexList(sentence).size() > 1) {
			execSentence = selectArray(sentence, 0, searchExecIndexList(sentence).get(1) - 1);
			nextSentence = selectArray(sentence, searchExecIndexList(sentence).get(1), sentence.length);
		} else if (searchExecIndexList(sentence).size() == 1) {
			execSentence = sentence;
		}
		// PERFORMチェック
		if (execSentence.length > 1 && KEY_PERFORM.equals(execSentence[0]) && searchSec(execSentence[1]) != null) {
			localQue.push(nextSentence);

		}
		ExecCmd exec = new ExecCmd(prev, execSentence);
		exec.addNextCmd(createCmdInExpand(exec, nextSentence, lastExec, localQue), "");
		return exec;
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

	/**
	 * ExecCmdを生成。次のCmdも追加し完了した状態とする。
	 *
	 * @param prev     呼び出し元Cmd。
	 * @param sentence 解析する行。複数命令文がある場合あり。
	 * @param localQue Perform戻り場所のスタック。
	 * @return
	 */
	private ExecCmd createCmd(ExecCmd prev, String[] sentence, Deque<String[]> localQue, ExecCmd endCmd) {
		// + localQue.size());
		String[] nextSentence = null;
		// 次の行の設定
		// getExecIndex(sentence).size());
		if ((searchExecIndexList(sentence).size() > 1) && (!KEY_READ.equals(sentence[0]))
				&& (!KEY_EVALUATE.equals(sentence[0])) && (!KEY_IF.equals(sentence[0]))) {
			// 行に複数命令がある場合は２番め以降を次へ
			nextSentence = this.selectArray(sentence, searchExecIndexList(sentence).get(1));
			sentence = this.selectArray(sentence, 0, searchExecIndexList(sentence).get(1));
		} else {
			// recListの次の行
			int next_i = recList.indexOf(sentence);
			if ((next_i > -1) && (next_i < recList.size())) {
				if (next_i < recList.size() - 1) { // 最後の行はEXITのはずなのでここではスキップ。
					nextSentence = recList.get(next_i + 1);
					if ((searchExecIndexList(nextSentence).size() > 1) && (next_i < recList.size() - 2)
							&& (!KEY_READ.equals(nextSentence[0])) && (!KEY_EVALUATE.equals(nextSentence[0]))
							&& (!KEY_IF.equals(nextSentence[0]))) {
						// 次の行が複数命令の場合はスタックに積む。（READ以外）
						localQue.push(recList.get(next_i + 2));
					}
				}
			} else if (sentence.length > 1 && KEY_PERFORM.equals(sentence[0]) && searchSec(sentence[1]) != null) {
				// 飛び先が存在するPERFORM
				nextSentence = searchSec(sentence[1]).cols;
			} else {
				if (localQue.isEmpty()) {
					ExecCmd ret = new ExecCmd(prev, sentence);
					if (endCmd != null) {
						ret.addNextCmd(endCmd, "exit");
					}
					return ret;
				}
				nextSentence = localQue.pop();
			}
		}
		// + localQue.size());
		// " que:" + localQue.size());
		ExecCmd exec = new ExecCmd(prev, sentence);
		if (KEY_EXIT.equals(sentence[0])) {
			return doExit(exec, sentence, localQue, nextSentence, endCmd);
		}
		// PERFORMジャンプ
		if (KEY_PERFORM.equals(sentence[0])) {
			return doPerform(exec, sentence, localQue, nextSentence, endCmd);
		}
		exec.addNextCmd(createCmd(exec, nextSentence, localQue, endCmd), "all");
		return exec;
	}

	private ExecCmd doPerform(ExecCmd exec, String[] sentence, Deque<String[]> locaQ, String[] nextSentence,
			ExecCmd endCmd) {
		logger.debug("PERFORM : "+String.join(" ", sentence));
		if (searchSec(sentence[1]) != null) {
			// PERFORNの次がSECTION 次行をスタックへ
			locaQ.push(nextSentence);
			exec.addNextCmd(createCmd(exec, searchSec(sentence[1]).cols, locaQ, endCmd), "all");
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

		public void logout() {
			logger.debug("orgSentence = " + String.join(" ", orgSentence));
			for (BranchElm elm : this.branchList) {
				logger.debug("cond = " + elm.cond);
				logger.debug("sentence = " + String.join(" ", elm.sentence));
			}
		}

	}

	private ExecCmd doEvaluate(ExecCmd exec, String[] sentence, Deque<String[]> locaQ, String[] nextSentence,
			ExecCmd endCmd) {
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
		// EVALUATEの次の文を登録。
		locaQ.push(nextSentence);
		// END-EVALUTEを登録。
		String[] endEva = { "END-EVALUATE" };
		locaQ.push(endEva);
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
				exec.addNextCmd(createCmd(exec, next.split(" "), new ArrayDeque<>(locaQ), endCmd),
						cond0 + " = " + cond1);
				cond1.clear();
				;
			}
		}
		return exec;
	}

	private ExecCmd doRead(ExecCmd exec, String[] sentence, Deque<String[]> locaQ, String[] nextSentence,
			ExecCmd lastCmd) {
//		if (!DEVIDE_READ) {
//			exec.addNextCmd(createCmd(exec, nextSentence, locaQ), "all");
//			return exec;
//		}
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
			ExecCmd endCmd = new ExecCmd(exec, subSentence);
			exec.addNextCmd(endCmd, KEY_AT + " " + KEY_END);
			endCmd.addNextCmd(createCmd(endCmd, nextSentence, new ArrayDeque<>(locaQ), lastCmd),
					KEY_AT + " " + KEY_END);
		}
		{
			// NOT AT END
			String[] subSentence = new String[endRead_i - notEnd_i - 3];
			for (int i = 0; i < subSentence.length; i++) {
				subSentence[i] = sentence[i + notEnd_i + 3];
			}
			ExecCmd endCmd = new ExecCmd(exec, subSentence);
			exec.addNextCmd(endCmd, KEY_NOT + " " + KEY_AT + " " + KEY_END);
			endCmd.addNextCmd(createCmd(endCmd, nextSentence, new ArrayDeque<>(locaQ), lastCmd),
					KEY_NOT + " " + KEY_AT + " " + KEY_END);
		}
		return exec;
	}

	private ExecCmd doExit(ExecCmd exec, String[] sentence, Deque<String[]> locaQ, String[] nextSentence,
			ExecCmd endCmd) {
		if ((sentence.length > 1) && (KEY_PROGRAM.equals(sentence[1]))) {
			// プログラム終了。最後のCmd。
		} else {
			// 戻るときの次行はスタックから
			if (endCmd != null) {
				exec.addNextCmd(endCmd, "exit");
			} else {
				String[] next = locaQ.pop();
				exec.addNextCmd(createCmd(exec, next, locaQ, endCmd), "all");
			}
		}
		return exec;

	}

	/**
	 * 命令予約後の場所を返す。
	 *
	 * @param sentence
	 * @return
	 */
	private List<Integer> searchExecIndexList(String[] sentence) {
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
	 * 処理フロー出力。Graphvizファイル出力。
	 * 
	 * @param filePath ファイルパス。nullのときはINFOログへ出力。
	 * @throws IOException
	 */
	public void outputDataDot(String filePath) throws IOException {
		FileWriter fw = null;
		if (filePath != null) {
			File file = new File(filePath);
			fw = new FileWriter(file);
		}
		writeDot("strict digraph {", fw);
		{
			writeDot(addDotNode(this.rootCmd, fw, 1), fw);
		}
		writeDot("}\n", fw);
		if (fw != null) {
			fw.close();
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
	private String addDotNode(ExecCmd cmd, FileWriter fw, int branch) throws IOException {
		// CMDの出力
		String execStr = cmd.execSentence[0];
		if (LONG_LABEL) {
			execStr = String.join(" ", cmd.execSentence);
			execStr = execStr.replaceAll("\"", "");
		}
		writeDot(Integer.toString(cmd.hashCode()) + Integer.toString(branch) + " [label=\"" + execStr + " " + cmd.type
				+ "\"];", fw);
		// IOファイルを出力。
		if (KEY_READ.equals(cmd.execSentence[0]) || KEY_WRITE.equals(cmd.execSentence[0])) {
			writeDot("io" + Integer.toString(cmd.hashCode()) + Integer.toString(branch) + " [label=\""
					+ cmd.execSentence[1] + "\", shape = box ];", fw);
			if (KEY_READ.equals(cmd.execSentence[0])) {
				writeDot("io" + Integer.toString(cmd.hashCode()) + Integer.toString(branch) + " -> "
						+ Integer.toString(cmd.hashCode()) + Integer.toString(branch), fw);
			} else {
				writeDot(Integer.toString(cmd.hashCode()) + Integer.toString(branch) + " -> " + "io"
						+ Integer.toString(cmd.hashCode()) + Integer.toString(branch), fw);
			}
		}
		switch (cmd.nextList.size()) {
		case 0:
			return Integer.toString(cmd.hashCode()) + Integer.toString(branch);
		case 1:
			return Integer.toString(cmd.hashCode()) + Integer.toString(branch) + " -> "
					+ addDotNode(nextValid(cmd.nextList.get(0).nextCmd), fw, branch);
		default:
			for (NextCmd next : cmd.nextList) {
				if (isNextExpand) {
					nestCounter++;
				}
				writeDot(cmd.hashCode() + Integer.toString(branch) + " -> " + nextValid(next.nextCmd).hashCode()
						+ Integer.toString(nestCounter) + " [label=\"" + next.condition + "\"]", fw);
				writeDot(addDotNode(nextValid(next.nextCmd), fw, nestCounter), fw);
			}
			return Integer.toString(cmd.hashCode()) + Integer.toString(branch);
		}
	}

	private ExecCmd nextValid(ExecCmd cmd) {
		if (true)
			return cmd; // 全部表示
		CmdType[] fils = { CmdType.BRANCH, CmdType.EXEC, CmdType.DEFINE };
		ExecCmd next = cmd.nextList.get(0).nextCmd;
		while (true) {
			for (CmdType fil : fils) {
				if (next.type == fil)
					return next;
			}
			if (next.nextList.size() < 1)
				return next;
			next = next.nextList.get(0).nextCmd;
		}
	}

	private void writeDot(String msg, FileWriter fw) throws IOException {
		if (fw == null) {
			logger.info(msg);
		} else {
			fw.write(msg + "\n");
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

		ExecCmd(ExecCmd prev, String[] sentence) {
			this.prevCmd = prev;
			this.nextList = new ArrayList<NextCmd>();
			this.execSentence = sentence;
			this.type = convType(sentence);
		}

		public void setPrevCmd(ExecCmd b1) {
			this.prevCmd = b1;
		}

		ExecCmd(ExecCmd prev, String[] sentence, CmdType type) {
			this(prev, sentence);
			this.type = type;
		}

		private CmdType convType(String[] sentence) {
			if ((sentence == null) || (sentence.length < 1)) {
				return CmdType.OTHER;
			}
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

		public void setSentence(String[] sentence) {
			this.execSentence = sentence;
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
