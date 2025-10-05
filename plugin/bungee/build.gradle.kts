plugins {
    id("com.github.johnrengelman.shadow").version("8.1.1")
}

group = project.parent?.group!!
version = project.parent?.version!!

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.21-R0.2")

    implementation("org.bstats:bstats-bungeecord:3.0.2")
    implementation(project(":plugin:common"))
    implementation(project(":common"))
}

tasks {
    shadowJar {
        archiveFileName.set("${rootProject.name}-${project.name}-${project.version}.jar")

        relocate("org.bstats", "net.savagedev.tpa.metrics")

        minimize()
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(sourceSets.main.get().resources.srcDirs) {
            expand(Pair("version", project.version))
                .include("bungee.yml")
        }
    }
}
