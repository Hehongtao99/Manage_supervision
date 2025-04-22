@echo off
echo 执行用户编号更新脚本...

rem 配置数据库连接信息
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=manage_supervision
set DB_USER=root
set DB_PASS=root

rem 执行SQL脚本
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% < update_user_number.sql

echo 更新完成!
pause 