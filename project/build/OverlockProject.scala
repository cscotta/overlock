import sbt._
import sbt.StringUtilities._

class OverlockProject(info : ProjectInfo) extends DefaultProject(info) {
  val boundary = "Boundary Public Repo" at "http://maven.boundary.com/artifactory/repo"
  override def managedStyle = ManagedStyle.Maven
  val publishTo = "Boundary Public Repo (Publish)" at "http://maven.boundary.com/artifactory/external"
  
  Credentials(Path.userHome / ".ivy2" / ".credentials-external", log)
  
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.7" % "test"
  val metrics = "com.yammer" %% "metrics" % "2.0.0-BETA13-SNAPSHOT"
  val highScale = "com.boundary" % "high-scale-lib" % "1.0.3"
  
  lazy val throughput = runTask(Some("overlock.atomicmap.ThroughputTest"), runClasspath).dependsOn(compile) describedAs "Runs the throughput test."
}