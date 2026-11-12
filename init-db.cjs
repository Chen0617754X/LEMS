const fs = require('fs')
const path = require('path')
const mysql = require('mysql2/promise')
const dns = require('dns').promises

const SQL_FILE = path.join(__dirname, 'database', 'lems-cloud.sql')

const HOST = process.env.AIVEN_HOST || 'lems-mysql-lems-lumos.f.aivencloud.com'
const PORT = process.env.AIVEN_PORT || '15447'
const USER = process.env.AIVEN_USER || 'avnadmin'
const PASSWORD = process.env.AIVEN_PASSWORD
const DB = process.env.AIVEN_DB || 'defaultdb'

function splitSql(sql) {
  const lines = sql.split('\n').filter(l => !l.trim().startsWith('--'))
  return lines.join('\n')
    .split(';')
    .map(s => s.trim())
    .filter(s => s.length > 0)
}

async function waitForDns(maxWaitSec = 600) {
  const start = Date.now()
  while ((Date.now() - start) / 1000 < maxWaitSec) {
    try {
      const addrs = await dns.resolve4(HOST)
      if (addrs.length) {
        console.log(`DNS 已解析: ${HOST} -> ${addrs.join(', ')}`)
        return true
      }
    } catch (e) {
      // 继续等
    }
    const waited = Math.round((Date.now() - start) / 1000)
    console.log(`[${waited}s] DNS 尚未就绪，继续等待...`)
    await new Promise(r => setTimeout(r, 15000))
  }
  return false
}

;(async () => {
  if (!PASSWORD) {
    console.error('ERROR: 请设置 AIVEN_PASSWORD 环境变量')
    process.exit(1)
  }

  console.log(`等待 ${HOST} DNS 解析 (最多 10 分钟)...`)
  const ok = await waitForDns()
  if (!ok) {
    console.error('DNS 超时，数据库服务可能仍未 Running')
    process.exit(1)
  }

  console.log(`\n连接 ${USER}@${HOST}:${PORT}/${DB} ...`)
  let conn
  try {
    conn = await mysql.createConnection({
      host: HOST,
      port: Number(PORT),
      user: USER,
      password: PASSWORD,
      database: DB,
      ssl: { rejectUnauthorized: false },
      connectTimeout: 20000,
    })
  } catch (err) {
    console.error('连接失败:', err.code || '', err.message)
    if (err.code === 'ER_ACCESS_DENIED_ERROR') {
      console.error('-> 密码错误，或服务还在初始化中（Building 状态）')
    }
    process.exit(1)
  }
  console.log('连接成功\n')

  const sql = fs.readFileSync(SQL_FILE, 'utf8')
  const statements = splitSql(sql)
  console.log(`执行 ${statements.length} 条语句\n`)

  let ok2 = 0, skipped = 0, failed = 0
  for (const stmt of statements) {
    const preview = stmt.replace(/\s+/g, ' ').slice(0, 55)
    try {
      await conn.query(stmt)
      ok2++
      console.log(`  [OK] ${preview}...`)
    } catch (err) {
      if (err.code === 'ER_DB_CREATE_EXISTS' || err.code === 1007) {
        skipped++
        console.log(`  [SKIP] ${preview}...`)
      } else {
        failed++
        console.log(`  [FAIL] ${preview}... -> ${err.code} ${err.message}`)
      }
    }
  }

  console.log(`\n结果: 成功 ${ok2}, 跳过 ${skipped}, 失败 ${failed}`)

  console.log('\n--- 数据验证 ---')
  const [tables] = await conn.query(
    "SELECT TABLE_NAME AS t FROM information_schema.TABLES WHERE TABLE_SCHEMA=? ORDER BY TABLE_NAME",
    [DB]
  )
  console.log('表:', tables.map(t => t.t).join(', '))
  for (const [label, sql] of [
    ['用户', 'SELECT COUNT(*) c FROM sys_user'],
    ['分类', 'SELECT COUNT(*) c FROM equipment_category'],
    ['设备', 'SELECT COUNT(*) c FROM equipment'],
    ['维修', 'SELECT COUNT(*) c FROM equipment_repair'],
  ]) {
    const [r] = await conn.query(sql)
    console.log(`${label}: ${r[0].c}`)
  }

  await conn.end()
  console.log('\n数据库初始化完成！')
  if (failed > 0) process.exit(1)
})().catch(e => {
  console.error('ERROR:', e.code || '', e.message)
  process.exit(1)
})
