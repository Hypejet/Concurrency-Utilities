plugins {
    java
    `maven-publish`
}

group = "net.hypejet"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.annotations)
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        pom.licenses {
            license {
                name = "MIT"
                url = "https://choosealicense.com/licenses/mit/"
            }
        }
    }
}

tasks.withType<Javadoc> {
    val docletOptions = options as StandardJavadocDocletOptions
    docletOptions.addBooleanOption("html5", true)
    docletOptions.addStringOption("Xdoclint:none", "-quiet")
}