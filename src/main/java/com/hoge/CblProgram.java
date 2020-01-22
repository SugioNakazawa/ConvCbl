package com.hoge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CblProgram {
	static Logger logger = LoggerFactory.getLogger(CblProgram.class.getName());

	private List<String[]> recList;

	private String fileName;
	IdentificationDiv idDiv;
	EnvironmentDiv envDiv;
	DataDiv dataDiv;
	ProcedureDiv procDiv;

	public CblProgram(String fileName) {
		this.fileName = fileName;
		recList = new ArrayList<String[]>();
		idDiv = new IdentificationDiv();
		envDiv = new EnvironmentDiv();
		dataDiv = new DataDiv(fileName);
		procDiv = new ProcedureDiv();
	}

	private CblProgram() {
	}

	public String getFileName() {
		return this.fileName;
	}

	public void read() throws IOException {
		recList = CblSource.read(fileName);
		logger.debug("read " + fileName + " " + recList.size() + " lines.");
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
		logger.info("fileName : " + fileName);

		logger.info(idDiv.getStat(Const.KEY_IDENTIFICATION + " " + Const.KEY_DIVISION));
		logger.info(envDiv.getStat(Const.KEY_ENVIRONMENT + " " + Const.KEY_DIVISION));
		logger.info(dataDiv.getStat(Const.KEY_DATA + " " + Const.KEY_DIVISION));
		logger.info(procDiv.getStat(Const.KEY_PROCEDURE + " " + Const.KEY_DIVISION));

		logger.info("valid " + recList.size() + " lines\n");
	}
}
