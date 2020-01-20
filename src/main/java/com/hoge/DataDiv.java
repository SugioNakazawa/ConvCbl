package com.hoge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataDiv extends BaseDiv {
	static Logger logger = LoggerFactory.getLogger(DataDiv.class.getName());
	private String fileName;

	public DataDiv(String fileName) {
		super();
		this.fileName = fileName;
	}

	/**
	 * 
	 * @throws IOException COPY句が存在しない。
	 */
	public void analyze() {
		List<String[]> tmp = new ArrayList<String[]>();
		for (String[] rec : recList) {
			if ("COPY".equals(rec[0])) {
				tmp.addAll(doCopy(rec[1]));
			} else {
				tmp.add(rec);
			}
		}
		recList = tmp;
	}

	/**
	 * COPY句処理。
	 * 
	 * @param string
	 * @throws IOException 
	 */
	private List<String[]> doCopy(String copyName) {
		while (copyName.endsWith(".")) {
			copyName = copyName.substring(0, copyName.length() - 1);
		}
		File dir = new File(fileName);
		try {
			return CblSource.read(dir.getParent() + "/" + copyName);
		} catch (IOException e) {
			// COPY句が見つからない。
//			e.printStackTrace();
			String msg = "コピー句 "+copyName+" がありません。";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
	}

	public String getStat(String title) {
		StringBuilder sb = new StringBuilder(super.getStat(title));
//		for (String[] rec : recList) {
//			sb.append(String.join(" ", rec) + "\n");
//		}
		return sb.toString();
	}
}
