# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Pick Perfume Backend is a perfume recommendation and community service built with Kotlin + Spring Boot following Domain-Driven Design (DDD) principles.

## Key Commands

### Build & Run
```bash
# Build the project (skip tests for faster builds)
./gradlew clean build -x test

# Run locally
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "ym_cosmetic.pick_perfume_be.*TestClassName"
```

### Deployment
```bash
# Deploy to AWS Elastic Beanstalk (triggered automatically on main branch push)
# Manual deployment:
./deploy-eb.sh

# Environment variables needed for deployment are configured in AWS EB console
```

### Local Development Setup
```bash
# Copy environment variables
cp .env.example .env

# Start dependencies (MySQL, Elasticsearch) via Docker
docker-compose -f docker-compose.local.yml up -d

# Initialize Elasticsearch mappings
./es-setup.sh
```

## Architecture & Structure

### Domain-Driven Design Structure
The codebase follows DDD principles with clear separation of concerns:

```
src/main/kotlin/ym_cosmetic/pick_perfume_be/
├── [domain]/              # Each bounded context (perfume, member, review, etc.)
│   ├── domain/           # Core business logic
│   │   ├── entity/      # Domain entities with private constructors
│   │   ├── vo/         # Value objects (immutable)
│   │   ├── repository/ # Repository interfaces
│   │   └── service/    # Domain services
│   ├── infrastructure/  # External dependencies
│   │   └── repository/ # JPA repository implementations
│   ├── application/    # Application services & DTOs
│   └── presentation/   # REST controllers
```

### Key Bounded Contexts
- **perfume**: Core perfume data and management
- **member**: User management and authentication
- **review**: User reviews and ratings
- **survey**: User preference surveys for recommendations
- **recommendation**: ML-based perfume recommendations
- **ai_recommendation**: Advanced AI-powered recommendation system with multimodal features
- **search**: Elasticsearch-based search functionality
- **community**: Social features and interactions
- **vote**: Voting and ranking systems

### Design Patterns
- **Static Factory Methods**: All entities use private constructors with companion object factory methods
- **Value Objects**: Immutable data classes for domain concepts (Rating, PerfumeId, etc.)
- **Repository Pattern**: Interface in domain layer, implementation in infrastructure
- **Domain Events**: Event-driven architecture for cross-boundary communication

## Technology Stack & Dependencies

- **Framework**: Spring Boot 3.4.5, Kotlin 1.9.25
- **Database**: MySQL with JPA/Hibernate, Flyway migrations
- **Search**: OpenSearch/Elasticsearch for perfume search
- **Caching**: Caffeine cache
- **Storage**: AWS S3 for images
- **Security**: Spring Security with JWT
- **API Docs**: SpringDoc OpenAPI (Swagger UI at /docs)
- **Build**: Gradle with Kotlin DSL
- **AI/ML**: Deep Java Library (DJL), PyTorch for model inference
- **Image Generation**: Stable Diffusion API integration

## Deployment Configuration

### AWS Elastic Beanstalk
- Platform: Java 21 Corretto
- Auto-deployment via GitHub Actions on main branch push
- Configuration in `.ebextensions/` directory
- Environment variables configured in AWS EB console

### Required Environment Variables
```
# Database
RDS_HOSTNAME, RDS_PORT, RDS_DB_NAME, RDS_USERNAME, RDS_PASSWORD

# Elasticsearch
ES_HOST, ES_PORT, ES_USERNAME, ES_PASSWORD

# AWS
AWS_ACCESS_KEY, AWS_SECRET_KEY, S3_BUCKET_NAME

# Security
JWT_SECRET, SWAGGER_USERNAME, SWAGGER_PASSWORD

# AI/ML Services
AI_IMAGE_API_URL, AI_IMAGE_API_KEY, AI_EMBEDDING_MODEL
```

## Database Migrations

Flyway manages database migrations automatically on startup. Migration files are in `src/main/resources/db/migration/`.

## API Documentation

Swagger UI available at `/docs` endpoint (requires Basic Auth in production).

## AI Recommendation System

### Features
- **Few-Shot Learning**: Personalized recommendations with minimal user data (3-5 interactions)
- **Multimodal Fusion**: Combines text descriptions, visual features, and olfactory notes
- **Visual Preference Survey**: AI-generated images based on fragrance notes for user onboarding
- **Cross-Domain Transfer**: Leverages knowledge from wine, cosmetics, and food domains
- **Meta-Learning (MAML)**: Rapid adaptation to new users and preferences

### Key APIs
- `POST /api/v1/ai-recommendation/visual-survey/start` - Generate visual preference survey
- `POST /api/v1/ai-recommendation/visual-survey/submit` - Submit survey responses
- `GET /api/v1/ai-recommendation/recommendations/{memberId}` - Get AI-powered recommendations

### Development Notes
- Image generation uses Stable Diffusion API (configure `AI_IMAGE_API_KEY`)
- Embeddings are cached in database for performance
- Transfer learning models are pre-configured for perfume domain adaptation
- All AI features are designed to work with sparse data scenarios