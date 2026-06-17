# Fabric Modding Setup (Minecraft 26.1.2)

Tout ce qu'il faut pour démarrer un mod Fabric frais.

---

## 1. Structure du projet

```
MonMod/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew / gradlew.bat
├── gradle/wrapper/gradle-wrapper.jar
├── icon.png
├── LICENSE
├── README.md
├── .gitignore
├── libs/
│   ├── fabric-api-base-*.jar
│   └── fabric-networking-api-v1-*.jar
└── src/main/
    ├── java/com/monmod/
    │   ├── MonMod.java             # ModInitializer
    │   ├── MonModClient.java       # ClientModInitializer
    │   ├── client/
    │   ├── mixin/
    │   ├── network/
    │   ├── payload/
    │   └── util/
    └── resources/
        ├── fabric.mod.json
        └── monmod.mixins.json
```

---

## 2. Fichiers de configuration

### settings.gradle

```groovy
pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = "https://maven.fabricmc.net/"
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "MonMod"
```

### gradle.properties

```properties
mod_version = 1.0.0
maven_group = com.monmod
archives_base_name = MonMod

minecraft_version = 26.1.2
loader_version = 0.19.2
loom_version = 1.16-SNAPSHOT

fabric_version = 0.150.0+26.1.2

java_version = 25

org.gradle.download.sources=false
```

### build.gradle

```groovy
plugins {
    id "fabric-loom" version "1.16-SNAPSHOT"
}

version = project.mod_version
group = project.maven_group

base {
    archivesName = project.archives_base_name
}

repositories {
    mavenCentral()
    maven {
        name = "Fabric"
        url = "https://maven.fabricmc.net/"
    }
}

dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings "net.fabricmc:intermediary:0.0.0:v2"
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation fileTree(dir: "libs", include: "*.jar")
}

processResources {
    inputs.property "version", project.version
    inputs.property "loader_version", project.loader_version
    inputs.property "minecraft_version", project.minecraft_version
    inputs.property "fabric_version", project.fabric_version
    inputs.property "java_version", project.java_version

    filesMatching("fabric.mod.json") {
        expand "version": project.version,
                "loader_version": project.loader_version,
                "minecraft_version": project.minecraft_version,
                "fabric_version": project.fabric_version,
                "java_version": project.java_version
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(project.java_version)
    }
}

jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}
```

### src/main/resources/fabric.mod.json

```json
{
  "schemaVersion": 1,
  "id": "monmod",
  "version": "${version}",
  "name": "MonMod",
  "description": "Description",
  "authors": ["Toi"],
  "contact": {
    "homepage": "https://...",
    "sources": "https://..."
  },
  "license": "CC-BY-NC-4.0",
  "icon": "icon.png",
  "environment": "*",
  "entrypoints": {
    "main": ["com.monmod.MonMod"],
    "client": ["com.monmod.MonModClient"]
  },
  "mixins": ["monmod.mixins.json"],
  "depends": {
    "fabricloader": ">=${loader_version}",
    "minecraft": ">=${minecraft_version}",
    "fabric-api": ">=${fabric_version}",
    "java": ">=${java_version}"
  }
}
```

### src/main/resources/monmod.mixins.json

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.monmod.mixin",
  "compatibilityLevel": "JAVA_21",
  "client": [
    "MaMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

### .gitignore

```
build/
.gradle/
.idea/
*.iml
libs/
run/
```

---

## 3. Classes de base

### MonMod.java (ModInitializer)

```java
package com.monmod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonMod implements ModInitializer {
    public static final String MOD_ID = "monmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Enregistrement des packets serveur, etc.
        LOGGER.info("MonMod initialized");
    }
}
```

### MonModClient.java (ClientModInitializer)

```java
package com.monmod;

import net.fabricmc.api.ClientModInitializer;

public class MonModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Enregistrement des packets client, etc.
    }
}
```

---

## 4. Pièges à éviter (26.1.2)

| Problème | Solution |
|----------|----------|
| `officialMojangMappings()` ne marche pas | Utiliser `intermediary:0.0.0:v2` — identity map, noms Mojang natifs |
| Fabric API meta-jar fait crasher Loom | Extraire les vrais jars dans `libs/`, utiliser `fileTree(dir: "libs")` |
| `mouseClicked` pas toujours intercepté (sous-classes) | Injecter dans `slotClicked` à la place |
| `ContainerSetSlotPacket` slot -1 → crash | Utiliser `ClientboundSetCursorItemPacket` pour le curseur |
| `playC2S`/`playS2C` n'existent plus | Remplacer par `serverboundPlay()`/`clientboundPlay()` |
| Couleurs hex invisibles | Toujours mettre l'alpha : `0xFF` devant (sinon alpha=0 → transparent) |

---

## 5. Commandes

```bash
./gradlew build          # compile + jar dans build/libs/
./gradlew runClient      # lance Minecraft (si loom configure)
```

---

## 6. Java requis

**Java 25** (early-access). Téléchargement : https://jdk.java.net/25/
