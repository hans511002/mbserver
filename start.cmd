chcp 65001
REM del -F .\conf\cluster.cfg
REM del -F .\bin\install\docker_containers

start /MIN "" sbin\start-server.cmd
start "" sbin\webUi.url

