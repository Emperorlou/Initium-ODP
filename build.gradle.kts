plugins {
    java
    //id("com.google.cloud.tools.appengine-appenginewebxml") version "2.4.5"
    //id("com.moowork.node") version "1.3.1"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

//node {
//    version = "14.17.1"
//    npmVersion = "6.14.13"
//    download = true
//}

//tasks.register<Exec>("uglify") {
//    commandLine("cmd", "/c", "for /R build\\staged-app\\ %i in (*.js) do uglifyjs %i -o %i -c -m")
//}

//appengine {
//    deploy {
//        projectId = "playinitium"
//        version = findProperty("frontendVersion") as String? ?: "s1"
//        promote = true
//    }
//
//    run {
//        host = "0.0.0.0"
//        jvmFlags = listOf(
//            "-Ddatastore.backing_store=${rootDir.absolutePath}/../local_db.bin",
//            "-Xss512k",
//            "-XX:+UseSerialGC",
////            "-XX:MaxRAM=800m",
//            "-Xdebug",
//            "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
//        )
//
//        automaticRestart = true
//    }
//
//    tools {
//        cloudSdkVersion = "436.0.0"
//    }
//}

repositories {
    // Add the maven central repo
    mavenCentral()
}

dependencies {
    implementation("javax.servlet:javax.servlet-api:3.1.0")
    implementation("com.google.appengine:appengine-api-1.0-sdk:2.0.12")
    implementation("com.google.cloud:google-cloud-tasks:2.1.0")
    implementation("com.google.protobuf:protobuf-java-util:3.22.2")

    implementation("commons-codec:commons-codec:1.13")
    implementation("javax.servlet:jstl:1.2")
    implementation("org.eclipse.mylyn.github:org.eclipse.egit.github.core:2.1.5")
    implementation("com.theokanning.openai-gpt3-java:service:0.14.0")
    implementation("commons-fileupload:commons-fileupload:1.5")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.knuddels:jtokkit:0.5.0")
    implementation("org.reflections:reflections:0.9.12")
    implementation("org.mozilla:rhino:1.7.14")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("com.sun.activation:javax.activation:1.2.0")
    implementation("org.apache.tika:tika-core:1.28.4")
    implementation("org.apache.commons:commons-text:1.9")

    implementation(project(":CachedDatastore"))
    implementation(project(":GameElementsFramework"))
    implementation(project(":java-webapp-lite"))
    implementation(project(":JsonSimple"))
    implementation(project(":RandomGenerators"))
    implementation(project(":GEFCommon"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("com.google.appengine:appengine-testing:1.9.59")
    testImplementation("com.google.appengine:appengine-api-stubs:1.9.59")
    testImplementation("com.google.appengine:appengine-api-labs:1.9.59")
    testImplementation("com.google.appengine:appengine-api-1.0-sdk:1.9.59")
    testImplementation("io.searchbox:jest:6.3.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<War> {
    from(project(":GameElementsFramework").file("src/main/webapp/shared")) {
        into("admin/editor")
    }
    from(project(":GameElementsFramework").file("src/main/webapp/WEB-INF/pages")) {
        into("WEB-INF/pages/gef")
    }
}

//tasks.named("appengineDeploy") {
//    dependsOn("uglify")
//}