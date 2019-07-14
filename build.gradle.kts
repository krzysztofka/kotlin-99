
plugins {
    kotlin("jvm") version "1.3.41"
}

dependencies {
    compile(kotlin("stdlib"))
    testCompile("org.assertj:assertj-core:3.12.2")
    testCompile("junit:junit:4.12")
}

repositories {
    jcenter()
}
