# Copy WSL Path IntelliJ Plugin Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `idea/` 中构建一个面向 IntelliJ IDEA 2025.1+ 的 Kotlin 插件，实现与 VS Code 扩展一致的四种路径复制能力、菜单开关和成功通知开关。

**Architecture:** 使用 IntelliJ Platform Gradle Plugin 2.x 创建独立 Gradle 工程。纯路径转换和真实路径解析放在可单元测试的 Kotlin 组件中；四个 Project View Action 共享一个复制流程基类；应用级持久化设置控制通知与菜单可见性。

**Tech Stack:** Kotlin 2.1.20, Java 21, Gradle Kotlin DSL, IntelliJ Platform Gradle Plugin 2.x, IntelliJ IDEA 2025.1+ platform, JUnit 5。

---

## Task 1: Create the IntelliJ plugin project skeleton

**Files:**
- Create: `idea/settings.gradle.kts`
- Create: `idea/build.gradle.kts`
- Create: `idea/gradle.properties`
- Create: `idea/.gitignore`
- Create: `idea/src/main/resources/META-INF/plugin.xml`
- Create: `idea/src/main/kotlin/com/cyitianyou/copywslpath/package-info.kt`

- [ ] **Step 1: Add the Gradle settings file**

Create `idea/settings.gradle.kts` with the project name and plugin repositories:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "copy-wsl-path-idea"
```

Add `import org.gradle.api.initialization.resolve.RepositoriesMode` at the top of the file.

- [ ] **Step 2: Add the Gradle plugin configuration**

Create `idea/build.gradle.kts` using IntelliJ Platform Gradle Plugin 2.x, Kotlin JVM 2.1.20, target IntelliJ IDEA 2025.2.6.1, minimum build `251` for IntelliJ IDEA 2025.1+, Java 21, JUnit 5, and the platform test framework. Disable the automatic stdlib dependency so the plugin uses the Kotlin runtime bundled by the target IntelliJ platform instead of packaging a duplicate runtime.

The build must define:

```kotlin
plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.11.0"
}

group = "com.cyitianyou"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2025.2.6.1")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.cyitianyou.copywslpath"
        name = "Copy WSL Path"
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "251"
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
    test {
        useJUnitPlatform()
    }
}
```

Adjust only syntax required by the installed Gradle/Kotlin plugin versions; retain the same platform, compatibility, and task behavior.

- [ ] **Step 3: Add project properties and ignore rules**

Create `idea/gradle.properties` with Kotlin code style and Gradle JVM settings:

```properties
kotlin.code.style=official
org.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8
```

Create `idea/.gitignore` ignoring `.gradle/`, `build/`, `.idea/`, and generated plugin archives.

- [ ] **Step 4: Add the initial plugin descriptor**

Create `idea/src/main/resources/META-INF/plugin.xml` with the plugin ID, name, vendor, description, and `com.intellij.modules.platform` dependency. Register the settings service, Project View actions, notification group, and `applicationConfigurable` entry as each corresponding implementation is added in later tasks.

Do not register actions or classes before their implementation exists; this step may contain the metadata and extension points that are completed in later tasks.

- [ ] **Step 5: Add a package marker and create the Gradle wrapper**

Create an empty `package-info.kt` containing only the package declaration and an explanatory comment. From `idea/`, generate a Gradle wrapper using the locally available Gradle installation. The wrapper must be committed as `idea/gradlew`, `idea/gradlew.bat`, and `idea/gradle/wrapper/*` so the project can build independently of a system Gradle installation.

- [ ] **Step 6: Run the empty project configuration task**

Run:

```bash
cd idea
./gradlew tasks --all
```

Expected: Gradle configures the IntelliJ Platform project and lists `test`, `buildPlugin`, and `verifyPlugin` tasks. If dependency resolution or DSL syntax fails, fix the build files before adding production code.

## Task 2: Implement the path converter with TDD

**Files:**
- Create: `idea/src/test/kotlin/com/cyitianyou/copywslpath/PathConverterTest.kt`
- Create: `idea/src/main/kotlin/com/cyitianyou/copywslpath/PathConverter.kt`

- [ ] **Step 1: Write failing converter tests**

Create JUnit 5 tests using the platform test framework for the exact behavior agreed in the design:

```kotlin
package com.cyitianyou.copywslpath

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PathConverterTest {
    @Test
    fun convertsUppercaseDrivePath() {
        assertEquals(
            "/mnt/e/MyWorks/demo.txt",
            PathConverter.toWslPath("E:\\MyWorks\\demo.txt")
        )
    }

    @Test
    fun convertsLowercaseDrivePath() {
        assertEquals(
            "/mnt/c/Users/demo/file.txt",
            PathConverter.toWslPath("c:\\Users\\demo\\file.txt")
        )
    }

    @Test
    fun convertsWindowsRootPathWithTrailingSlash() {
        assertEquals("/mnt/c/", PathConverter.toWslPath("C:\\"))
    }

    @Test
    fun trimsInputBeforeConverting() {
        assertEquals("/mnt/e/Work/demo", PathConverter.toWslPath("  E:\\Work\\demo  "))
    }

    @Test
    fun rejectsEmptyInput() {
        assertThrows<IllegalArgumentException> {
            PathConverter.toWslPath("")
        }
    }

    @Test
    fun rejectsUncPath() {
        assertThrows<IllegalArgumentException> {
            PathConverter.toWslPath("\\\\server\\share\\file.txt")
        }
    }

    @Test
    fun rejectsRelativePath() {
        assertThrows<IllegalArgumentException> {
            PathConverter.toWslPath("src\\main\\file.txt")
        }
    }
}
```

- [ ] **Step 2: Run converter tests and verify the expected red failure**

Run:

```bash
cd idea
./gradlew test --tests com.cyitianyou.copywslpath.PathConverterTest
```

Expected: the test task fails because `PathConverter` does not exist yet. Fix test setup errors if the failure is not caused by the missing implementation.

- [ ] **Step 3: Implement the minimal converter**

Implement `PathConverter.toWslPath(winPath: String): String` with a compiled regular expression matching `^[A-Za-z]:\\(.*)$`, `trim()`, lowercase drive conversion, backslash replacement, and `/mnt/<drive>/<tail>` assembly. Throw `IllegalArgumentException("Only Windows drive paths like C:\\... are supported.")` for invalid input.

- [ ] **Step 4: Run converter tests and verify green**

Run the same focused test command and then the complete test suite:

```bash
cd idea
./gradlew test --tests com.cyitianyou.copywslpath.PathConverterTest
./gradlew test
```

Expected: all converter tests pass with zero failures.

## Task 3: Implement real-path resolution with TDD

**Files:**
- Create: `idea/src/test/kotlin/com/cyitianyou/copywslpath/PathResolverTest.kt`
- Create: `idea/src/main/kotlin/com/cyitianyou/copywslpath/PathResolver.kt`

- [ ] **Step 1: Write failing resolver tests**

Test the injected resolver seam so tests remain platform-independent:

```kotlin
package com.cyitianyou.copywslpath

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PathResolverTest {
    @Test
    fun returnsResolvedPath() {
        val result = PathResolver.resolveRealPath("E:\\link", { "E:\\physical\\target" })

        assertEquals("E:\\physical\\target", result)
    }

    @Test
    fun wrapsResolverFailureWithReadableMessage() {
        val error = assertThrows<IllegalArgumentException> {
            PathResolver.resolveRealPath("E:\\missing", { throw IllegalStateException("ENOENT") })
        }

        assertEquals("Failed to resolve real path: ENOENT", error.message)
    }

    @Test
    fun trimsPathBeforeResolving() {
        var received = ""
        PathResolver.resolveRealPath("  E:\\link  ") {
            received = it
            it
        }

        assertEquals("E:\\link", received)
    }
}
```

- [ ] **Step 2: Run resolver tests and verify the expected red failure**

Run:

```bash
cd idea
./gradlew test --tests com.cyitianyou.copywslpath.PathResolverTest
```

Expected: the test task fails because `PathResolver` does not exist yet.

- [ ] **Step 3: Implement the resolver**

Implement `PathResolver.resolveRealPath(path: String, resolver: (String) -> String = { Paths.get(it).toRealPath().toString() })`. Trim before invoking the resolver and wrap any exception as `IllegalArgumentException("Failed to resolve real path: <cause message>")`.

- [ ] **Step 4: Run all unit tests**

Run:

```bash
cd idea
./gradlew test
```

Expected: converter and resolver tests pass.

## Task 4: Add persistent settings and the Settings page

**Files:**
- Create: `idea/src/main/kotlin/com/cyitianyou/copywslpath/CopyWslPathSettings.kt`
- Create: `idea/src/main/kotlin/com/cyitianyou/copywslpath/CopyWslPathConfigurable.kt`
- Modify: `idea/src/main/resources/META-INF/plugin.xml`

- [ ] **Step 1: Add the persistent settings service**

Implement an application-level `@Service` and `@State` component with a serializable state containing five Boolean properties, all defaulting to `true`: `showNotification`, `showCopyPathMenu`, `showCopyRealPathMenu`, `showCopyWslPathMenu`, and `showCopyRealWslPathMenu`. Expose a companion `getInstance()` method and mutable properties that persist changes.

- [ ] **Step 2: Add the configurable UI**

Implement `Configurable` with a `JPanel` containing five `JBCheckBox` controls. `reset()` loads the service values, `isModified()` compares UI values to persisted values, `apply()` writes all five values, and `disposeUIResources()` clears the panel reference. The display name must be `Copy WSL Path` and the settings page must appear under Tools.

- [ ] **Step 3: Register the service and configurable**

Keep the `@Service` and `@State` annotations on `CopyWslPathSettings` and update `plugin.xml` with the configurable entry only:

```xml
<applicationConfigurable
    parentId="tools"
    instance="com.cyitianyou.copywslpath.CopyWslPathConfigurable" />
```

- [ ] **Step 4: Run compilation and platform verification**

Run:

```bash
cd idea
./gradlew compileKotlin verifyPluginConfiguration
```

Expected: Kotlin compilation and plugin descriptor validation pass.

## Task 5: Implement shared copy Actions and Project View integration

**Files:**
- Create: `idea/src/main/kotlin/com/cyitianyou/copywslpath/CopyMode.kt`
- Create: `idea/src/main/kotlin/com/cyitianyou/copywslpath/CopyPathAction.kt`
- Create: `idea/src/main/kotlin/com/cyitianyou/copywslpath/CopyPathActions.kt`
- Modify: `idea/src/main/resources/META-INF/plugin.xml`

- [ ] **Step 1: Define the four copy modes**

Create an enum with `RAW`, `REAL`, `WSL`, and `REAL_WSL`. Each concrete action will pass one enum value to the shared action implementation.

- [ ] **Step 2: Implement shared Action behavior**

Create an abstract `CopyPathAction(private val mode: CopyMode, private val menuSetting: (CopyWslPathSettings) -> Boolean)` that:

1. Implements `getActionUpdateThread()` using the IntelliJ 2025.1-compatible background update thread.
2. In `update()`, reads `CommonDataKeys.VIRTUAL_FILE`, enables the action only when a single resource exists, and sets visibility from the matching settings property.
3. In `actionPerformed()`, reads the selected `VirtualFile`, chooses raw or resolved path, optionally converts it with `PathConverter`, writes the result through `CopyPasteManager`, and optionally publishes a success notification.
4. Catches all expected failures and displays a readable error notification without fallback behavior.

Use `VirtualFile.path` as the raw path source and preserve the complete copied result in the success message.

- [ ] **Step 3: Add four concrete action classes**

Create four small classes with stable action IDs and titles:

```kotlin
class CopyPathAction : CopyPathAction(CopyMode.RAW, CopyWslPathSettings::showCopyPathMenu)
class CopyRealPathAction : CopyPathAction(CopyMode.REAL, CopyWslPathSettings::showCopyRealPathMenu)
class CopyWslPathAction : CopyPathAction(CopyMode.WSL, CopyWslPathSettings::showCopyWslPathMenu)
class CopyRealWslPathAction : CopyPathAction(CopyMode.REAL_WSL, CopyWslPathSettings::showCopyRealWslPathMenu)
```

If Kotlin name shadowing prevents the exact class/base-class names, name the base `BaseCopyPathAction` while retaining the four public action class names and behavior.

- [ ] **Step 4: Register the actions and notification group**

Add four `<action>` entries to `ProjectViewPopupMenu` with group anchors ordered as raw, real raw, WSL, real WSL. Add a notification group with display type `BALLOON`. Keep all action classes in the plugin descriptor so their visibility is available in the Project View context menu.

- [ ] **Step 5: Compile the plugin**

Run:

```bash
cd idea
./gradlew compileKotlin verifyPluginConfiguration
```

Expected: all Action, settings, and plugin.xml classes compile and descriptor verification passes.

## Task 6: Add documentation and complete verification

**Files:**
- Create: `idea/README.md`
- Create: `idea/CHANGELOG.md`
- Modify: `docs/superpowers/plans/2026-08-05-copy-wsl-path-intellij-plugin.md`

- [ ] **Step 1: Document IDEA plugin usage**

Document supported IDE version, required Java 21, Gradle commands, local run command, Project View actions, settings, supported path formats, and the generated plugin ZIP location.

- [ ] **Step 2: Document the initial release**

Add a `0.1.0` changelog entry describing the four actions, real-path support, configurable menu visibility, and notification setting.

- [ ] **Step 3: Run the full test and packaging commands**

Run from `idea/`:

```bash
./gradlew clean test
./gradlew buildPlugin
./gradlew verifyPlugin
```

Expected:

- `test` completes with zero failures.
- `buildPlugin` creates `idea/build/distributions/copy-wsl-path-idea-0.1.0.zip`.
- `verifyPlugin` completes without plugin compatibility errors.

- [ ] **Step 4: Inspect the generated plugin archive**

Run:

```bash
unzip -l build/distributions/*.zip
```

Confirm that the archive contains `META-INF/plugin.xml`, compiled Kotlin classes, and no test classes or source files.

- [ ] **Step 5: Check the final worktree**

Run from the repository root:

```bash
git diff --check
git status --short --untracked-files=all
```

Confirm that only the intended `idea/` files, design document, and plan document are newly added; do not stage, remove, or modify the pre-existing `.codex`, `docs/windows-context-menu.md`, or `scripts/` files.
