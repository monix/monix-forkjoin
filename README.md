# monix-forkjoin

A port of the ForkJoinPool JSR-166 implementation used in Scala 2.11.x
for usage in Scala 2.12.

## Rationale 

In Scala 2.11.8 the `scala.concurrent.forkjoin.ForkJoinPool` implementation 
is a fork of the JSR-166 implementation by Doug Lea, in order to provide 
support for Java versions older than version 8. 
However in Scala 2.12.x the implementation is now an alias for 
`java.util.concurrent.ForkJoinPool`, given of its availability in 
Java 8 and the Scala 2.12 requirement to have Java 8 as a target. 

Unfortunately these 2 implementations are NOT the same, as the 
old Scala 2.11 implementation has better throughput in testing. 

The version of `ForkJoinPool` in 2.11 uses busy-waiting more 
aggressively which helps out in tests with more cores than 
tasks. However, this comes at the expense of other workloads on 
the process/machine, especially because the JVM does not let the 
busy waiting signal its intention to the CPU with a Spin Loop hint. 
This trade-off was found during testing of the `ForkJoinPool` as it was 
integrated into parallel `java.util.stream` and is discussed in JDK-8080623.

But in testing this implementation can have considerable better 
throughput and can prove useful for heavy CPU-bound tasks and
the performance hit for some use-cases is quite noticeable.

## Usage

Available for Scala 2.10, 2.11 and 2.12.
Add the dependency in your SBT file:

```scala
libraryDependencies += "io.monix" %% "monix-forkjoin" % "1.0" 
```

And then:

```scala
import monix.forkJoin.ForkJoinExecutionContext

implicit val executionContext = 
  ForkJoinExecutionContext.createDynamic(
    parallelism = Runtime.getRuntime.availableProcessors(),
    maxThreads = 256,
    name = "forkJoin-dynamic",
    daemonic = true
  )
```

## License

The `monix-forkjoin` project is licensed under the 3-Clause BSD license,
the same license as Scala, see `LICENSE.txt` for details. 

The `ForkJoinPool` implementation is copied from the Scala 2.11 repository, 
which itself was copied from Doug Lea's JSR-166 implementation and is under the 
[public domain](http://creativecommons.org/publicdomain/zero/1.0/).