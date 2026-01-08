package com.revethq.iam.user.persistence.deployment

import io.quarkus.deployment.annotations.BuildStep
import io.quarkus.deployment.builditem.FeatureBuildItem

class UserPersistenceProcessor {

    companion object {
        const val FEATURE = "revet-user-persistence"
    }

    @BuildStep
    fun feature(): FeatureBuildItem {
        return FeatureBuildItem(FEATURE)
    }
}
