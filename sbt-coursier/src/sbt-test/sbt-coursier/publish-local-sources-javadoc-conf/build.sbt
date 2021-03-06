
val org = "io.get-coursier.scriptedtest"
val ver = "0.1.0-SNAPSHOT"

lazy val foo = project
  .settings(
    shared
  )

lazy val bar = project
  .settings(
    shared,
    libraryDependencies += org %% "foo" % ver
  )


lazy val shared = Seq(
  organization := org,
  version := ver,
  scalaVersion := "2.11.8",
  confCheck := {

    val updateReport = update.value
    val updateClassifiersReport = updateClassifiers.value

    def artifacts(config: String, classifier: Option[String], useClassifiersReport: Boolean = false) = {

      val configReport = (if (useClassifiersReport) updateClassifiersReport else updateReport)
        .configuration(config)
        .getOrElse {
          throw new Exception(
            s"$config configuration not found in update report"
          )
        }

      val artifacts = configReport
        .modules
        .flatMap(_.artifacts)
        .collect {
          case (a, _) if a.classifier == classifier =>
            a
        }

      streams.value.log.info(
        s"Found ${artifacts.length} artifacts for config $config / classifier $classifier" +
          (if (useClassifiersReport) " in classifiers report" else "")
      )
      for (a <- artifacts)
        streams.value.log.info("  " + a)

      artifacts
    }

    val compileSourceArtifacts = artifacts("compile", Some("sources"))
    val sourceArtifacts = artifacts("compile", Some("sources"), useClassifiersReport = true)

    val compileDocArtifacts = artifacts("compile", Some("javadoc"))
    val docArtifacts = artifacts("compile", Some("javadoc"), useClassifiersReport = true)

    assert(
      compileSourceArtifacts.isEmpty,
      "Expected no source artifact in main update report"
    )
    assert(
      sourceArtifacts.length == 2,
      "Expected 2 source artifacts in classifier report"
    )
    assert(
      compileDocArtifacts.isEmpty,
      "Expected no doc artifact in main update report"
    )
    assert(
      docArtifacts.length == 2,
      "Expected 2 doc artifacts in classifier report"
    )
  }
)


lazy val confCheck = TaskKey[Unit]("confCheck")
