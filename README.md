JavaZinc
========

This is a WIP of a Java Zinc client.
Only a very limited set of features is supported:

  - Tracking different sources and bundles.
  - Flavors.
  - Tracking distributions.

### Download

Grab via Maven:

```xml
<dependency>
    <groupId>com.mindsnacks.javazinc</groupId>
    <artifactId>JavaZinc</artifactId>
    <version>(insert latest version)</version>
</dependency>
```

### Usage
You can create a ```ZincRepo``` using ```ZincRepoFactory```:
```java
final int bundleCloneConcurrency = 2;

new ZincRepoFactory().createRepo(
    new File(currentDirectory),
    "flavor",
    bundleCloneConcurrency,
    new DownloadPriorityCalculator<BundleID>());
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

```DownloadPriorityCalculator``` allows you to add objects that handle calculating priorities for a subset of the bundles.
For example:
```java
priorityCalculator.addHandler(new PriorityCalculator<BundleID>() {
                               @Override
                               public DownloadPriority getPriorityForObject(final BundleID bundleID) {
                                   return DownloadPriority.NEEDED_IMMEDIATELY;
                               }
                           });
```

If a particular handler does not know about a subset of the bundle IDs (for example, a handler might only work with a catalog), then it can return ```DownloadPriority.UNKNOWN```.

Once you've started tracking the bundles you need, you have to start the repo:
```java
repo.start();
```

And then you can get ```Future```s for the bundles:
```java
final Future<ZincBundle> bundle = repo.getBundle(bundleID);
```

### Other clients:
  - [Python](https://github.com/mindsnacks/Zinc)
  - [Objective-C](https://github.com/mindsnacks/Zinc-ObjC/)
