#!/bin/bash
echo "🟢 Iniciando Postgres para Orders..."

service postgresql start

# Esperar a que Postgres esté disponible
until pg_isready -U postgres > /dev/null 2>&1; do
  echo "⏳ Esperando Postgres..."
  sleep 2
done

# Crear usuario y base de datos si no existen
su - postgres -c "psql -tc \"SELECT 1 FROM pg_database WHERE datname = 'orders_db'\" | grep -q 1 || createdb orders_db"
su - postgres -c "psql -c \"CREATE USER orders_user WITH PASSWORD 'orders_pass';\" || true"
su - postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE orders_db TO orders_user;\""

echo "🚀 Iniciando microservicio Orders..."
java -jar /app/app.jar
