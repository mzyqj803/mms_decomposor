@echo off
echo 测试Redis缓存功能
echo.

echo 1. 检查Redis连接状态
curl -X GET "http://localhost:8080/api/cache/status"
echo.
echo.

echo 2. 重新加载缓存
curl -X POST "http://localhost:8080/api/cache/reload"
echo.
echo.

echo 3. 再次检查缓存状态
curl -X GET "http://localhost:8080/api/cache/status"
echo.
echo.

echo 4. 测试获取零部件（请替换为实际的零部件编号）
echo curl -X GET "http://localhost:8080/api/cache/component/YOUR_COMPONENT_CODE"
echo.

echo 5. 清空缓存
curl -X DELETE "http://localhost:8080/api/cache/clear"
echo.
echo.

echo 测试完成！
pause
