"""HTML del panel de administración de Oráculo."""

_LOGIN_HTML = """<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Oráculo Admin — Acceso</title>
<style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:system-ui,sans-serif;background:#0f172a;color:#e2e8f0;
     display:flex;align-items:center;justify-content:center;min-height:100vh}
.card{background:#1e293b;border-radius:12px;padding:2rem;width:100%;max-width:360px;
      box-shadow:0 4px 24px rgba(0,0,0,.4)}
h1{font-size:1.6rem;margin-bottom:.4rem;color:#a78bfa}
p{font-size:.9rem;color:#94a3b8;margin-bottom:1.5rem}
input{width:100%;padding:.75rem 1rem;border-radius:8px;border:1px solid #334155;
      background:#0f172a;color:#e2e8f0;font-size:1rem;margin-bottom:1rem}
input:focus{outline:2px solid #7c3aed;border-color:transparent}
button{width:100%;padding:.75rem;background:#7c3aed;color:#fff;border:none;
       border-radius:8px;font-size:1rem;cursor:pointer;font-weight:600}
button:hover{background:#6d28d9}
.err{color:#f87171;font-size:.85rem;margin-top:.5rem;display:none}
</style>
</head>
<body>
<div class="card">
  <h1>🔮 Oráculo Admin</h1>
  <p>Introduce la clave de administración para acceder al panel.</p>
  <input id="key" type="password" placeholder="API Key" autofocus
         onkeydown="if(event.key==='Enter')login()">
  <button onclick="login()">Entrar</button>
  <div class="err" id="err">Clave incorrecta. Inténtalo de nuevo.</div>
</div>
<script>
function login(){
  const k=document.getElementById('key').value.trim();
  if(k) window.location.href='/admin?key='+encodeURIComponent(k);
}
const params=new URLSearchParams(location.search);
if(params.get('error')) document.getElementById('err').style.display='block';
</script>
</body>
</html>"""


def _bar(pct: float) -> str:
    filled = round(pct / 5)
    return "█" * filled + "░" * (20 - filled)


_DASHBOARD_HTML = r"""<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Oráculo Admin v0.8</title>
<style>
*{box-sizing:border-box;margin:0;padding:0}
:root{--bg:#0f172a;--card:#1e293b;--border:#334155;--accent:#7c3aed;
      --accent2:#a78bfa;--text:#e2e8f0;--muted:#94a3b8;--green:#4ade80;
      --red:#f87171;--yellow:#fbbf24}
body{font-family:system-ui,sans-serif;background:var(--bg);color:var(--text);
     min-height:100vh;padding:1rem}
header{display:flex;align-items:center;justify-content:space-between;
       margin-bottom:1.5rem;flex-wrap:wrap;gap:.5rem}
header h1{font-size:1.4rem;color:var(--accent2)}
header span{font-size:.8rem;color:var(--muted);background:var(--card);
            padding:.25rem .6rem;border-radius:20px}
.logout{background:transparent;border:1px solid var(--border);color:var(--muted);
        padding:.35rem .8rem;border-radius:6px;cursor:pointer;font-size:.85rem}
.logout:hover{border-color:var(--red);color:var(--red)}
.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:1rem;margin-bottom:1.5rem}
.card{background:var(--card);border-radius:10px;padding:1.2rem;border:1px solid var(--border)}
.card .val{font-size:2rem;font-weight:700;color:var(--accent2);line-height:1}
.card .lbl{font-size:.8rem;color:var(--muted);margin-top:.4rem}
section{background:var(--card);border-radius:10px;padding:1.2rem;
        border:1px solid var(--border);margin-bottom:1.2rem}
section h2{font-size:1rem;font-weight:600;margin-bottom:1rem;color:var(--accent2)}
.bar-row{display:flex;align-items:center;gap:.75rem;margin-bottom:.6rem;font-size:.85rem}
.bar-label{width:160px;flex-shrink:0;color:var(--muted);white-space:nowrap;
           overflow:hidden;text-overflow:ellipsis}
.bar-track{flex:1;background:#1a2035;border-radius:4px;height:12px;overflow:hidden}
.bar-fill{height:100%;background:var(--accent);border-radius:4px;transition:width .4s}
.bar-pct{width:40px;text-align:right;color:var(--text)}
.badge{display:inline-block;padding:.15rem .5rem;border-radius:4px;font-size:.75rem;font-weight:600}
.badge-green{background:#14532d;color:var(--green)}
.badge-red{background:#450a0a;color:var(--red)}
table{width:100%;border-collapse:collapse;font-size:.82rem}
th{text-align:left;padding:.5rem .75rem;color:var(--muted);border-bottom:1px solid var(--border);
   font-weight:500;white-space:nowrap}
td{padding:.5rem .75rem;border-bottom:1px solid #1e2d40;vertical-align:top}
tr:last-child td{border-bottom:none}
.ts{color:var(--muted);font-size:.75rem;white-space:nowrap}
.q{color:var(--text);max-width:300px}
.a{color:var(--muted);max-width:300px;font-size:.78rem}
details{margin-top:.5rem}
summary{cursor:pointer;color:var(--accent2);font-size:.85rem;padding:.3rem 0;list-style:none}
summary::before{content:"▶ "}
details[open] summary::before{content:"▼ "}
.conv-user{background:#12202e;border-radius:6px;padding:.6rem .8rem;margin:.4rem 0;font-size:.82rem}
.conv-user b{color:var(--accent2)}
.msg-q{color:var(--text);margin:.2rem 0}
.msg-a{color:var(--muted);font-size:.78rem}
.refresh{background:var(--accent);color:#fff;border:none;border-radius:6px;
         padding:.45rem 1rem;cursor:pointer;font-size:.85rem;font-weight:600}
.refresh:hover{background:#6d28d9}
.empty{color:var(--muted);font-size:.85rem;text-align:center;padding:1rem}
.stat-row{display:flex;gap:1rem;flex-wrap:wrap;margin-bottom:.8rem}
.stat-item{font-size:.9rem}<br>
.stat-item b{color:var(--text)}
.stat-item span{color:var(--muted)}
@media(max-width:600px){
  .bar-label{width:100px}
  .grid{grid-template-columns:1fr 1fr}
  th,td{padding:.4rem .5rem}
}
</style>
</head>
<body>
<header>
  <h1>🔮 Oráculo Admin</h1>
  <div style="display:flex;gap:.75rem;align-items:center;flex-wrap:wrap">
    <span id="last-update">Cargando...</span>
    <button class="refresh" onclick="loadAll()">↻ Actualizar</button>
    <button class="logout" onclick="logout()">Salir</button>
  </div>
</header>

<div class="grid" id="stat-cards">
  <div class="card"><div class="val" id="s-total">–</div><div class="lbl">Preguntas totales</div></div>
  <div class="card"><div class="val" id="s-today">–</div><div class="lbl">Preguntas hoy</div></div>
  <div class="card"><div class="val" id="s-users">–</div><div class="lbl">Usuarios únicos totales</div></div>
  <div class="card"><div class="val" id="s-users-today">–</div><div class="lbl">Usuarios únicos hoy</div></div>
  <div class="card"><div class="val" id="s-fb-pos">–</div><div class="lbl">👍 Útil</div></div>
  <div class="card"><div class="val" id="s-fb-neg">–</div><div class="lbl">👎 No útil</div></div>
  <div class="card"><div class="val" id="s-convs">–</div><div class="lbl">Conversaciones</div></div>
</div>

<section>
  <h2>📊 Uso por unidad</h2>
  <div id="unit-bars"><div class="empty">Sin datos de uso todavía.</div></div>
</section>

<section>
  <h2>💬 Preguntas con 👎 negativo</h2>
  <div id="neg-feedback">
    <div class="empty">Cargando...</div>
  </div>
</section>

<section>
  <h2>🗂️ Historial de conversaciones</h2>
  <div id="conversations">
    <div class="empty">Cargando...</div>
  </div>
</section>

<section>
  <h2>📋 Últimas entradas de feedback</h2>
  <div id="feedback-entries">
    <div class="empty">Cargando...</div>
  </div>
</section>

<script>
const KEY = decodeURIComponent(new URLSearchParams(location.search).get('key') || '');

function logout() {
  location.href = '/admin';
}

function h(s) {
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

function ts(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  return d.toLocaleDateString('es-ES') + ' ' + d.toLocaleTimeString('es-ES', {hour:'2-digit',minute:'2-digit'});
}

async function apiFetch(path) {
  const r = await fetch(path, {headers: {'X-API-Key': KEY}});
  if (!r.ok) throw new Error(r.status);
  return r.json();
}

async function loadAll() {
  document.getElementById('last-update').textContent = 'Actualizando...';
  try {
    const [stats, feedback, convs, books] = await Promise.all([
      apiFetch('/admin/stats'),
      apiFetch('/admin/feedback'),
      apiFetch('/admin/conversations'),
      fetch('/books').then(r => r.json()),
    ]);
    renderStats(stats.data, feedback.data, convs.data);
    renderUnitBars(stats.data, books.data ?? []);
    renderNegFeedback(feedback.data);
    renderConversations(convs.data);
    renderFeedbackEntries(feedback.data);
    document.getElementById('last-update').textContent =
      'Actualizado: ' + new Date().toLocaleTimeString('es-ES');
  } catch(e) {
    document.getElementById('last-update').textContent = 'Error al cargar datos';
  }
}

function renderStats(stats, fb, convs) {
  document.getElementById('s-total').textContent = stats.questions_total ?? '–';
  document.getElementById('s-today').textContent = stats.questions_today ?? '–';
  document.getElementById('s-users').textContent = stats.active_users ?? '–';
  document.getElementById('s-users-today').textContent = stats.active_users_today ?? '–';
  const pos = fb.positive ?? 0, neg = fb.negative ?? 0, total = fb.total ?? 0;
  const posPct = total > 0 ? Math.round(pos/total*100) : 0;
  const negPct = total > 0 ? Math.round(neg/total*100) : 0;
  document.getElementById('s-fb-pos').textContent = pos + ' (' + posPct + '%)';
  document.getElementById('s-fb-neg').textContent = neg + ' (' + negPct + '%)';
  document.getElementById('s-convs').textContent = convs.total_sessions ?? '–';
}

function renderUnitBars(stats, books) {
  const byUnit = stats.usage_by_unit ?? {};
  const total = stats.questions_total || 0;
  const el = document.getElementById('unit-bars');

  // Merge: all registered books as baseline with 0, then overlay real stats
  const merged = {};
  books.forEach(b => { merged[b.label] = 0; });
  Object.entries(byUnit).forEach(([k, v]) => { merged[k] = (merged[k] ?? 0) + v; });

  const entries = Object.entries(merged).sort((a, b) => b[1] - a[1]);
  if (!entries.length) { el.innerHTML = '<div class="empty">Sin unidades registradas.</div>'; return; }

  const maxCount = entries[0][1] || 1;
  el.innerHTML = entries.map(([label, count]) => {
    const pct = total > 0 ? Math.round(count / total * 100) : 0;
    const barW = Math.round(count / maxCount * 100);
    return `<div class="bar-row">
      <div class="bar-label" title="${h(label)}">${h(label)}</div>
      <div class="bar-track"><div class="bar-fill" style="width:${barW}%"></div></div>
      <div class="bar-pct">${pct}%</div>
      <div style="color:var(--muted);font-size:.78rem">(${count})</div>
    </div>`;
  }).join('');
}

function renderNegFeedback(fb) {
  const el = document.getElementById('neg-feedback');
  const entries = (fb.entries ?? []).filter(e => !e.useful && e.question);
  if (!entries.length) { el.innerHTML = '<div class="empty">¡Sin feedback negativo! 🎉</div>'; return; }
  el.innerHTML = `<table>
    <thead><tr><th>Pregunta</th><th>Respuesta (extracto)</th><th>Fecha</th></tr></thead>
    <tbody>` + entries.slice(-50).reverse().map(e =>
    `<tr>
      <td class="q">${h(e.question)}</td>
      <td class="a">${h((e.answer||'').slice(0,100))}${(e.answer||'').length>100?'…':''}</td>
      <td class="ts">${ts(e.timestamp)}</td>
    </tr>`
  ).join('') + '</tbody></table>';
}

function renderConversations(convs) {
  const el = document.getElementById('conversations');
  const all = convs.conversations ?? {};
  const users = Object.keys(all);
  if (!users.length) { el.innerHTML = '<div class="empty">Sin conversaciones registradas.</div>'; return; }
  const summary = `<div class="stat-row">
    <div class="stat-item"><span>Usuarios: </span><b>${convs.total_users}</b></div>
    <div class="stat-item"><span>Sesiones: </span><b>${convs.total_sessions}</b></div>
    <div class="stat-item"><span>Mensajes: </span><b>${convs.total_messages}</b></div>
  </div>`;
  const details = users.slice(0,20).map(uid => {
    const sessions = all[uid];
    const sids = Object.keys(sessions);
    const msgs = sids.map(sid => {
      const ms = sessions[sid];
      return `<div style="margin-left:1rem;margin-bottom:.4rem">
        <span style="color:var(--muted);font-size:.75rem">Sesión ${h(sid)} (${ms.length} msgs)</span>
        ${ms.slice(-3).map(m =>
          `<div class="msg-q">❓ ${h(m.question)}</div>
           <div class="msg-a">💬 ${h((m.answer||'').slice(0,120))}${(m.answer||'').length>120?'…':''}</div>`
        ).join('')}
      </div>`;
    }).join('');
    return `<details><summary><b>${h(uid)}</b> — ${sids.length} sesión(es)</summary>
      <div style="margin-top:.5rem">${msgs}</div>
    </details>`;
  }).join('');
  el.innerHTML = summary + '<div style="margin-top:.5rem">' + details + '</div>';
}

function renderFeedbackEntries(fb) {
  const el = document.getElementById('feedback-entries');
  const entries = (fb.entries ?? []).slice(-30).reverse();
  if (!entries.length) { el.innerHTML = '<div class="empty">Sin entradas de feedback.</div>'; return; }
  el.innerHTML = `<table>
    <thead><tr><th>Estado</th><th>Pregunta</th><th>Fecha</th></tr></thead>
    <tbody>` + entries.map(e =>
    `<tr>
      <td><span class="badge ${e.useful?'badge-green':'badge-red'}">${e.useful?'👍 Útil':'👎 No útil'}</span></td>
      <td class="q">${h(e.question||'(sin pregunta)')}</td>
      <td class="ts">${ts(e.timestamp)}</td>
    </tr>`
  ).join('') + '</tbody></table>';
}

loadAll();
setInterval(loadAll, 30000);
</script>
</body>
</html>"""


def get_login_html() -> str:
    return _LOGIN_HTML


def get_dashboard_html(key: str) -> str:
    return _DASHBOARD_HTML
