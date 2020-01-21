package com.hoge;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AsakusaDMDL
 * 
 * @author nakazawasugio
 *
 */
public class AsakusaDmdl {
	static Logger logger = LoggerFactory.getLogger(AsakusaDmdl.class.getName());
	/*
	 * "IF05-07 月間電力量（低圧：HC月間使用量）"
	 * 
	 * @namespace(value = file)
	 * 
	 * @directio.csv(date = "yyyyMMdd")
	 */
	// 定数
	private static final String FILE_DESC = "\"{0})\"\n";
	private static final String NAME_SPACE_REC = "@namespace(value = {0})\n";
	private static final String DIRECTIO_CSV = "@directio.csv(charset = \"{0}\")\n";
	private static final String SP4 = "    ";

	/** データモデル名称 **/
	private String fileDesc;
	private String namespaceValue;
	private String charset;
	private String name;
	List<AsakusaCol> colList;

	//	TODO 削除予定。テストケースへ。
	static public void main(String[] args) throws IOException {
		AsakusaDmdl obj = new AsakusaDmdl("sample", "file", "UTF-8");
		obj.setFileDesc("説明");
		obj.addCol("column_status", "TEXT", "マスター更新区分");
		obj.addCol("column_name", "TEXT", "名称");

		obj.outputDmdl("sample.dmdl");
	}

	public AsakusaDmdl(String name, String namespaceValue, String charset) {
		this.name = name;
		this.namespaceValue = namespaceValue;
		this.charset = charset;
		this.colList = new ArrayList<AsakusaCol>();
	}

	public void addCol(String name, String type, String desc) {
		this.colList.add((new AsakusaCol(name, type, desc)));
	}

	public void outputDmdl(String fileName) throws IOException {
		File file = new File(fileName);
		FileWriter fw = new FileWriter(file);

		if (namespaceValue != null) {
			fw.write(MessageFormat.format(FILE_DESC, fileDesc));
		}
		fw.write(MessageFormat.format(NAME_SPACE_REC, namespaceValue));
		fw.write(MessageFormat.format(DIRECTIO_CSV, charset));
		fw.write(name + " = {\n"); // name

		for (AsakusaCol col : colList) {
			fw.write(SP4 + "\"" + col.desc + "\"\n");
			fw.write(SP4 + col.name + " : " + col.type + ";\n\n");
		}

		fw.write("}\n"); // end-name
		fw.close();
		logger.info("created dmdl " + file.getAbsolutePath());
	}

	public String getFileDesc() {
		return fileDesc;
	}

	public void setFileDesc(String fileDesc) {
		this.fileDesc = fileDesc;
	}

	public String getNamespaceValue() {
		return namespaceValue;
	}

	public String getCharset() {
		return charset;
	}

	class AsakusaCol {
		String name;
		String type;
		String desc;

		AsakusaCol(String name, String type, String desc) {
			this.name = name;
			this.type = type;
			this.desc = desc;
		}
	}
}
