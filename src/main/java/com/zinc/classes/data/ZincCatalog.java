package com.zinc.classes.data;

import com.google.gson.annotations.SerializedName;
import com.zinc.exceptions.ZincException;

import java.util.Map;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincCatalog {
    @SerializedName("id")
    private final String mIdentifier;

    @SerializedName("bundles")
    private final Map<String, Info> mBundles;

    public ZincCatalog(final String identifier, final Map<String, Info> bundles) {
        mIdentifier = identifier;
        mBundles = bundles;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public int getVersionForBundleID(final String bundleID, final String distribution) throws DistributionNotFoundException {
        try {
            return mBundles.get(bundleID).getVersionForDistribution(distribution);
        } catch (DistributionNotFoundException e) {
            throw new DistributionNotFoundException(distribution, bundleID);
        } catch (NullPointerException e) {
            throw new DistributionNotFoundException(distribution, bundleID);
        }
    }

    @Override
    public String toString() {
        return "ZincCatalog {" +
                "mIdentifier='" + mIdentifier + '\'' +
                ", mBundles=" + mBundles +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ZincCatalog that = (ZincCatalog)o;

        return mBundles.equals(that.mBundles) && mIdentifier.equals(that.mIdentifier);

    }

    public static class Info {
        @SerializedName("distributions")
        private final Map<String, Integer> mDistributions;

        public Info(final Map<String, Integer> distributions) {
            mDistributions = distributions;
        }

        public int getVersionForDistribution(String distribution) throws DistributionNotFoundException {
            if (mDistributions.containsKey(distribution)) {
                return mDistributions.get(distribution);
            } else {
                throw new DistributionNotFoundException(distribution);
            }
        }

        @Override
        public String toString() {
            return "Info {" +
                    "mDistributions=" + mDistributions +
                    '}';
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            return mDistributions.equals(((Info)o).mDistributions);
        }
    }

    public static class DistributionNotFoundException extends ZincException {
        public DistributionNotFoundException(final String distribution) {
            super(String.format("Distribution '%s' not found", distribution));
        }

        public DistributionNotFoundException(final String distribution, final String bundleID) {
            super(String.format("Distribution '%s' not found in bundle ''%s", distribution, bundleID));
        }
    }
}
