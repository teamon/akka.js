name := "akka.js"

scalaVersion := "2.9.2"

libraryDependencies += "org.mashupbots.socko" %% "socko-webserver" % "0.2.3"

seq(Revolver.settings: _*)
