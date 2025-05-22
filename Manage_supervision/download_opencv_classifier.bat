@echo off
echo 正在下载OpenCV级联分类器文件...

set CLASSIFIER_URL=https://raw.githubusercontent.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_alt.xml
set RESOURCE_DIR=backend\src\main\resources\haarcascade
set DATA_DIR=data\haarcascade

echo 创建目录...
if not exist %RESOURCE_DIR% mkdir %RESOURCE_DIR%
if not exist %DATA_DIR% mkdir %DATA_DIR%

echo 下载文件到资源目录...
powershell -Command "& {Invoke-WebRequest -Uri '%CLASSIFIER_URL%' -OutFile '%RESOURCE_DIR%\haarcascade_frontalface_alt.xml'}"
echo 下载文件到数据目录...
powershell -Command "& {Invoke-WebRequest -Uri '%CLASSIFIER_URL%' -OutFile '%DATA_DIR%\haarcascade_frontalface_alt.xml'}"

echo 检查下载结果...
if exist %RESOURCE_DIR%\haarcascade_frontalface_alt.xml (
    echo 资源目录下载成功: %RESOURCE_DIR%\haarcascade_frontalface_alt.xml
) else (
    echo 资源目录下载失败!
)

if exist %DATA_DIR%\haarcascade_frontalface_alt.xml (
    echo 数据目录下载成功: %DATA_DIR%\haarcascade_frontalface_alt.xml
) else (
    echo 数据目录下载失败!
)

echo 下载完成!
pause 