package play.ground.sootup

import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation
import sootup.java.core.JavaProject
import sootup.java.core.JavaSootClass
import sootup.java.core.JavaSootMethod
import sootup.java.core.language.JavaLanguage
import java.io.File


val home = "${System.getenv("HOME")}/Dalviks/"
val lang = JavaLanguage(9)

/**
 * open apk file, if missing, open copy corresponding jar file and open as dex
 */
fun read(manufacture: String, identifier: String) =
  File("$home/$manufacture/$identifier.apk").takeIf { it.exists() } ?: run {
    val jar = File("$home/$manufacture/$identifier.jar")
    val apk = File("$home/$manufacture/$identifier.apk")
    jar.copyTo(apk)
    apk
  }

/**
 * dump all parcelables class definition
 */
fun parcelable(manufacture: String, identifier: String): Set<String> {
  val src = read(manufacture, identifier)
  val location = JavaClassPathAnalysisInputLocation(src.absolutePath)

  val project = JavaProject.builder(lang).addInputLocation(location).build()
  val view = project.createView()
  val Parcelable = project.identifierFactory.getClassType("android.os.Parcelable")
  val cache = view.classes.asSequence().filter {
    it.isConcrete && it.interfaces.contains(Parcelable)
  }.map(JavaSootClass::toString).toSet()

  // write file
  val output = "parcelable/$manufacture.$identifier.txt"
  File(output).writeText(cache.joinToString("\n"))

  return cache
}


/**
 * extract binder call interfaces from framework.jar
 */
fun aidl(manufacture: String, identifier: String): Set<String> {
  val src = read(manufacture, identifier)
  val location = JavaClassPathAnalysisInputLocation(src.absolutePath)
  val project = JavaProject.builder(lang).addInputLocation(location).build()
  val view = project.createView()
  val IInterface = project.identifierFactory.getClassType("android.os.IInterface")

  val aidls = view.classes.asSequence().filter {
    it.isInterface && it.interfaces.contains(IInterface)
  }.flatMap {
    it.methods.filter(JavaSootMethod::isAbstract)
  }.map(JavaSootMethod::toString).toSet()

  val output = "aidl/$manufacture.$identifier.txt"
  File(output).writeText(aidls.joinToString("\n"))
  return aidls
}


fun generateQuantified() {
  """tecno14 pixel14beta2 samsung""".split("""\s+""".toRegex()).forEach {
    aidl(it, "framework")
    parcelable(it, "framework")
    parcelable(it, "services")
  }
}

fun generateDiff() {
  val (tecno, pixel, samsung) = """tecno14 pixel14beta2 samsung""".split("""\s+""".toRegex()).map {
    File("aidl/$it.framework.txt").readLines().toSet()
  }
  File("diff/tecno-pixel.aidl").writeText((tecno - pixel).joinToString("\n"))
  File("diff/samsung-pixel.aidl").writeText((samsung - pixel).joinToString("\n"))
}

fun main() {
  generateDiff()
}