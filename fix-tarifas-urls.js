const fs = require('fs');

// Leer la colección
const collection = JSON.parse(fs.readFileSync('TPI-2025-COMPLETE.postman_collection.json', 'utf8'));

function fixTarifasUrls(items) {
    items.forEach(item => {
        // Si tiene carpetas anidadas, recursión
        if (item.item) {
            fixTarifasUrls(item.item);
        }
        
        // Si es la carpeta "Tarifas"
        if (item.name === 'Tarifas' && item.item) {
            console.log('📁 Encontrada carpeta Tarifas');
            item.item.forEach(endpoint => {
                if (endpoint.request && endpoint.request.url) {
                    const url = endpoint.request.url;
                    
                    // Si la URL es un objeto con "raw"
                    if (typeof url === 'object' && url.raw) {
                        // Cambiar de gateway_base_url o orders_base_url a fleet_base_url
                        if (url.raw.includes('tarifas')) {
                            const newUrl = url.raw
                                .replace('{{gateway_base_url}}/api/tarifas', '{{fleet_base_url}}/tarifas')
                                .replace('{{orders_base_url}}/tarifas', '{{fleet_base_url}}/tarifas');
                            
                            console.log(`  ✏️  ${endpoint.name}`);
                            console.log(`     Antes: ${url.raw}`);
                            console.log(`     Después: ${newUrl}`);
                            
                            // Convertir a formato simple
                            endpoint.request.url = newUrl;
                        }
                    }
                    // Si ya es un string simple
                    else if (typeof url === 'string' && url.includes('tarifas')) {
                        const newUrl = url
                            .replace('{{gateway_base_url}}/api/tarifas', '{{fleet_base_url}}/tarifas')
                            .replace('{{orders_base_url}}/tarifas', '{{fleet_base_url}}/tarifas');
                        
                        if (newUrl !== url) {
                            console.log(`  ✏️  ${endpoint.name}`);
                            console.log(`     Antes: ${url}`);
                            console.log(`     Después: ${newUrl}`);
                            endpoint.request.url = newUrl;
                        }
                    }
                }
            });
        }
    });
}

fixTarifasUrls(collection.item);

// Guardar
fs.writeFileSync('TPI-2025-COMPLETE.postman_collection.json', JSON.stringify(collection, null, 2));
console.log('\n✅ URLs de Tarifas corregidas para usar {{fleet_base_url}}');
