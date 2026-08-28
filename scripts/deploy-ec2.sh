#!/bin/bash
# =============================================================================
# EcoHome — Script de despliegue en EC2 (Ubuntu 22.04 / 24.04)
# Uso: bash deploy-ec2.sh
# Requisito: tener .env.production configurado en el mismo directorio
# =============================================================================
set -e

echo "🚀 EcoHome Deploy — iniciando..."

# ── 1. Instalar Docker si no está ─────────────────────────────────────────────
if ! command -v docker &> /dev/null; then
    echo "📦 Instalando Docker..."
    apt-get update -q
    apt-get install -y -q ca-certificates curl gnupg
    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    chmod a+r /etc/apt/keyrings/docker.gpg
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
        https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
        > /etc/apt/sources.list.d/docker.list
    apt-get update -q
    apt-get install -y -q docker-ce docker-ce-cli containerd.io docker-compose-plugin
    systemctl enable docker
    systemctl start docker
    echo "✅ Docker instalado"
else
    echo "✅ Docker ya instalado"
fi

# ── 2. Verificar que existe el archivo de producción ──────────────────────────
if [ ! -f ".env.production" ]; then
    echo "❌ Error: falta .env.production con las variables de entorno."
    echo "   Copia .env.production, rellena los valores y vuelve a ejecutar."
    exit 1
fi

# ── 3. Construir y levantar ────────────────────────────────────────────────────
echo "🔨 Construyendo imagen Docker..."
docker compose --env-file .env.production build --no-cache

echo "🟢 Levantando servicios..."
docker compose --env-file .env.production up -d

# ── 4. Esperar healthcheck ─────────────────────────────────────────────────────
echo "⏳ Esperando que la API arranque..."
for i in {1..30}; do
    if curl -sf http://localhost:8080/api/privacy > /dev/null 2>&1; then
        echo "✅ API disponible en http://$(curl -s ifconfig.me):8080"
        break
    fi
    echo "   intento $i/30..."
    sleep 5
done

echo ""
echo "======================================"
echo "  EcoHome desplegado correctamente"
echo "  API: http://$(curl -s ifconfig.me):8080"
echo "======================================"
echo ""
echo "Próximos pasos:"
echo "  1. Configura un dominio apuntando a esta IP"
echo "  2. Instala Nginx + Certbot para HTTPS"
echo "  3. Abre el puerto 8080 en el Security Group de EC2"
