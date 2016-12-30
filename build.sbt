name := "monix-forkjoin"

version := "1.0"

organization := "io.monix"

scalaVersion := "2.12.1"

crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1")

compileOrder in ThisBuild := CompileOrder.JavaThenScala

scalacOptions ++= {
  val baseOptions = Seq(
    // warnings
    "-unchecked", // able additional warnings where generated code depends on assumptions
    "-deprecation", // emit warning for usages of deprecated APIs
    "-feature",     // emit warning usages of features that should be imported explicitly
    // possibly deprecated options
    "-Ywarn-dead-code",
    "-Ywarn-inaccessible"
  )
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, majorVersion)) if majorVersion >= 12 => baseOptions
    case _ => baseOptions :+ "-target:jvm-1.6" // generates code with the Java 6 class format
  }
}

// version specific compiler options
scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, majorVersion)) if majorVersion >= 11 =>
    Seq(
      // enables linter options
      "-Xlint:adapted-args", // warn if an argument list is modified to match the receiver
      "-Xlint:nullary-unit", // warn when nullary methods return Unit
      "-Xlint:inaccessible", // warn about inaccessible types in method signatures
      "-Xlint:nullary-override", // warn when non-nullary `def f()' overrides nullary `def f'
      "-Xlint:infer-any", // warn when a type argument is inferred to be `Any`
      "-Xlint:missing-interpolator", // a string literal appears to be missing an interpolator id
      "-Xlint:doc-detached", // a ScalaDoc comment appears to be detached from its element
      "-Xlint:private-shadow", // a private field (or class parameter) shadows a superclass field
      "-Xlint:type-parameter-shadow", // a local type parameter shadows a type already in scope
      "-Xlint:poly-implicit-overload", // parameterized overloaded implicit methods are not visible as view bounds
      "-Xlint:option-implicit", // Option.apply used implicit view
      "-Xlint:delayedinit-select", // Selecting member of DelayedInit
      "-Xlint:by-name-right-associative", // By-name parameter of right associative operator
      "-Xlint:package-object-classes", // Class or object defined in package object
      "-Xlint:unsound-match" // Pattern match may not be typesafe
    )
  case _ =>
    Seq.empty
})

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
  "Spy" at "http://files.couchbase.com/maven2/",
  Resolver.sonatypeRepo("snapshots")
)

// -- Settings meant for deployment on oss.sonatype.org

useGpg := true
useGpgAgent := true
usePgpKeyHex("2673B174C4071B0E")

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false
pomIncludeRepository := { _ => false } // removes optional dependencies

pomExtra in ThisBuild :=
  <url>https://github.com/monix/monix-forkjoin</url>
  <licenses>
    <license>
      <name>The MIT License</name>
      <url>https://opensource.org/licenses/BSD-3-Clause</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:monix/monix-forkjoin.git</url>
    <connection>scm:git:git@github.com:monix/monix-forkjoin.git</connection>
  </scm>
  <developers>
    <developer>
      <id>alex_ndc</id>
      <name>Alexandru Nedelcu</name>
      <url>https://alexn.org</url>
    </developer>
  </developers>

// Multi-project-related

lazy val monixForkJoin = project in file(".")
