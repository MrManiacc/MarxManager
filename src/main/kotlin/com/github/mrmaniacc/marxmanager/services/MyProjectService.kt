package com.github.mrmaniacc.marxmanager.services

import com.github.mrmaniacc.marxmanager.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
