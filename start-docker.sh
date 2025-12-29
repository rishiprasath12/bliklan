#!/bin/bash

# Carpooling Application - Docker Startup Script

echo "======================================"
echo "  Carpooling Application - Docker"
echo "======================================"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Error: Docker is not installed!"
    echo "Please install Docker Desktop from: https://www.docker.com/products/docker-desktop"
    exit 1
fi

# Check if Docker is running
if ! docker info &> /dev/null; then
    echo "❌ Error: Docker is not running!"
    echo "Please start Docker Desktop and try again."
    exit 1
fi

echo "✅ Docker is installed and running"
echo ""

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Error: docker-compose is not installed!"
    exit 1
fi

# Clean up any existing containers (both docker-compose managed and standalone)
echo "🧹 Cleaning up existing containers..."
docker-compose down 2>/dev/null || true

# Remove containers by name if they exist (handles containers created outside docker-compose)
if docker ps -a --format '{{.Names}}' | grep -q "^carpolling-postgres$"; then
    echo "   Removing existing carpolling-postgres container..."
    docker rm -f carpolling-postgres 2>/dev/null || true
fi

if docker ps -a --format '{{.Names}}' | grep -q "^carpolling-app$"; then
    echo "   Removing existing carpolling-app container..."
    docker rm -f carpolling-app 2>/dev/null || true
fi

echo ""

echo "🏗️  Building and starting containers..."
echo ""

# Build and start containers
docker-compose up --build -d

# Wait for services to be healthy
echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check if services are running
if docker-compose ps | grep -q "Up"; then
    echo ""
    echo "✅ Application started successfully!"
    echo ""
    echo "======================================"
    echo "  Access Points"
    echo "======================================"
    echo "🌐 Application API:    http://localhost:8010"
    echo "📚 Swagger UI:         http://localhost:8010/swagger-ui.html"
    echo "📖 API Docs:           http://localhost:8010/api-docs"
    echo "🗄️  PostgreSQL:         localhost:5432"
    echo ""
    echo "======================================"
    echo "  Database Credentials"
    echo "======================================"
    echo "Database: carpolling_db"
    echo "Username: admin"
    echo "Password: admin"
    echo ""
    echo "======================================"
    echo "  Useful Commands"
    echo "======================================"
    echo "View logs:        docker-compose logs -f"
    echo "Stop services:    docker-compose stop"
    echo "Restart:          docker-compose restart"
    echo "Shut down:        docker-compose down"
    echo ""
else
    echo ""
    echo "❌ Error: Failed to start services"
    echo "Check logs with: docker-compose logs"
    exit 1
fi


