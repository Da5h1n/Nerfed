stonecutter active "26.4-snapshot-10"

val activeProject = stonecutter.current?.project

if (activeProject != null) {
    tasks.register("buildActive") {
        group = "stonecutter"
        description = "Builds the currently active Stonecutter version ($activeProject)"
        dependsOn(":$activeProject:build")
    }

    tasks.register("runActiveClient") {
        group = "stonecutter"
        description = "Runs the client for the currently active Stonecutter version ($activeProject)"
        dependsOn(":$activeProject:runClient")
    }
}