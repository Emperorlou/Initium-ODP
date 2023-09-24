rootProject.name = "InitiumODP"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.google.cloud.tools.appengine")) {
                useModule("com.google.cloud.tools:appengine-gradle-plugin:${requested.version}")
            }
        }
    }
}


include(":CachedDatastore")
project(":CachedDatastore").projectDir = file("../../CachedDatastore")
project(":CachedDatastore").buildFileName = "build.gradle"

include(":GameElementsFramework")
project(":GameElementsFramework").projectDir = file("../../GameElementsFramework")
project(":GameElementsFramework").buildFileName = "build.gradle"

include(":GEFCommon")
project(":GEFCommon").projectDir = file("../../GEFCommon")
project(":GEFCommon").buildFileName = "build.gradle"

include(":RandomGenerators")
project(":RandomGenerators").projectDir = file("../../RandomGenerators")
project(":RandomGenerators").buildFileName = "build.gradle"

include(":JsonSimple")
project(":JsonSimple").projectDir = file("../../JsonSimple")
project(":JsonSimple").buildFileName = "build.gradle"

include(":UPCommon")
project(":UPCommon").projectDir = file("../../UPCommon")
project(":UPCommon").buildFileName = "build.gradle"

include(":java-webapp-lite")
project(":java-webapp-lite").projectDir = file("../../java-webapp-lite")
project(":java-webapp-lite").buildFileName = "build.gradle"
