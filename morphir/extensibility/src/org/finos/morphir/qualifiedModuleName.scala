package org.finos.morphir

private[morphir] trait QualifiedModuleNameExports {
  self: PackageNameExports with PathExports with ModuleNameExports with NamespaceExports =>

  /// A qualified moduule name is a globally unique identifier for a module. It is represented by the combination of a package name and the module name.
  sealed case class QualifiedModuleName(packageName: PackageName, modulePath: ModuleName) { self =>
    def /(moduleName: ModuleName): QualifiedModuleName =
      QualifiedModuleName(self.packageName, self.modulePath ++ moduleName)
    def /(namespaceAddition: String): QualifiedModuleName =
      QualifiedModuleName(self.packageName, modulePath.addPart(namespaceAddition))

    def toTuple: (Path, Path) = (packageName.toPath, modulePath.toPath)
  }

  object QualifiedModuleName {
    val empty: QualifiedModuleName = QualifiedModuleName(PackageName.empty, ModuleName.empty)

    def apply(packageName: Path, modulePath: Path): QualifiedModuleName =
      QualifiedModuleName(PackageName.fromPath(packageName), ModuleName(modulePath))

    def apply(modulePath: String)(implicit packageName: PackageName): QualifiedModuleName =
      QualifiedModuleName(packageName, ModuleName.fromString(modulePath))

    object AsTuple {
      def unapply(name: QualifiedModuleName): Option[(Path, Path)] =
        Some(name.toTuple)
    }
  }
}
