pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven { url = uri("https://maven.theoplayer.com/releases") }
    }
}

rootProject.name = "Samples-THEOplayer-Android-SDK"

include(":basic-playback")
include(":background-playback")
include(":custom-surface-rendering")
include(":drm-playback")
include(":full-screen-handling")
include(":google-cast")
include(":google-ima")
include(":metadata-handling")
include(":offline-playback")
include(":pip-handling")
include(":simple-ott")
include(":common")
include(":open-video-ui")
