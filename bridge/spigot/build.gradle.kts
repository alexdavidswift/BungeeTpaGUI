plugins {
    id("com.github.johnrengelman.shadow").version("8.1.1")
}

group = project.parent?.group!!
version = project.parent?.version!!

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")

    compileOnly("net.essentialsx:EssentialsX:2.22.0") {
        exclude("io.papermc")
    }
    compileOnly("com.github.LeonMangler:SuperVanish:6.2.18-3")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation(project(":bridge:common"))
    implementation(project(":common"))
}

tasks {
    shadowJar {
        archiveFileName.set("${rootProject.name}-${project.parent?.name}-${project.name}-${project.version}.jar")

        relocate("org.bstats", "net.savagedev.tpa.metrics")

        minimize()
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(sourceSets.main.get().resources.srcDirs) {
            expand(Pair("version", project.version))
                .include("plugin.yml")
        }
    }
}
