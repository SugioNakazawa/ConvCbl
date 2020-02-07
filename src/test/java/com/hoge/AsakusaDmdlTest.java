package com.hoge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.hoge.AsakusaDmdl.DmdlModel;

public class AsakusaDmdlTest {
	static private final String PATH = "src/test/resources/com/hoge/asakusadmdl";

	@Test
	public void testAsakusaDmdl() throws IOException {
		AsakusaDmdl target = new AsakusaDmdl();
		{
			DmdlModel model = target.new DmdlModel("model1", "モデル1", "file", "UTF-8");
			model.addColumn(target.new DmdlColumn("column_key", "TEXT", "キー１"));
			model.addColumn(target.new DmdlColumn("column_value", "TEXT", "値１"));
			target.addModel(model);
			
			Assert.assertEquals("model1", model.getName());
			Assert.assertEquals("file", model.getNamespaceValue());
			Assert.assertEquals("UTF-8", model.getCharset());
		}
		{
			DmdlModel model = target.new DmdlModel("model2", "モデル2", "file", "UTF-8");
			model.addColumn(target.new DmdlColumn("column_key", "TEXT", "キー１"));
			model.addColumn(target.new DmdlColumn("column_value", "TEXT", "値１"));
			target.addModel(model);
		}
		target.createDmdl(Paths.get("out/sample.dot"));
		String fileA = PATH + "/exp_sample.dmdl";
		String fileB = "out/sample.dmdl";
		Assert.assertTrue(Arrays.equals(Files.readAllBytes(Paths.get(fileA)), Files.readAllBytes(Paths.get(fileB))));

	}

}
