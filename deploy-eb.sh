#!/bin/bash

# 도커 없이 AWS Elastic Beanstalk에 애플리케이션 배포

# 환경 변수 설정
EB_ENV_NAME=${EB_ENV_NAME:-"Scentist-env"}
EB_APP_NAME=${EB_APP_NAME:-"scentist"}
AWS_REGION=${AWS_REGION:-"ap-northeast-2"}
S3_BUCKET=${S3_BUCKET_NAME:-"elasticbeanstalk-ap-northeast-2-441464446738"}
VERSION_LABEL="v$(date +%Y%m%d%H%M%S)"

echo "===== 빈스톡 배포 시작 ====="
echo "애플리케이션: $EB_APP_NAME"
echo "환경: $EB_ENV_NAME"
echo "리전: $AWS_REGION"
echo "버전: $VERSION_LABEL"

# 1. 프로젝트 빌드
echo "===== 프로젝트 빌드 ====="
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo "빌드 실패"
    exit 1
fi

# 2. 배포 패키지 생성
echo "===== 배포 패키지 생성 ====="
mkdir -p deploy
cp -r .ebextensions deploy/
cp -r build/libs/*.jar deploy/application.jar
cp Procfile deploy/
cp mapping.json settings.json deploy/

# 3. 배포 패키지 압축
echo "===== 배포 패키지 압축 ====="
cd deploy
zip -r "../${EB_APP_NAME}-${VERSION_LABEL}.zip" .
cd ..

# 4. S3에 업로드
echo "===== S3에 업로드 ====="
aws s3 cp "${EB_APP_NAME}-${VERSION_LABEL}.zip" "s3://${S3_BUCKET}/${EB_APP_NAME}/${EB_APP_NAME}-${VERSION_LABEL}.zip" --no-paginate

if [ $? -ne 0 ]; then
    echo "S3 업로드 실패"
    exit 1
fi

# 5. Elastic Beanstalk 애플리케이션 버전 생성
echo "===== Elastic Beanstalk 애플리케이션 버전 생성 ====="
aws elasticbeanstalk create-application-version \
    --application-name "$EB_APP_NAME" \
    --version-label "$VERSION_LABEL" \
    --source-bundle S3Bucket="$S3_BUCKET",S3Key="${EB_APP_NAME}/${EB_APP_NAME}-${VERSION_LABEL}.zip" \
    --region "$AWS_REGION" \
    --no-paginate

if [ $? -ne 0 ]; then
    echo "애플리케이션 버전 생성 실패"
    exit 1
fi

# 6. Elastic Beanstalk 환경 업데이트
echo "===== Elastic Beanstalk 환경 업데이트 ====="
aws elasticbeanstalk update-environment \
    --environment-name "$EB_ENV_NAME" \
    --version-label "$VERSION_LABEL" \
    --region "$AWS_REGION" \
    --no-paginate

if [ $? -ne 0 ]; then
    echo "환경 업데이트 실패"
    exit 1
fi

# 7. 임시 파일 정리
echo "===== 임시 파일 정리 ====="
rm -rf deploy
rm "${EB_APP_NAME}-${VERSION_LABEL}.zip"

echo "===== 배포 완료 ====="
echo "배포 상태를 확인하려면 다음 명령어를 실행하세요:"
echo "aws elasticbeanstalk describe-environments --environment-names $EB_ENV_NAME --region $AWS_REGION --no-paginate" 