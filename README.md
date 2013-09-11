JavaZinc
========

[![Build Status](https://travis-ci.org/mindsnacks/JavaZinc.png)](https://travis-ci.org/mindsnacks/JavaZinc)

This is a WIP of a Java Zinc client.
Only a very limited of features is supported:
    - Tracking different sources and bundles.
    - Flavors.
    - Tracking distributions.

### Usage
You can create a ```ZincRepo``` using ```ZincRepoFactory```:
```java
new ZincRepoFactory().createRepo(new File(currentDirectory), "flavor");
```

Then you can add your sources:
```java
final String catalogID = "com.mindsnacks.catalog";
repo.addSourceURL(new SourceURL(new URL("http://zinc-repo.com/"), catalogID));
```

And start tracking bundles:
```java
repo.startTrackingBundle(new BundleID(catalogID, "english-kidsvocab-astronomy"), "master");
```

Once you've started tracking the bundles you need, you can get ```Future```s for them:
```java
final Future<ZincBundle> bundle = repo.getBundle(bundleID);
```

### Other clients:
  - [Python](https://github.com/mindsnacks/Zinc)
  - [Objective-C](https://github.com/mindsnacks/Zinc-ObjC/)
