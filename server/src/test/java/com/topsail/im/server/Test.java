package com.topsail.im.server;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @date 2021-02-01
 */
public class Test {
    public static void main(String[] args) {
        System.out.println("===");
        Map<String, String> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k2", null);
        System.out.println(map);

    }
}
