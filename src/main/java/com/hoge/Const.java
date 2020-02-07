package com.hoge;

/**
 * 共通変数＆メッセージ。
 * 
 * @author nakazawasugio
 *
 */
public class Const {
	// 定数
	static final String KEY_IDENTIFICATION = "IDENTIFICATION";
	static final String KEY_ENVIRONMENT = "ENVIRONMENT";
	static final String KEY_DATA = "DATA";
	static final String KEY_PROCEDURE = "PROCEDURE";
	static final String KEY_DIVISION = "DIVISION";
	static final String KEY_SECTION = "SECTION";
	// メッセージ
	static final String MSG_NO_FILE_PARAM = "入力ファイルが指定されていません。";
	static final String MSG_NO_FILE = "指定ファイル {0} が存在しません。";
	static final String MSG_NO_DIR = "指定ディレクトリ {0} が存在しません。";
	static final String MSG_NOT_FOUND_PAIR_PERFORM_UNTIL = "END-PERFORM の前に対応するPERFORM UNTILがありません。";
	// メッセージ
	static final String MSG_NO_SUPPORT = "COPY句ではREPLACING以外はサポートしていません。";
	static final String MSG_NOT_FOUND_COPY = "コピー句 {0} がありません。";
}
