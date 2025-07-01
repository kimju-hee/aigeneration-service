#!/bin/bash
set -e

echo "[0] 유틸리티 설치 (기본 툴)"
sudo apt-get update
sudo apt-get install -y curl apt-transport-https ca-certificates gnupg lsb-release

echo "[1] httpie 설치"
pip install --upgrade pip
pip install httpie

echo "[2] Azure CLI 설치"
curl -sL https://packages.microsoft.com/keys/microsoft.asc | \
  gpg --dearmor | \
  sudo tee /etc/apt/trusted.gpg.d/microsoft.gpg > /dev/null

echo "deb [arch=amd64] https://packages.microsoft.com/repos/azure-cli/ jammy main" | \
  sudo tee /etc/apt/sources.list.d/azure-cli.list

sudo apt-get update
sudo apt-get install -y azure-cli

echo "[3] Helm 설치"
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 | bash


echo "[4] maven snapshot 빌드"
mvn clean package -DskipTests

echo "[5] Kafka 도구 다운로드 및 PATH 설정"
mkdir -p ~/kafka
cd ~/kafka

if [ ! -d "kafka_2.13-3.7.0" ]; then
  echo "Kafka 압축파일 다운로드 중..."
  wget https://archive.apache.org/dist/kafka/3.7.0/kafka_2.13-3.7.0.tgz
  tar -xzf kafka_2.13-3.7.0.tgz
fi

export KAFKA_HOME=~/kafka/kafka_2.13-3.7.0
export PATH=$KAFKA_HOME/bin:$PATH
echo "Kafka CLI PATH 설정 완료: $(which kafka-console-producer.sh)"

echo "[6] Kafka docker-compose 실행"
if [ -f "./docker-compose.yml" ]; then
  docker-compose up -d
else
  echo "docker-compose.yml 파일이 현재 디렉토리에 없습니다. Kafka 컨테이너는 수동 실행이 필요합니다."
fi

echo "모든 초기 설정 완료!"
