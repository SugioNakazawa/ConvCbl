/**
 * 
 */
package com.hoge;

import java.util.Arrays;

/**
 * @author nakazawasugio
 *
 */
public class ComUtil {
	/**
	 * String配列から指定文字のインデックスを取得。ないときは-1
	 * 
	 * @param cols
	 * @param key
	 * @return
	 */
	public static int search(String[] cols, String key) {
		return Arrays.asList(cols).indexOf(key);
	}
}
