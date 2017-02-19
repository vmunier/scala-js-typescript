package models.parse.sc.printer

import better.files._
import models.parse.ProjectDefinition
import models.parse.sc.transform.ReplacementManager
import models.parse.sc.tree.Name
import services.parse.ClassReferenceService

case class PrinterFilesSingle(project: ProjectDefinition, file: File) extends PrinterFiles {
  if (file.exists) {
    file.delete()
  }

  file.append(s"package org.scalajs.${project.keyNormalized}\n")

  file.append("\n")
  file.append("import scala.scalajs.js\n")

  override def pushPackage(pkg: Name) = {
    val p = pkg.name.replaceAllLiterally("-", "")
    file.append(s"package $p {\n")
  }
  override def popPackage(pkg: Name) = file.append(s"}\n")

  override def print(s: String) = file.append(s)

  override def onComplete() = {
    val replacements = ReplacementManager.getReplacements(project.key)
    val originalContent = file.lines.toList
    val newContent = replacements.replace(originalContent)
    val (rewrite, finalContent) = if (originalContent != newContent) {
      true -> newContent
    } else {
      false -> originalContent
    }

    val ret = ClassReferenceService.insertImports(finalContent)
    file.delete()
    file.write(ret.mkString("\n"))
    ret
  }
}