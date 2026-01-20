package com.revethq.iam.permission.persistence.deployment

import io.quarkus.deployment.annotations.BuildStep
import io.quarkus.deployment.builditem.FeatureBuildItem

class PermissionPersistenceProcessor {

    companion object {
        const val FEATURE = "revet-permission-persistence"
    }

    @BuildStep
    fun feature(): FeatureBuildItem {
        return FeatureBuildItem(FEATURE)
    }
}
