/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Sets common values for Android Applications and Libraries
 */
fun org.gradle.api.Project.androidConfiguration(
    extension: CommonExtension<*, *, *, *>
) = extension.apply {
    compileSdk = 31

    defaultConfig {
        // Could have been 21, but I need sqlite 3.24.0 for upserts
        minSdk = 30
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = versionCatalog.findVersion("androidxCompose").get().requiredVersion
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    (this as org.gradle.api.plugins.ExtensionAware).apply {
        val kotlinOptions = "kotlinOptions"
        if (extensions.findByName(kotlinOptions) != null) {
            extensions.configure(kotlinOptions, Action<KotlinJvmOptions> {
                jvmTarget = "11"
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-Xuse-experimental=androidx.compose.animation.ExperimentalAnimationApi",
                    "-Xuse-experimental=androidx.compose.material.ExperimentalMaterialApi",
                    "-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi",
                    "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-Xuse-experimental=kotlinx.coroutines.FlowPreview"
                )
            })
        }
    }
}

fun org.gradle.api.Project.coerceComposeVersion(configuration: Configuration) {
    configuration.resolutionStrategy.eachDependency {
        if (requested.group.startsWith("androidx.compose")) {
            useVersion(versionCatalog.findVersion("androidxCompose").get().requiredVersion)
            because("I need the changes in lazyGrid")
        }
    }
}

val org.gradle.api.Project.versionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java)
        .named("libs")