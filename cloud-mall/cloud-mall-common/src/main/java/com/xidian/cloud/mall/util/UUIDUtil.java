package com.xidian.cloud.mall.util;

import java.util.UUID;

public class UUIDUtil {
	public static String uuid() {
		return UUID.randomUUID().toString().replace("-", "");//去掉原生的"-"
	}
}
