package com.example.android.sunshine.app.test;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by hector on 08/09/14.
 */
public class FullTextSuite extends TestSuite {

    public static Test suite() {
        return new TestSuiteBuilder(FullTextSuite.class)
                .includeAllPackagesUnderHere().build();
    }

    public FullTextSuite(){
        super();
    }
}