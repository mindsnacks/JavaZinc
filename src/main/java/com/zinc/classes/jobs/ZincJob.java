package com.zinc.classes.jobs;

import java.util.concurrent.Callable;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public interface ZincJob<V> extends Callable<V> {

}