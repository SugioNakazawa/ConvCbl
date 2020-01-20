package com.hoge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CblSource {
	static Logger logger = LoggerFactory.getLogger(CblSource.class.getName());

	static public List<String[]> read(String fileName) throws IOException {

		List<String[]> recList = new ArrayList<String[]>();
		Path path = Paths.get(fileName);
		List<String> recListAll = Files.readAllLines(path);
		String recBuff = "";
		for (String rec : recListAll) {
			// 全角スペースを半角スペースへ
			rec = rec.replaceAll("　", " ");
			// コメント行以外の8カラム以降（A領域）を対象
			if (rec.length() > 7 && !"*".equals(rec.subSequence(6, 7)) && !"/".equals(rec.subSequence(6, 7))) {
				if (rec.endsWith(".")) {
					String target = "";
					if (recBuff.length() > 0) {
						target = recBuff + " " + rec.substring(7).trim();
						recBuff = "";
					} else {
						target = rec.substring(7).trim();
					}
//					logger.debug(target);
					String[] cols = target.split(" ");
					if (cols.length > 0) {
						recList.add(cols);
					}
				} else {
					// 次の行へ継続
					recBuff = rec.substring(7).trim();
				}
			}
		}
		return recList;
	}
}
