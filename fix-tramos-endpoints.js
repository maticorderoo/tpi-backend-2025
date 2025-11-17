const fs = require('fs');

// Leer la colección
let content = fs.readFileSync('TPI-2025-COMPLETE.postman_collection.json', 'utf8');

console.log('🔧 Corrigiendo endpoints de tramos...\n');

// Contar cambios
let changes = 0;

// Reemplazar /iniciar por /inicios
const iniciarRegex = /tramos\/\{\{tramo_id\}\}\/iniciar/g;
const iniciarMatches = content.match(iniciarRegex);
if (iniciarMatches) {
    content = content.replace(iniciarRegex, 'tramos/{{tramo_id}}/inicios');
    changes += iniciarMatches.length;
    console.log(`✅ Corregido "/iniciar" → "/inicios" (${iniciarMatches.length} ocurrencias)`);
}

// Reemplazar /finalizar por /finalizaciones
const finalizarRegex = /tramos\/\{\{tramo_id\}\}\/finalizar/g;
const finalizarMatches = content.match(finalizarRegex);
if (finalizarMatches) {
    content = content.replace(finalizarRegex, 'tramos/{{tramo_id}}/finalizaciones');
    changes += finalizarMatches.length;
    console.log(`✅ Corregido "/finalizar" → "/finalizaciones" (${finalizarMatches.length} ocurrencias)`);
}

// Guardar
fs.writeFileSync('TPI-2025-COMPLETE.postman_collection.json', content);
console.log(`\n✅ Total de correcciones: ${changes}`);
console.log('\nEndpoints correctos:');
console.log('  • Iniciar: {{logistics_base_url}}/tramos/{{tramo_id}}/inicios');
console.log('  • Finalizar: {{logistics_base_url}}/tramos/{{tramo_id}}/finalizaciones');
