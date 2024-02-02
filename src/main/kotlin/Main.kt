package play.ground.sootup

import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation
import sootup.java.core.JavaProject
import sootup.java.core.JavaSootMethod
import sootup.java.core.language.JavaLanguage
import java.io.File

fun generate() {
  val framework = "${System.getenv("HOME")}/Dalviks/pixel14beta2/framework"
  val dex = "$framework.apk"
  val txt = "$framework.txt"
  val lang = JavaLanguage(8)
  val location = JavaClassPathAnalysisInputLocation(dex)
  val project = JavaProject.builder(lang).addInputLocation(location).build()
  val view = project.createView()
  val IInterface = project.identifierFactory.getClassType("android.os.IInterface")

  val cache = view.classes.asSequence().filter {
    it.isInterface && it.interfaces.contains(IInterface)
  }.flatMap {
    it.methods.filter(JavaSootMethod::isAbstract)
  }.joinToString("\n") {
    it.toString()
  }
  File(txt).writeText(cache)
}


fun main() {
  val base = "${System.getenv("HOME")}/Dalviks/framework.aidl.api"
  val pixel = File("$base/pixel14beta2.aidl.api").readLines().toSet()
  val coloros = File("$base/coloros.flip.aidl.api").readLines().toSet()
  File("$base/coloros-pixel.diff.aidl.api").writeText(
    (coloros - pixel).joinToString("\n")
  )
}