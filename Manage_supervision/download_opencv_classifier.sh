#!/bin/bash

echo "正在下载OpenCV级联分类器文件..."

CLASSIFIER_URL="https://raw.githubusercontent.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_alt.xml"
RESOURCE_DIR="backend/src/main/resources/haarcascade"
DATA_DIR="data/haarcascade"

echo "创建目录..."
mkdir -p "$RESOURCE_DIR"
mkdir -p "$DATA_DIR"

echo "下载文件到资源目录..."
curl -o "$RESOURCE_DIR/haarcascade_frontalface_alt.xml" "$CLASSIFIER_URL"

echo "下载文件到数据目录..."
curl -o "$DATA_DIR/haarcascade_frontalface_alt.xml" "$CLASSIFIER_URL"

echo "检查下载结果..."
if [ -f "$RESOURCE_DIR/haarcascade_frontalface_alt.xml" ]; then
    echo "资源目录下载成功: $RESOURCE_DIR/haarcascade_frontalface_alt.xml"
else
    echo "资源目录下载失败!"
fi

if [ -f "$DATA_DIR/haarcascade_frontalface_alt.xml" ]; then
    echo "数据目录下载成功: $DATA_DIR/haarcascade_frontalface_alt.xml"
else
    echo "数据目录下载失败!"
fi

echo "下载完成!" 