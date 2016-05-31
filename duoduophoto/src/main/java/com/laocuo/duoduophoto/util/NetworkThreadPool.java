/*******************************************************************************
 * Copyright (c) 2012 Manning
 * See the file license.txt for copying permission.
 ******************************************************************************/
package com.laocuo.duoduophoto.util;

import java.util.concurrent.Future;

public class NetworkThreadPool {
  private static LIFOThreadPoolProcessor pool = new LIFOThreadPoolProcessor(
      3);

  public static Future<?> submitTask(LIFOTask task) {
    return pool.submitTask(task);
  }
}
