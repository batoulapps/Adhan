import PackageDescription

let package = Package(
    name: "AdhanSwift",
    targets: [
        Target(name: "AdhanSwift")
    ]
)
package.exclude = ["java", "JavaScript", "Times", "AdhanSwift/Adhan.xcodeproj", "AdhanSwift/AdhanTests", "AdhanSwift/Example"]
