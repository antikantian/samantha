name := "samantha"

version := "0.1.0"

scalaVersion := "2.12.1"

organization in ThisBuild := "co.quine"

lazy val versions = new {
  val akka = "2.5.0"
  val akkaHttp = "10.0.5"
  val bijection = "0.9.5"
  val cats = "0.9.0"
  val circe = "0.7.0"
  val config = "1.3.1"
  val finagle = "6.43.0"
  val finch = "0.14.0"
  val jmdns = "3.4.1"
  val monix = "2.1.1"
  val enumeratum = "1.5.12"
  val nscalatime = "2.16.0"
  val spire = "0.13.0"
  val squants = "1.2.0"
  val shapeless = "2.3.2"
  val twitterServer = "1.28.0"
  val rabbit = "1.1.4"
  val redis = "1.8.0"
}

lazy val baseSettings = Seq(
  scalaVersion := "2.12.1",
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    "jgit-repo" at "http://download.eclipse.org/jgit/maven"
  ),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % versions.akka,
    "com.typesafe.akka" %% "akka-stream" % versions.akka,
    "com.typesafe.akka" %% "akka-contrib" % versions.akka,
    "com.typesafe.akka" %% "akka-http" % versions.akkaHttp,
    "com.github.etaty" %% "rediscala" % versions.redis,
    "org.typelevel" %% "cats" % versions.cats,
    "org.typelevel" %% "squants" % versions.squants,
    "com.twitter" %% "bijection-core" % versions.bijection,
    "com.twitter" %% "finagle-core" % versions.finagle,
    "com.twitter" %% "finagle-netty4" % versions.finagle,
    "com.twitter" %% "finagle-redis" % versions.finagle,
    "com.github.finagle" %% "finch-core" % versions.finch,
    "com.github.finagle" %% "finch-circe" % versions.finch,
    "com.github.finagle" %% "finch-sse" % versions.finch,
    "org.spire-math" %% "spire" % versions.spire,
    "io.monix" %% "monix" % versions.monix,
    "com.chuusai" %% "shapeless" % versions.shapeless,
    "com.github.nscala-time" %% "nscala-time" % versions.nscalatime,
    "com.typesafe" % "config" % versions.config,
    "com.beachape" %% "enumeratum" % versions.enumeratum,
    "io.scalac" %% "reactive-rabbit" % versions.rabbit
  )
)

lazy val root = project.in(file("."))
  .settings(baseSettings)
  .aggregate(core, server)
  .dependsOn(core)

lazy val core = project
  .settings(moduleName := "samantha-core")
  .settings(baseSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % versions.circe,
      "io.circe" %% "circe-generic" % versions.circe,
      "io.circe" %% "circe-parser" % versions.circe,
      "io.circe" %% "circe-optics" % versions.circe
    ),
    initialCommands in console :=
      """
        |import com.twitter.conversions.time._
        |import com.twitter.finagle.Samantha
        |import com.twitter.finagle.samantha.protocol._
        |import com.twitter.util._
      """.stripMargin
  )

lazy val appletv = project
  .settings(moduleName := "samantha-appletv")
  .settings(baseSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.twitter" %% "finagle-mdns" % versions.finagle
    )
  )
  .dependsOn(core)

lazy val free = project
  .settings(moduleName := "samantha-free")
  .settings(baseSettings)
  .dependsOn(core)

lazy val library = project
  .settings(moduleName := "samantha-library")
  .settings(baseSettings)
  .dependsOn(core)

lazy val pioneervsx = project
  .settings(moduleName := "samantha-pioneer-vsx")
  .settings(baseSettings)
  .dependsOn(core)

lazy val server = project
  .settings(moduleName := "samantha-server")
  .settings(baseSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.twitter" %% "twitter-server" % versions.twitterServer
    ),
    initialCommands in console :=
    """
      |import akka.actor.ActorSystem
      |import akka.stream.ActorMaterializer
      |import io.samantha.protocol._
      |import io.samantha.devices._
      |
      |implicit val a = ActorSystem()
      |implicit val m = ActorMaterializer()
    """.stripMargin
  )
  .dependsOn(core)


  
        