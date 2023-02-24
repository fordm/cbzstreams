flatMap over ZStream question

Constructing a ZStream using ZStream.async does not behave as expected with some combinators 

When the (finite) stream is constructed with ZStream.fromIterable flatMap works as expected

When the (infinite) stream is constructed with ZStream.async, flatMap causes the calling thread
to get stuck in state WAITING on zio.internal.OneShot after mapping the first data item
from the first stream.  The second data item from the first stream is never processed
by the flatMap parameter

There is a test example that runs: EnhancedSubscriptionSpec

There is a test app that gets stuck: SubscriptionRunner

