lazy val root = (project in file("."))
  .settings(
    name := "mlb-api",
    version := "1.0",
    scalaVersion := "3.3.0",
    libraryDependencies ++= Seq(
      "com.h2database" % "h2" % "2.1.214",
      "dev.zio" %% "zio" % "2.0.15",
      "dev.zio" %% "zio-streams" % "2.0.15",
      "dev.zio" %% "zio-schema" % "0.4.12",
      "dev.zio" %% "zio-jdbc" % "0.0.2",
      "dev.zio" %% "zio-json" % "0.6.0",
      "dev.zio" %% "zio-http" % "3.0.0-RC2",
      "com.github.tototoshi" %% "scala-csv" % "1.3.10"
    ).map(_ % Compile),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29"
    ).map(_ % Test)
  )
