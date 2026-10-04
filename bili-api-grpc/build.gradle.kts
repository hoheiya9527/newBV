
import com.google.protobuf.gradle.proto

plugins {
    alias(libs.plugins.google.protobuf)
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(libs.grpc.kotlin.stub)
    api(libs.grpc.okhttp)
    api(libs.grpc.protobuf)
    api(libs.grpc.stub)
    implementation(libs.kotlinx.coroutines)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.truth)
}

sourceSets["main"].proto {
    srcDir("./proto")
    // 只编实际用到的 proto，避免全量生成
    include(
        "bilibili/app/archive/middleware/v1/preload.proto",
        "bilibili/app/archive/v1/archive.proto",
        "bilibili/app/card/v1/ad.proto",
        "bilibili/app/card/v1/card.proto",
        "bilibili/app/card/v1/common.proto",
        "bilibili/app/card/v1/single.proto",
        "bilibili/app/dynamic/v2/dynamic.proto",
        "bilibili/app/interfaces/v1/history.proto",
        "bilibili/app/interfaces/v1/search.proto",
        "bilibili/api/player/v1/player.proto",
        "bilibili/app/playeronline/v1/playeronline.proto",
        "bilibili/app/playerunite/v1/playerunite.proto",
        "bilibili/app/show/popular/v1/popular.proto",
        "bilibili/app/space/v1/space.proto",
        "bilibili/app/view/v1/view.proto",
        "bilibili/community/service/dm/v1/dm.proto",
        "bilibili/dagw/component/avatar/common/common.proto",
        "bilibili/dagw/component/avatar/v1/avatar.proto",
        "bilibili/dagw/component/avatar/v1/plugin.proto",
        "bilibili/main/community/reply/v1/reply.proto",
        "bilibili/metadata/device/device.proto",
        "bilibili/metadata/locale/locale.proto",
        "bilibili/metadata/metadata.proto",
        "bilibili/metadata/network/network.proto",
        "bilibili/pagination/pagination.proto",
        "bilibili/pgc/gateway/player/v2/playurl.proto",
        "bilibili/playershared/playershared.proto",
        "bilibili/polymer/app/search/v1/search.proto",
        "bilibili/rpc/status.proto",
        "common/ErrorProto.proto",
    )
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }
    plugins {
        create("java") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.72.0"
        }
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.72.0"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.builtins {
                // 不生成 protobuf Kotlin DSL：否则 compileKotlin 要吃两千多个生成文件
                named("java") {
                }
            }
            it.plugins {
                create("grpc") {
                }
                create("grpckt") {
                }
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
