flatMap over ZStream question

Constructing a ZStream using ZStream.async does not behave as expected with some combinators 

When the (finite) stream is constructed with ZStream.fromIterable flatMap works as expected

When the (infinite) stream is constructed with ZStream.async, flatMap causes the calling thread
to get stuck in state WAITING on zio.internal.OneShot after mapping the first data item
from the first stream.  The second data item from the first stream is never processed
by the flatMap parameter

There is a test example that runs: EnhancedSubscriptionSpec

There is a test app that gets stuck: SubscriptionRunner


Name: main
State: WAITING on zio.internal.OneShot@1b3a79b0
Total blocked: 5  Total waited: 1

Stack trace:
java.base@11.0.17/java.lang.Object.wait(Native Method)
java.base@11.0.17/java.lang.Object.wait(Object.java:328)
app//zio.internal.OneShot.get(OneShot.scala:83)
app//zio.Runtime$UnsafeAPIV1.run(Runtime.scala:134)
app//zio.ZIOAppPlatformSpecific.main(ZIOAppPlatformSpecific.scala:19)
app//zio.ZIOAppPlatformSpecific.main$(ZIOAppPlatformSpecific.scala:11)
app//zstreamapp.SubscriptionRunner$.main(SubscriptionRunner.scala:7)
app//zstreamapp.SubscriptionRunner.main(SubscriptionRunner.scala)
