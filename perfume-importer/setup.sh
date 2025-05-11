#!/bin/bash

# 필요한 디렉토리 생성
mkdir -p logs
mkdir -p .vscode

# 패키지 설치
echo "패키지 설치 중..."
npm install

# 환경 파일 생성
if [ ! -f .env ]; then
    echo "환경 설정 파일 생성 중..."
    cp example.env .env
    echo ".env 파일이 생성되었습니다. 필요에 따라 설정을 수정하세요."
fi

# VSCode 설정
echo '{
    "editor.formatOnSave": true,
    "editor.codeActionsOnSave": {
        "source.fixAll.eslint": true
    },
    "typescript.tsdk": "node_modules/typescript/lib"
}' > .vscode/settings.json

# 컴파일
echo "TypeScript 컴파일 중..."
npm run build

echo "설치가 완료되었습니다!"
echo "다음 명령어로 배치 작업을 실행하세요: npm run import" 