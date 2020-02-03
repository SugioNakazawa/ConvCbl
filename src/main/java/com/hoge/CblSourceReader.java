package com.hoge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * COBOLソースを読み込み。
 * 
 * @author nakazawasugio
 *
 */
public class CblSourceReader {
	static Logger logger = LoggerFactory.getLogger(CblSourceReader.class.getName());

	static int[] COMMENT_AREA = { 6, 7 }; // ７カラム目
	static int[] A_AREA = { 7, 11 }; // ８〜１１カラム目
	static int[] B_AREA = { 11, 73 }; // １２〜７３カラム目

	/** COBOLソースファイルパス **/
	private String fileName;

	/**
	 * 
	 * @param fileName COBOLソースファイルパス
	 */
	public CblSourceReader(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * COBOLソースを読み込み実行文リストを返す。<BR>
	 * A領域（８カラム目以降）を実行分とする。 <BR>
	 * 継続行は１分とする。 <BR>
	 * PROCEDURE DIVISIONのみ。ラベルは１行とする。A領域から開始している。<BR>
	 * 文末のピリオドは外す。単語検索ではピリオドを無視できるように。
	 * 
	 * @param fileName ソースパス。
	 * @return 実行文のリスト。
	 * @throws IOException
	 */
	public List<String[]> read() throws IOException {

		List<String[]> recList = new ArrayList<String[]>();
		Path path = Paths.get(fileName);
		List<String> recListAll = Files.readAllLines(path);
		String recBuff = "";
		boolean inProcedureDiv = false;
		for (String rec : recListAll) {
			// 全角スペースを半角スペースへ
			rec = rec.replaceAll("　", " ").trim();
			// コメント行以外の8カラム以降（A領域）を対象
			if (rec.length() > COMMENT_AREA[1] && !"*".equals(rec.subSequence(COMMENT_AREA[0], COMMENT_AREA[1]))
					&& !"/".equals(rec.subSequence(COMMENT_AREA[0], COMMENT_AREA[1]))
					&& !"-".equals(rec.subSequence(COMMENT_AREA[0], COMMENT_AREA[1]))) {
				if (rec.endsWith(".")) {
					// PROCEDURE DIVISION内の判定
					if ((rec.length() > A_AREA[0] + Const.KEY_PROCEDURE.length() + Const.KEY_DIVISION.length() + 1)
							&& Const.KEY_PROCEDURE.equals(rec.substring(A_AREA[0]).split(" +")[0])) {
						inProcedureDiv = true;
					}
					// A領域から始まっている場合は宣言であるので前の行の継続にはしない。
					if ((inProcedureDiv) && (recBuff.length() > 0) && (rec.length() > A_AREA[1])
							&& (!" ".equals(rec.substring(A_AREA[0], A_AREA[0] + 1)))) {
						recList.add(recBuff.trim().split(" +"));
						recBuff = "";
					}
					// 最後のピリオドは外す。
					while (rec.endsWith(".")) {
						rec = rec.substring(0, rec.length() - 1);
					}
					String target = "";
					if (recBuff.length() > 0) {
						target = recBuff.trim() + " " + rec.substring(COMMENT_AREA[1]).trim();
					} else {
						target = rec.substring(COMMENT_AREA[1]).trim();
					}
					String[] cols = target.split(" +");
					if (cols.length > 0) {
						// １実行分としてリストに追加。
						recList.add(cols);
					}
					recBuff = "";
				} else {
					// 次の行へ継続
					recBuff += " " + rec.substring(7);
					recBuff.trim();
				}
			}
		}
		return recList;
	}
}
