#!/bin/bash

# 1. Certbot이 설치되어 있는지 확인하고, 없으면 설치합니다.
if ! command -v certbot &> /dev/null
then
    echo "Certbot not found. Installing..."
    sudo yum install -y certbot python3-certbot-nginx
fi

# 2. 인증서 파일이 존재하는지 확인합니다.
CERT_FILE="/etc/letsencrypt/live/api.scentist.link/fullchain.pem"

if [ ! -f "$CERT_FILE" ]
then
    echo "Certificate not found. Generating a new one..."

    # Certbot이 80번 포트를 사용해야 하므로 Nginx를 잠시 중지합니다.
    sudo systemctl stop nginx

    # --standalone 옵션으로 인증서를 발급받습니다. 이메일은 본인 것으로 수정해주세요.
    sudo certbot certonly --standalone --non-interactive --agree-tos -m your-email@example.com -d api.scentist.link

    # Nginx를 다시 시작합니다. (배포 프로세스가 나중에 다시 reload 하므로 이 부분은 보험용입니다.)
    sudo systemctl start nginx
else
    echo "Certificate already exists. Skipping generation."
fi