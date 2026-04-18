const CT = {
    primary:      '#16a34a',
    primaryLight: '#22c55e',
    accent:       '#4ade80',
    danger:       '#ef4444',
    warning:      '#f59e0b',
    info:         '#38bdf8',
    purple:       '#a78bfa',
    gridColor:    'rgba(30,48,39,0.8)',
    textMuted:    '#8aab95',
    fontFamily:   "'Space Grotesk', sans-serif",
};
Chart.defaults.color          = CT.textMuted;
Chart.defaults.font.family    = CT.fontFamily;
Chart.defaults.font.size      = 12;
Chart.defaults.plugins.legend.labels.boxWidth = 12;
Chart.defaults.plugins.legend.labels.padding  = 16;

function buildGradient(ctx, color, alpha = 0.35) {
    const gradient = ctx.createLinearGradient(0, 0, 0, ctx.canvas.height);
    gradient.addColorStop(0, color.replace(')', `, ${alpha})`).replace('rgb', 'rgba'));
    gradient.addColorStop(1, color.replace(')', ', 0)').replace('rgb', 'rgba'));
    return gradient;
}
function limitLine(value, label, color) {
    return {
        type: 'line',
        yMin: value, yMax: value,
        borderColor: color,
        borderWidth: 1.5,
        borderDash: [6, 4],
        label: {
            content: label,
            display: true,
            position: 'end',
            backgroundColor: color,
            color: '#fff',
            font: { size: 10, weight: 'bold' },
            padding: { x: 6, y: 3 },
            borderRadius: 4,
        }
    };
}
const baseOptions = (yLabel, unit) => ({
    responsive: true,
    maintainAspectRatio: true,
    interaction: { mode: 'index', intersect: false },
    plugins: {
        legend: { display: true, position: 'top' },
        tooltip: {
            backgroundColor: '#111814',
            borderColor: '#1e3027',
            borderWidth: 1,
            titleColor: '#e2f0e8',
            bodyColor: '#8aab95',
            padding: 10,
            callbacks: {
                label: ctx => ` ${ctx.dataset.label}: ${ctx.parsed.y?.toFixed(1)} ${unit}`
            }
        }
    },
    scales: {
        x: {
            grid: { color: CT.gridColor },
            ticks: { maxTicksLimit: 12, maxRotation: 0 }
        },
        y: {
            grid: { color: CT.gridColor },
            title: { display: true, text: yLabel, color: CT.textMuted, font: { size: 11 } },
            beginAtZero: false,
        }
    }
});
let co2Chart, pmChart, stationChart;

function initCo2Chart() {
    const canvas = document.getElementById('co2Chart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    const datasets = buildStationDatasets(co2ChartData || [], CT.primaryLight, CT.info);

    const opts = baseOptions('CO₂ (ppm)', 'ppm');
    opts.plugins.annotation = {
        annotations: { limit: limitLine(1000, 'Limite 1000 ppm', CT.danger) }
    };
    co2Chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: extractLabels(co2ChartData || []),
            datasets
        },
        options: opts
    });
}
function initPmChart() {
    const canvas = document.getElementById('pmChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    const datasets = buildStationDatasets(pmChartData || [], CT.warning, CT.purple);

    const opts = baseOptions('PM (μg/m³)', 'μg/m³');
    opts.plugins.annotation = {
        annotations: { limit: limitLine(150, 'Limite 150 μg/m³', CT.danger) }
    };

    pmChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: extractLabels(pmChartData || []),
            datasets
        },
        options: opts
    });
}
function initStationChart() {
    const canvas = document.getElementById('stationChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    stationChart = new Chart(ctx, {
        type: 'bar',
        data: { labels: [], datasets: [] },
        options: {
            ...baseOptions('Valor', ''),
            plugins: {
                ...baseOptions('', '').plugins,
                legend: { display: true, position: 'top' }
            },
            scales: {
                x: { grid: { color: CT.gridColor } },
                y: {
                    grid: { color: CT.gridColor },
                    beginAtZero: true
                }
            }
        }
    });

    loadStationComparison();
}

function loadStationComparison() {
    fetch('/api/charts/stations')
        .then(r => r.json())
        .then(data => {
            stationChart.data.labels = data.labels || [];
            stationChart.data.datasets = [
                {
                    label: 'CO₂ (ppm)',
                    data: data.co2 || [],
                    backgroundColor: hexAlpha(CT.primaryLight, 0.7),
                    borderColor: CT.primaryLight,
                    borderWidth: 1.5,
                    borderRadius: 6,
                },
                {
                    label: 'PM (μg/m³)',
                    data: data.pm || [],
                    backgroundColor: hexAlpha(CT.warning, 0.7),
                    borderColor: CT.warning,
                    borderWidth: 1.5,
                    borderRadius: 6,
                }
            ];
            stationChart.update();
        })
        .catch(err => console.warn('Station chart error:', err));
}
const COLORS = [CT.primaryLight, CT.info, CT.warning, CT.purple, CT.accent];

function buildStationDatasets(rawData, ...colors) {
    if (!rawData || rawData.length === 0) return [];

    const groups = {};
    rawData.forEach(point => {
        const name = point.stationName || 'Desconhecida';
        if (!groups[name]) groups[name] = {};
        groups[name][point.label] = point.value;
    });
    return Object.entries(groups).map(([station, hourMap], idx) => {
        const color = COLORS[idx % COLORS.length];
        const allLabels = extractLabels(rawData);
        return {
            label: station,
            data: allLabels.map(lbl => hourMap[lbl] ?? null),
            borderColor: color,
            backgroundColor: hexAlpha(color, 0.1),
            pointBackgroundColor: color,
            pointRadius: 3,
            pointHoverRadius: 5,
            borderWidth: 2,
            tension: 0.4,
            fill: true,
            spanGaps: true,
        };
    });
}
function extractLabels(rawData) {
    const seen = new Set();
    return rawData
        .map(p => p.label)
        .filter(l => { if (seen.has(l)) return false; seen.add(l); return true; })
        .sort();
}

function hexAlpha(hex, alpha) {
    // Convert hex or named color to rgba
    const c = document.createElement('canvas').getContext('2d');
    c.fillStyle = hex;
    const computed = c.fillStyle; // resolves to #rrggbb
    const r = parseInt(computed.slice(1,3),16);
    const g = parseInt(computed.slice(3,5),16);
    const b = parseInt(computed.slice(5,7),16);
    return `rgba(${r},${g},${b},${alpha})`;
}
function refreshCharts() {
    // Refresh CO2 chart
    fetch('/api/charts/co2?hours=24')
        .then(r => r.json())
        .then(data => {
            if (!co2Chart) return;
            co2Chart.data.labels = data.labels || [];
            co2Chart.data.datasets[0].data = data.data || [];
            co2Chart.update('none');
        })
        .catch(() => {});

    // Refresh PM chart
    fetch('/api/charts/pm?hours=24')
        .then(r => r.json())
        .then(data => {
            if (!pmChart) return;
            pmChart.data.labels = data.labels || [];
            pmChart.data.datasets[0].data = data.data || [];
            pmChart.update('none');
        })
        .catch(() => {});

    // Refresh station comparison
    if (stationChart) loadStationComparison();
}
document.addEventListener('DOMContentLoaded', () => {
    // Load annotation plugin (CDN fallback check)
    if (!Chart.registry.plugins.get('annotation')) {
        console.info('Chart.js annotation plugin not loaded — limit lines disabled');
    }

    initCo2Chart();
    initPmChart();
    initStationChart();

    setInterval(refreshCharts, 60000);
});