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
 * AsakusaDMDL出力。
 * 
 * @author nakazawasugio
 *
 */
public class AsakusaDmdl {
	static Logger logger = LoggerFactory.getLogger(AsakusaDmdl.class.getName());
	static final String MODEL_DESC = "\"{0}\"\n";
	static final String NAME_SPACE_REC = "@namespace(value = {0})\n";
	static final String DIRECTIO_CSV = "@directio.csv(charset = \"{0}\")\n";
	static final String SP4 = "    ";

	/** データモデル名称 **/
	private String fileName;
	List<DmdlModel> modelList;

	public AsakusaDmdl(String fileName) {
		this.fileName = fileName;
		this.modelList = new ArrayList<DmdlModel>();
	}

	void addModel(DmdlModel model) {
		this.modelList.add(model);
	}

	/**
	 * DMDLファイルの作成。
	 * 
	 * @param path 出力するディレクトリ。
	 * @throws IOException
	 */
	public void createDmdl(String path) throws IOException {
		File file = new File(path + "/" + fileName + ".dmdl");
		FileWriter fw = new FileWriter(file);
		for (DmdlModel model : modelList) {
			fw.write(MessageFormat.format(MODEL_DESC, model.desc));
			fw.write(MessageFormat.format(NAME_SPACE_REC, model.namespace));
			fw.write(MessageFormat.format(DIRECTIO_CSV, model.charset));
			fw.write(model.name + " = {\n"); // name
			for (DmdlColumn col : model.getColList()) {
				fw.write(SP4 + "\"" + col.desc + "\"\n");
				fw.write(SP4 + col.name + " : " + col.type + ";\n\n");
			}
			fw.write("};\n\n"); // end-name
		}
		fw.close();
		logger.info("created dmdl " + file.getAbsolutePath());
	}

	class DmdlModel {
		String name;
		String desc;
		String namespace;
		String charset;
		List<DmdlColumn> colList;

		DmdlModel(String name, String desc, String namespace, String charset) {
			this.name = name;
			this.desc = desc;
			this.namespace = namespace;
			this.charset = charset;
			colList = new ArrayList<DmdlColumn>();
		}

		String getName() {
			return name;
		}

		String getNamespaceValue() {
			return namespace;
		}

		String getCharset() {
			return charset;
		}

		List<DmdlColumn> getColList() {
			return colList;
		}

		void addColumn(DmdlColumn col) {
			this.colList.add(col);
		}
	}

	class DmdlColumn {
		String name;
		String type;
		String desc;

		DmdlColumn(String name, String type, String desc) {
			this.name = name;
			this.type = type;
			this.desc = desc;
		}
	}
}
