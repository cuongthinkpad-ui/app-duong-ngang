# Source this file from bash in the prepared cloud environment.
export JAVA_HOME=/workspace/toolchains/jdk-21.0.8
export ANDROID_HOME=/workspace/toolchains/android-sdk
export ANDROID_USER_HOME=/workspace/toolchains/android-home
export GRADLE_USER_HOME=/workspace/toolchains/gradle-home
export PATH="/workspace/toolchains/gradle-8.9/bin:$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
