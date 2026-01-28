let config = {};

function showStatus(message, duration = 2000) {
    const status = document.getElementById('status');
    status.textContent = message;
    status.classList.add('show');
    setTimeout(() => {
        status.classList.remove('show');
    }, duration);
}

function switchTab(tabName) {
    document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    
    event.target.classList.add('active');
    document.getElementById(tabName).classList.add('active');
}

function toggleSetting(element) {
    const id = element.id;
    const currentValue = getConfigValue(id);
    const newValue = !currentValue;
    
    setConfigValue(id, newValue);
    updateToggle(element, newValue);
    
    updateConfig();
}

function updateToggle(element, value) {
    if (value) {
        element.classList.add('active');
    } else {
        element.classList.remove('active');
    }
}

function updateNumber(element) {
    const id = element.id;
    const value = parseInt(element.value);
    setConfigValue(id, value);
    updateConfig();
}

function updateString(element) {
    const id = element.id;
    const value = element.value;
    setConfigValue(id, value);
    updateConfig();
}

function updateColor(element) {
    const id = element.id;
    const value = element.value;
    setConfigValue(id, value);
    document.getElementById(id + '-text').textContent = value.toUpperCase();
    updateConfig();
}

function getConfigValue(path) {
    const keys = path.split('.');
    let value = config;
    for (const key of keys) {
        if (value && typeof value === 'object' && key in value) {
            value = value[key];
        } else {
            return null;
        }
    }
    return value;
}

function setConfigValue(path, value) {
    const keys = path.split('.');
    let current = config;
    for (let i = 0; i < keys.length - 1; i++) {
        const key = keys[i];
        if (!(key in current) || typeof current[key] !== 'object') {
            current[key] = {};
        }
        current = current[key];
    }
    current[keys[keys.length - 1]] = value;
}

function loadConfig() {
    if (typeof mcef !== 'undefined' && mcef.getConfig) {
        try {
            const configJson = mcef.getConfig();
            config = JSON.parse(configJson);
            convertConfigFromJava(config);
            applyConfigToUI();
        } catch (e) {
            console.error('Failed to load config:', e);
            showStatus('Failed to load configuration', 3000);
            config = getDefaultConfig();
            applyConfigToUI();
        }
    } else {
        console.warn('MCEF bridge not available, using default config');
        config = getDefaultConfig();
        applyConfigToUI();
    }
}

function convertConfigFromJava(javaConfig) {
    if (javaConfig.mineshaft && javaConfig.mineshaft.mineshaftESP && javaConfig.mineshaft.mineshaftESP.mineshaftESPColor) {
        const color = javaConfig.mineshaft.mineshaftESP.mineshaftESPColor;
        if (typeof color === 'object' && color.r !== undefined) {
            const r = Math.round(color.r * 255).toString(16).padStart(2, '0');
            const g = Math.round(color.g * 255).toString(16).padStart(2, '0');
            const b = Math.round(color.b * 255).toString(16).padStart(2, '0');
            javaConfig.mineshaft.mineshaftESP.mineshaftESPColor = '#' + r + g + b;
        }
    }
    config = javaConfig;
}

function getDefaultConfig() {
    return {
        general: {
            masterSwitch: true,
            updateNotifications: true,
            checkForUpdates: true,
            partyCommands: true
        },
        mineshaft: {
            mineshaftFeaturesToggle: true,
            corpseFinder: true,
            mineshaftProfitHint: true,
            mineshaftCommands: false,
            traceThickness: 4,
            mineshaftParty: {
                mineshaftPartyMode: false,
                autoWarpToMineshaft: false,
                autoTransferPartyOnMineshaft: false,
                messageOnMineshaftSpawned: "!ptme Found mineshaft!"
            },
            mineshaftESP: {
                createWaypointToMineshaft: false,
                mineshaftESPColor: "#00FFFF"
            }
        },
        nucleusRuns: {
            jungleTempleCheese: true
        },
        dev: {
            debug: false
        }
    };
}

function applyConfigToUI() {
    document.querySelectorAll('.toggle').forEach(toggle => {
        const id = toggle.id;
        const value = getConfigValue(id);
        if (value !== null) {
            updateToggle(toggle, value);
        }
    });

    document.querySelectorAll('input[type="number"]').forEach(input => {
        const id = input.id;
        const value = getConfigValue(id);
        if (value !== null) {
            input.value = value;
        }
    });

    document.querySelectorAll('input[type="text"]').forEach(input => {
        const id = input.id;
        const value = getConfigValue(id);
        if (value !== null) {
            input.value = value;
        }
    });

    document.querySelectorAll('input[type="color"]').forEach(input => {
        const id = input.id;
        const value = getConfigValue(id);
        if (value !== null) {
            input.value = value;
            const textElement = document.getElementById(id + '-text');
            if (textElement) {
                textElement.textContent = value.toUpperCase();
            }
        }
    });
}

function updateConfig() {
    if (typeof mcef !== 'undefined' && mcef.updateConfig) {
        try {
            const configToSend = convertConfigToJava(JSON.parse(JSON.stringify(config)));
            mcef.updateConfig(JSON.stringify(configToSend));
        } catch (e) {
            console.error('Failed to update config:', e);
        }
    }
}

function convertConfigToJava(jsConfig) {
    if (jsConfig.mineshaft && jsConfig.mineshaft.mineshaftESP && jsConfig.mineshaft.mineshaftESP.mineshaftESPColor) {
        const colorHex = jsConfig.mineshaft.mineshaftESP.mineshaftESPColor;
        if (typeof colorHex === 'string' && colorHex.startsWith('#')) {
            const r = parseInt(colorHex.substr(1, 2), 16) / 255.0;
            const g = parseInt(colorHex.substr(3, 2), 16) / 255.0;
            const b = parseInt(colorHex.substr(5, 2), 16) / 255.0;
            jsConfig.mineshaft.mineshaftESP.mineshaftESPColor = {
                r: r,
                g: g,
                b: b,
                a: 1.0
            };
        }
    }
    return jsConfig;
}

function saveConfig() {
    if (typeof mcef !== 'undefined' && mcef.saveConfig) {
        try {
            const configToSend = convertConfigToJava(JSON.parse(JSON.stringify(config)));
            mcef.saveConfig(JSON.stringify(configToSend));
            showStatus('Configuration saved!', 2000);
        } catch (e) {
            console.error('Failed to save config:', e);
            showStatus('Failed to save configuration', 3000);
        }
    } else {
        showStatus('MCEF bridge not available', 3000);
    }
}

function resetConfig() {
    if (confirm('Are you sure you want to reset all settings to default?')) {
        config = getDefaultConfig();
        applyConfigToUI();
        updateConfig();
        showStatus('Configuration reset to defaults', 2000);
    }
}

window.addEventListener('load', () => {
    loadConfig();
});
