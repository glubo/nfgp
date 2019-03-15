#!/bin/bash -x
set -e

function show_help() {
    echo "Options:"
    echo "  -h/-?/--help        Displays this help"
    echo "  --build             Run build"
    echo "  --push              Push Docker images to glubo"
    echo "  --pull              Pull Docker images from glubo"
}

REQUIRED_IMAGES=("glubo/java:8-jre")

APP_VERSION=${TAG:-'latest'}
WORK_HOME=/opt/app-root

BUILD_IMAGE_NAME=glubo/build-nfgp:${APP_VERSION}
CONTAINER_NAME=build-nfgp-${APP_VERSION}
TARGET_NAME=glubo/nfgp
TARGET_IMAGE_NAME=${TARGET_NAME}:${APP_VERSION}

PROJECT_NAME="nfgp:$APP_VERSION"

export BUILD_IMAGE_NAME
export WORK_HOME
export APP_VERSION

function build() {
    docker build -t ${BUILD_IMAGE_NAME} -f docker/Dockerfile.build .
    docker create --name ${CONTAINER_NAME} ${BUILD_IMAGE_NAME}
    docker cp ${CONTAINER_NAME}:target ./
    docker rm ${CONTAINER_NAME}
    docker rmi ${BUILD_IMAGE_NAME}
    docker build -t ${TARGET_IMAGE_NAME} -f docker/Dockerfile .
}

function push() {
    docker push ${TARGET_IMAGE_NAME}
}

function docker_pull() {
    docker pull ${TARGET_IMAGE_NAME}
}


while :; do
    case $1 in
        -h|-\?|--help)   # Call a "show_help" function to display a synopsis, then exit.
            show_help
            exit
            ;;
        --build)
            build
            ;;
        --push)
            push
            ;;
        --pull)
            docker_pull
            ;;
        --)              # End of all options.
            shift
            break
            ;;
        -?*)
            printf 'WARN: Unknown option (ignored): %s\n' "$1" >&2
            ;;
        *)               # Default case: If no more options then break out of the loop.
            break
    esac

    shift
done
