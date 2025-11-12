const fs = require('fs');

// Leer el docker-compose.yml
let content = fs.readFileSync('docker-compose.yml', 'utf8');

console.log('🔧 Corrigiendo healthchecks en docker-compose.yml...\n');

// Reemplazar todos los healthcheck que no tienen /api
const before = content;
content = content.replace(
    /test: \["CMD", "curl", "-f", "http:\/\/localhost:8080\/actuator\/health"\]/g,
    'test: ["CMD", "curl", "-f", "http://localhost:8080/api/actuator/health"]'
);

const changes = (before.match(/localhost:8080\/actuator\/health/g) || []).length;

// Guardar
fs.writeFileSync('docker-compose.yml', content);

if (changes > 0) {
    console.log(`✅ Corregidos ${changes} healthchecks para incluir /api en la ruta`);
    console.log('\nNueva ruta: http://localhost:8080/api/actuator/health');
} else {
    console.log('⚠️  No se encontraron healthchecks para corregir');
}
