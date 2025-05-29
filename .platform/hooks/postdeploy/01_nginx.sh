#!/bin/bash

cp .platform/nginx/conf.d/*.conf /etc/nginx/conf.d/
nginx -t && systemctl reload nginx
