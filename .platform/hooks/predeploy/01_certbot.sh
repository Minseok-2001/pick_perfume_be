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

    sudo systemctl stop nginx

    sudo certbot certonly --standalone --non-interactive --agree-tos -m ms.jung.dev@gmail.com -d api.scentist.link

    sudo systemctl start nginx
else
    echo "Certificate already exists. Skipping generation."
fi