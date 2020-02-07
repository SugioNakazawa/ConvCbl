package com.hoge;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CblProgram {
	static Logger logger = LoggerFactory.getLogger(CblProgram.class.getName());

	private List<String[]> recList;
	/** COBOLソース・ファイル **/
	private Path sourceFile;
	/** プログラムID（ソースファイルの拡張子除外） **/
	private String programId;
	IdentificationDiv idDiv;
	EnvironmentDiv envDiv;
	DataDiv dataDiv;
	ProcedureDiv procDiv;

	public CblProgram(Path sourceFile) {
		this.sourceFile = sourceFile;
		recList = new ArrayList<String[]>();
		idDiv = new IdentificationDiv();
		envDiv = new EnvironmentDiv();
		dataDiv = new DataDiv(sourceFile);
		procDiv = new ProcedureDiv();
		// 拡張子外し
		String fileName = sourceFile.getFileName().toString();
		int point = fileName.lastIndexOf(".");
		if (point != -1) {
			programId = fileName.substring(0, point);
		} else {
			programId = fileName;
		}
	}

	@SuppressWarnings("unused")
	private CblProgram() {
	}

//	public String getFileName() {
//		return this.fileName;
//	}
//
	public void read() throws IOException {
		CblSourceReader reader = new CblSourceReader(sourceFile);
		recList = reader.read();
		logger.info("read " + sourceFile.getFileName() + " " + recList.size() + " lines.");
	}

	void analyze() {
		separate();
		this.idDiv.analyze();
		this.dataDiv.analyze();
		this.procDiv.analyze();
	}

	/**
	 * 実行行のみを抽出しDIVISIONの開始行、終了行を求める。
	 */
	private void separate() {
		BaseDiv saveDiv = null;
		for (String[] cols : recList) {
			if (cols.length > 0) {
				if (cols.length > 1) {
					// check DIVISION
					if ("DIVISION".equals(cols[1])) {
						if (Const.KEY_IDENTIFICATION.equals(cols[0])) {
							saveDiv = idDiv;
						} else if (Const.KEY_ENVIRONMENT.equals(cols[0])) {
							saveDiv = envDiv;
						} else if (Const.KEY_DATA.equals(cols[0])) {
							saveDiv = dataDiv;
						} else if (Const.KEY_PROCEDURE.equals(cols[0])) {
							saveDiv = procDiv;
						}
					}
				}
				saveDiv.addRec(cols);
			}
		}
	}

	public void logout() {
		logger.info("fileName : " + sourceFile.toAbsolutePath().toString());

		logger.info(idDiv.getStat(Const.KEY_IDENTIFICATION + " " + Const.KEY_DIVISION));
		logger.info(envDiv.getStat(Const.KEY_ENVIRONMENT + " " + Const.KEY_DIVISION));
		logger.info(dataDiv.getStat(Const.KEY_DATA + " " + Const.KEY_DIVISION));
		logger.info(procDiv.getStat(Const.KEY_PROCEDURE + " " + Const.KEY_DIVISION));

		logger.info("valid " + recList.size() + " lines\n");
	}

	/**
	 * 
	 * @param outDir DMDL出力ディレクトリ。
	 * @throws IOException
	 */
	public void createDmdl(Path outDir) throws IOException {
		this.dataDiv.createDmdl(Paths.get(outDir.toString() + "/" + programId + ".dmdl"));
	}

	public void outputDataDot(Path outDir,boolean forkedMerge,boolean returnArrow) throws IOException {
		procDiv.setForkedMerge(forkedMerge);
		procDiv.setReturnArrow(returnArrow);
		procDiv.outputDataDot(Paths.get(outDir.toString() + "/" + programId + ".dot"));
	}
}
