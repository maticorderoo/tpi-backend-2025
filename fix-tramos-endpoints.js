const fs = require('fs');

// Leer la colección
let content = fs.readFileSync('TPI-2025-COMPLETE.postman_collection.json', 'utf8');

console.log('🔧 Corrigiendo endpoints de tramos...\n');

// Contar cambios
let changes = 0;

// Reemplazar /iniciar por /inicio
const iniciarRegex = /tramos\/\{\{tramo_id\}\}\/iniciar/g;
const iniciarMatches = content.match(iniciarRegex);
if (iniciarMatches) {
    content = content.replace(iniciarRegex, 'tramos/{{tramo_id}}/inicio');
    changes += iniciarMatches.length;
    console.log(`✅ Corregido "/iniciar" → "/inicio" (${iniciarMatches.length} ocurrencias)`);
}

// Reemplazar /finalizar por /fin
const finalizarRegex = /tramos\/\{\{tramo_id\}\}\/finalizar/g;
const finalizarMatches = content.match(finalizarRegex);
if (finalizarMatches) {
    content = content.replace(finalizarRegex, 'tramos/{{tramo_id}}/fin');
    changes += finalizarMatches.length;
    console.log(`✅ Corregido "/finalizar" → "/fin" (${finalizarMatches.length} ocurrencias)`);
}

// Guardar
fs.writeFileSync('TPI-2025-COMPLETE.postman_collection.json', content);
console.log(`\n✅ Total de correcciones: ${changes}`);
console.log('\nEndpoints correctos:');
console.log('  • Iniciar: {{logistics_base_url}}/tramos/{{tramo_id}}/inicio');
console.log('  • Finalizar: {{logistics_base_url}}/tramos/{{tramo_id}}/fin');
