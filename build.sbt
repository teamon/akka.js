name := "akka.js"

scalaVersion := "2.10.2"

libraryDependencies += "org.mashupbots.socko" %% "socko-webserver" % "0.3.0"

seq(Revolver.settings: _*)
