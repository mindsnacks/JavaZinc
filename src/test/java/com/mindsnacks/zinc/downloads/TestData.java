package com.mindsnacks.zinc.downloads;

import com.mindsnacks.zinc.classes.downloads.DownloadPriority;
import com.mindsnacks.zinc.utils.TestFactory;

/**
 * @author NachoSoto
 */
public class TestData {
    private DownloadPriority mPriority;
    private final String mResult;

    public static TestData randomTestData() {
        return TestData.randomTestData(
                DownloadPriority.values()[TestFactory.randomInt(0, DownloadPriority.values().length - 1)]);
    }

    public static TestData randomTestData(final DownloadPriority priority) {
        return new TestData(
                priority,
                TestFactory.randomString());
    }

    public TestData(final DownloadPriority priority, final String result) {
        mPriority = priority;
        mResult = result;
    }

    public DownloadPriority getPriority() {
        return mPriority;
    }

    public String getResult() {
        return mResult;
    }

    public void setPriority(final DownloadPriority priority) {
        mPriority = priority;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TestData data = (TestData) o;

        return (mPriority == data.mPriority) && !(mResult != null ? !mResult.equals(data.mResult) : data.mResult != null);
    }

    @Override
    public int hashCode() {
        return 31 * mPriority.getValue() + (mResult != null ? mResult.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "TestData {" + mResult + ": " + mPriority + "}";
    }
}

